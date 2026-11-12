-- Migración sin borrar reportes ni usuarios existentes. Ejecutar completa como administrador.
begin;
set local lock_timeout = '10s';

create schema if not exists privado;
revoke all on schema privado from public, anon;
grant usage on schema privado to authenticated;

create table if not exists public.perfiles (
 id uuid primary key references auth.users(id),
 nombre text not null check (char_length(nombre) between 1 and 100),
 correo text not null,
 rol text not null default 'usuario' check (rol in ('usuario','encargado')),
 creado_en timestamptz not null default now()
);
insert into public.perfiles(id,nombre,correo)
select id,left(coalesce(nullif(btrim(raw_user_meta_data->>'nombre'),''),nullif(split_part(email,'@',1),''),'Usuario'),100),coalesce(email,'')
from auth.users on conflict(id) do nothing;

create or replace function privado.crear_perfil() returns trigger
language plpgsql security definer set search_path='' as $$
begin
 insert into public.perfiles(id,nombre,correo)
 values(new.id,left(coalesce(nullif(btrim(new.raw_user_meta_data->>'nombre'),''),nullif(split_part(new.email,'@',1),''),'Usuario'),100),coalesce(new.email,''))
 on conflict(id) do nothing;
 return new;
end; $$;
drop trigger if exists upt_crear_perfil on auth.users;
create trigger upt_crear_perfil after insert on auth.users for each row execute function privado.crear_perfil();

create or replace function privado.es_encargado() returns boolean
language sql stable security definer set search_path='' as $$
 select exists(select 1 from public.perfiles where id=auth.uid() and rol='encargado');
$$;
revoke all on public.perfiles from public,anon,authenticated;
grant select on public.perfiles to authenticated;
alter table public.perfiles enable row level security;
drop policy if exists perfil_propio on public.perfiles;
create policy perfil_propio on public.perfiles for select to authenticated using(id=(select auth.uid()));

create table if not exists public.reporte_detalles (
 reporte_id uuid primary key references public.reportes(id),
 autor_id uuid not null references auth.users(id),
 particulares jsonb not null default '{"senal":"","accesorios":"","inscripcion":""}',
 foto_ruta text,
 check(jsonb_typeof(particulares)='object' and octet_length(particulares::text)<=2048)
);
insert into public.reporte_detalles(reporte_id,autor_id) select id,autor_id from public.reportes on conflict do nothing;
alter table public.reporte_detalles enable row level security;
revoke all on public.reporte_detalles from public,anon,authenticated;
grant select on public.reporte_detalles to authenticated;
drop policy if exists detalles_autorizados on public.reporte_detalles;
create policy detalles_autorizados on public.reporte_detalles for select to authenticated
 using(autor_id=(select auth.uid()) or (select privado.es_encargado()));

-- Los estudiantes consultan sus reportes; los candidatos ajenos solo se devuelven resumidos por RPC.
drop policy if exists reportes_lectura on public.reportes;
create policy reportes_lectura on public.reportes for select to authenticated using(autor_id=(select auth.uid()));
alter table public.reportes enable row level security;
revoke all on public.reportes from public,anon,authenticated;
grant select on public.reportes to authenticated;
alter table public.reportes drop constraint if exists reporte_version_estado;
alter table public.reportes drop constraint if exists reportes_estado_check;
alter table public.reportes drop constraint if exists reporte_estado_v3;
alter table public.reportes add constraint reporte_estado_v3
 check(estado in ('ACTIVO','CANCELADO','EN_CUSTODIA','RECUPERADO') and version>0);

create or replace function public.validar_reporte() returns trigger
language plpgsql set search_path='' as $$
begin
 if tg_op='INSERT' then
  if new.fecha_evento>(now() at time zone 'America/Lima')::date then raise exception 'Fecha futura no permitida.' using errcode='22023'; end if;
 else
  if row(new.id,new.autor_id,new.tipo,new.categoria,new.descripcion,new.fecha_evento,new.zona,new.referencia,new.caracteristicas,new.area_entrega,new.creado_en)
   is distinct from row(old.id,old.autor_id,old.tipo,old.categoria,old.descripcion,old.fecha_evento,old.zona,old.referencia,old.caracteristicas,old.area_entrega,old.creado_en)
   then raise exception 'Las características publicadas no se editan.' using errcode='22023'; end if;
  if new.version<>old.version+1 or not (
   (old.estado='ACTIVO' and new.estado='CANCELADO') or
   (old.tipo='ENCONTRADO' and old.estado='ACTIVO' and new.estado='EN_CUSTODIA') or
   (old.estado in ('ACTIVO','EN_CUSTODIA') and new.estado='RECUPERADO')
  ) then raise exception 'Transición de estado no permitida.' using errcode='22023'; end if;
 end if;
 new.actualizado_en:=clock_timestamp();
 return new;
end; $$;

create or replace function privado.particulares(datos jsonb) returns jsonb
language plpgsql immutable set search_path='' as $$
declare p jsonb:=coalesce(datos,'{}'); clave text;
begin
 if jsonb_typeof(p)<>'object' then raise exception 'Características particulares inválidas.' using errcode='22023'; end if;
 foreach clave in array array['senal','accesorios','inscripcion'] loop
  if p ? clave and (jsonb_typeof(p->clave)<>'string' or char_length(p->>clave)>160) then
   raise exception 'Usa textos breves en las características.' using errcode='22023';
  end if;
 end loop;
 return jsonb_build_object('senal',btrim(coalesce(p->>'senal','')),'accesorios',btrim(coalesce(p->>'accesorios','')),'inscripcion',btrim(coalesce(p->>'inscripcion','')));
end; $$;

create or replace function privado.reporte_json(identificador uuid) returns jsonb
language sql stable set search_path='' as $$
 select to_jsonb(r)||jsonb_build_object('particulares',d.particulares,'foto_ruta',d.foto_ruta)
 from public.reportes r join public.reporte_detalles d on d.reporte_id=r.id where r.id=identificador;
$$;

drop function if exists public.guardar_reporte(jsonb);
create function public.guardar_reporte(datos jsonb) returns jsonb
language plpgsql security definer set search_path='' as $$
declare u uuid:=auth.uid(); r public.reportes; d public.reporte_detalles; i uuid;
 p jsonb; foto text; nuevo boolean:=false; estado_n text; version_n integer;
begin
 if u is null then raise exception 'Inicia sesión.' using errcode='42501'; end if;
 if jsonb_typeof(datos) is distinct from 'object' or not(datos ?& array['id','tipo','categoria','descripcion','fechaEvento','zona','referencia','caracteristicas','estado','version'])
 then raise exception 'Reporte incompleto.' using errcode='22023'; end if;
 i:=(datos->>'id')::uuid; estado_n:=datos->>'estado'; version_n:=(datos->>'version')::integer;
 if not((estado_n='ACTIVO' and version_n=1) or (estado_n='CANCELADO' and version_n=2)) then
  raise exception 'El cliente solo puede publicar o cancelar.' using errcode='22023'; end if;
 p:=privado.particulares(datos->'particulares'); foto:=nullif(datos->>'fotoRuta','');
 select * into r from public.reportes where id=i for update;
 if not found then
  if p->>'senal'='' and p->>'accesorios'='' and p->>'inscripcion'='' then
   raise exception 'Registra al menos una característica particular.' using errcode='22023'; end if;
  if datos->>'tipo'='ENCONTRADO' and foto is null then raise exception 'El hallazgo requiere una foto.' using errcode='22023'; end if;
  if foto is not null and (foto !~ ('^'||u::text||'/'||i::text||'/[a-f0-9]{64}\.jpg$') or
   not exists(select 1 from storage.objects where bucket_id='fotos-reportes' and name=foto)) then
   raise exception 'La foto no está disponible para esta cuenta.' using errcode='22023'; end if;
  insert into public.reportes(id,autor_id,tipo,categoria,descripcion,fecha_evento,zona,referencia,caracteristicas,area_entrega,estado,version)
  values(i,u,datos->>'tipo',datos->>'categoria',datos->>'descripcion',(datos->>'fechaEvento')::date,datos->>'zona',datos->>'referencia',datos->'caracteristicas',datos->>'areaEntrega',estado_n,version_n)
  on conflict(id) do nothing returning * into r;
  if found then
   insert into public.reporte_detalles(reporte_id,autor_id,particulares,foto_ruta) values(i,u,p,foto);
   return privado.reporte_json(i);
  end if;
  select * into strict r from public.reportes where id=i for update;
 end if;
 if r.autor_id<>u then raise exception 'No puedes modificar este reporte.' using errcode='42501'; end if;
 select * into strict d from public.reporte_detalles where reporte_id=i;
 if row(r.tipo,r.categoria,r.descripcion,r.fecha_evento,r.zona,r.referencia,r.caracteristicas,r.area_entrega,d.particulares,d.foto_ruta)
 is distinct from row(datos->>'tipo',datos->>'categoria',datos->>'descripcion',(datos->>'fechaEvento')::date,datos->>'zona',datos->>'referencia',datos->'caracteristicas',datos->>'areaEntrega',p,foto)
 then raise exception 'Las características originales no coinciden.' using errcode='22023'; end if;
 if version_n>r.version then
  if r.estado<>'ACTIVO' or estado_n<>'CANCELADO' then raise exception 'El reporte ya cambió de estado.' using errcode='22023'; end if;
  update public.reportes set estado=estado_n,version=version_n where id=i;
 end if;
 return privado.reporte_json(i);
end; $$;

create or replace function public.mis_reportes(inicio integer default 0,limite integer default 200)
returns setof jsonb language sql stable security definer set search_path='' as $$
 select privado.reporte_json(id) from public.reportes where autor_id=auth.uid()
 order by id limit least(greatest(limite,1),200) offset greatest(inicio,0);
$$;

create table if not exists public.coincidencias (
 id uuid primary key default gen_random_uuid(), perdida_id uuid not null references public.reportes(id),
 hallazgo_id uuid not null references public.reportes(id), solicitante_id uuid not null references auth.users(id),
 categoria text not null, zona text not null, fecha_evento date not null,
 puntaje integer not null check(puntaje between 0 and 100), motivos jsonb not null,
 activa boolean not null default true, actualizado_en timestamptz not null default now(),
 unique(perdida_id,hallazgo_id)
);
alter table public.coincidencias enable row level security;
revoke all on public.coincidencias from public,anon,authenticated;
grant select on public.coincidencias to authenticated;
drop policy if exists coincidencias_propias on public.coincidencias;
create policy coincidencias_propias on public.coincidencias for select to authenticated using(solicitante_id=(select auth.uid()));

create or replace function privado.normalizar(t text) returns text language sql immutable set search_path='' as $$
 select btrim(regexp_replace(translate(lower(coalesce(t,'')),'áéíóúüñ','aeiouun'),'[^a-z0-9]+',' ','g'));
$$;
create or replace function privado.tokens(t text) returns text[] language sql immutable set search_path='' as $$
 select coalesce(array_agg(distinct x),'{}') from unnest(string_to_array(privado.normalizar(t),' ')) x
 where x<>'' and x<>all(array['de','la','el','en','un','una','con','y','del','los','las','al','mi','mis']);
$$;
create or replace function privado.similitud(a text,b text) returns numeric language sql immutable set search_path='' as $$
 with ta as(select unnest(privado.tokens(a)) x),tb as(select unnest(privado.tokens(b)) x),
 union_t as(select x from ta union select x from tb),comunes as(select x from ta intersect select x from tb)
 select coalesce((select count(*)::numeric from comunes)/nullif((select count(*) from union_t),0),0);
$$;
create or replace function privado.puntuar(p public.reportes,h public.reportes,cp jsonb,ch jsonb) returns integer
language plpgsql stable set search_path='' as $$
declare s numeric; a numeric; i numeric; puntos numeric; diferencia integer;
begin
 if privado.normalizar(p.categoria)<>privado.normalizar(h.categoria) then return null; end if;
 diferencia:=h.fecha_evento-p.fecha_evento;
 if diferencia< -1 or diferencia>30 then return null; end if;
 if privado.normalizar(p.caracteristicas->>'marca')<>'' and privado.normalizar(h.caracteristicas->>'marca')<>''
  and privado.normalizar(p.caracteristicas->>'marca')<>privado.normalizar(h.caracteristicas->>'marca') then return null; end if;
 if privado.normalizar(p.caracteristicas->>'modelo')<>'' and privado.normalizar(h.caracteristicas->>'modelo')<>''
  and privado.normalizar(p.caracteristicas->>'modelo')<>privado.normalizar(h.caracteristicas->>'modelo') then return null; end if;
 s:=privado.similitud(cp->>'senal',ch->>'senal'); a:=privado.similitud(cp->>'accesorios',ch->>'accesorios'); i:=privado.similitud(cp->>'inscripcion',ch->>'inscripcion');
 if greatest(s,a,i)<0.6 then return null; end if;
 puntos:=(case when s>=0.6 then 35*s else 0 end)+(case when a>=0.6 then 25*a else 0 end)+(case when i>=0.6 then 30*i else 0 end);
 if privado.normalizar(p.caracteristicas->>'marca')<>'' and privado.normalizar(p.caracteristicas->>'marca')=privado.normalizar(h.caracteristicas->>'marca') then puntos:=puntos+5; end if;
 if privado.normalizar(p.caracteristicas->>'modelo')<>'' and privado.normalizar(p.caracteristicas->>'modelo')=privado.normalizar(h.caracteristicas->>'modelo') then puntos:=puntos+10; end if;
 if privado.normalizar(p.caracteristicas->>'color')<>'' and privado.normalizar(p.caracteristicas->>'color')=privado.normalizar(h.caracteristicas->>'color') then puntos:=puntos+5; end if;
 if privado.normalizar(p.zona)=privado.normalizar(h.zona) then puntos:=puntos+5; end if;
 puntos:=puntos+case when abs(diferencia)<=3 then 5 when abs(diferencia)<=7 then 2 else 0 end;
 if round(puntos)<35 then return null; end if;
 return least(round(puntos)::integer,100);
end; $$;

create or replace function public.buscar_coincidencias(perdida_id uuid) returns setof public.coincidencias
language plpgsql security definer set search_path='' as $$
declare p public.reportes; dp jsonb; h record; puntos integer;
begin
 select * into strict p from public.reportes where id=perdida_id and autor_id=auth.uid() and tipo='PERDIDO' and estado='ACTIVO';
 select particulares into dp from public.reporte_detalles where reporte_id=p.id;
 update public.coincidencias c set activa=false where c.perdida_id=p.id;
 for h in select r.*,d.particulares from public.reportes r join public.reporte_detalles d on d.reporte_id=r.id
  where r.tipo='ENCONTRADO' and r.estado in ('ACTIVO','EN_CUSTODIA') and r.autor_id<>p.autor_id loop
  puntos:=privado.puntuar(p,(select r from public.reportes r where r.id=h.id),dp,h.particulares);
  if puntos is not null then
   insert into public.coincidencias(perdida_id,hallazgo_id,solicitante_id,categoria,zona,fecha_evento,puntaje,motivos)
   values(p.id,h.id,p.autor_id,h.categoria,h.zona,h.fecha_evento,puntos,'["Coincidencia en características particulares","Categoría compatible","Fecha dentro del intervalo de búsqueda"]')
   on conflict on constraint coincidencias_perdida_id_hallazgo_id_key do update set puntaje=excluded.puntaje,motivos=excluded.motivos,
    activa=true,actualizado_en=now();
  end if;
 end loop;
 return query select c.* from public.coincidencias c where c.perdida_id=p.id and c.activa order by c.puntaje desc,c.id;
end; $$;

create table if not exists public.solicitudes_recuperacion (
 id uuid primary key, coincidencia_id uuid not null references public.coincidencias(id),
 solicitante_id uuid not null references auth.users(id), perdida_id uuid not null references public.reportes(id),
 hallazgo_id uuid not null references public.reportes(id), explicacion text not null check(char_length(btrim(explicacion)) between 10 and 1000),
 estado text not null default 'PENDIENTE' check(estado in ('PENDIENTE','APROBADA','RECHAZADA','ENTREGADA')),
 creado_en timestamptz not null default now(), actualizado_en timestamptz not null default now()
);
create unique index if not exists solicitud_activa_usuario on public.solicitudes_recuperacion(solicitante_id,hallazgo_id) where estado in ('PENDIENTE','APROBADA');
create unique index if not exists solicitud_aprobada_objeto on public.solicitudes_recuperacion(hallazgo_id) where estado='APROBADA';
alter table public.solicitudes_recuperacion enable row level security;
revoke all on public.solicitudes_recuperacion from public,anon,authenticated;
grant select on public.solicitudes_recuperacion to authenticated;
drop policy if exists solicitudes_autorizadas on public.solicitudes_recuperacion;
create policy solicitudes_autorizadas on public.solicitudes_recuperacion for select to authenticated
 using(solicitante_id=(select auth.uid()) or (select privado.es_encargado()));

create table if not exists public.custodias (
 reporte_id uuid primary key references public.reportes(id), area text not null check(char_length(btrim(area)) between 1 and 120),
 recibido_por uuid not null references auth.users(id), recibido_en timestamptz not null default now()
);
create table if not exists public.entregas (
 id uuid primary key default gen_random_uuid(), solicitud_id uuid not null unique references public.solicitudes_recuperacion(id),
 entregado_por uuid not null references auth.users(id), entregado_a uuid not null references auth.users(id),
 entregado_en timestamptz not null default now()
);
alter table public.custodias enable row level security;
alter table public.entregas enable row level security;
revoke all on public.custodias,public.entregas from public,anon,authenticated;
grant select on public.custodias,public.entregas to authenticated;
drop policy if exists custodias_autorizadas on public.custodias;
create policy custodias_autorizadas on public.custodias for select to authenticated using(
 (select privado.es_encargado()) or exists(select 1 from public.reportes r where r.id=reporte_id and r.autor_id=auth.uid()));
drop policy if exists entregas_autorizadas on public.entregas;
create policy entregas_autorizadas on public.entregas for select to authenticated using(entregado_a=(select auth.uid()) or (select privado.es_encargado()));

create or replace function public.solicitar_recuperacion(datos jsonb) returns public.solicitudes_recuperacion
language plpgsql security definer set search_path='' as $$
declare c public.coincidencias; s public.solicitudes_recuperacion; p public.reportes; h public.reportes; id_n uuid:=(datos->>'id')::uuid;
begin
 if auth.uid() is null then raise exception 'Inicia sesión.' using errcode='42501'; end if;
 select * into s from public.solicitudes_recuperacion where id=id_n;
 if found then
  if s.solicitante_id<>auth.uid() or s.coincidencia_id<>(datos->>'coincidenciaId')::uuid or s.explicacion<>datos->>'explicacion' then
   raise exception 'Solicitud incompatible.' using errcode='42501'; end if;
  return s;
 end if;
 select * into strict c from public.coincidencias where id=(datos->>'coincidenciaId')::uuid and solicitante_id=auth.uid() and activa;
 perform id from public.reportes where id in(c.perdida_id,c.hallazgo_id) order by id for update;
 select * into strict p from public.reportes where id=c.perdida_id and estado='ACTIVO';
 select * into strict h from public.reportes where id=c.hallazgo_id and estado in ('ACTIVO','EN_CUSTODIA');
 if privado.puntuar(p,h,(select particulares from public.reporte_detalles where reporte_id=p.id),(select particulares from public.reporte_detalles where reporte_id=h.id)) is null then
  raise exception 'La coincidencia dejó de estar disponible.' using errcode='22023'; end if;
 insert into public.solicitudes_recuperacion(id,coincidencia_id,solicitante_id,perdida_id,hallazgo_id,explicacion)
 values(id_n,c.id,auth.uid(),p.id,h.id,datos->>'explicacion') returning * into s;
 return s;
end; $$;

create or replace function public.hallazgos_encargado() returns setof jsonb
language plpgsql stable security definer set search_path='' as $$
begin
 if not privado.es_encargado() then raise exception 'Acceso reservado al encargado.' using errcode='42501'; end if;
 return query select privado.reporte_json(id) from public.reportes where tipo='ENCONTRADO' and estado in ('ACTIVO','EN_CUSTODIA') order by creado_en desc;
end; $$;

create or replace function public.solicitudes_encargado() returns setof jsonb
language plpgsql stable security definer set search_path='' as $$
begin
 if not privado.es_encargado() then raise exception 'Acceso reservado al encargado.' using errcode='42501'; end if;
 return query select to_jsonb(s)||jsonb_build_object('nombre_solicitante',p.nombre)
 from public.solicitudes_recuperacion s join public.perfiles p on p.id=s.solicitante_id
 order by s.creado_en desc;
end; $$;

create or replace function public.recibir_hallazgo(reporte_id uuid,area text) returns jsonb
language plpgsql security definer set search_path='' as $$
declare r public.reportes;
begin
 if not privado.es_encargado() then raise exception 'Acceso reservado al encargado.' using errcode='42501'; end if;
 select * into strict r from public.reportes where id=reporte_id and tipo='ENCONTRADO' for update;
 if r.estado='EN_CUSTODIA' then return privado.reporte_json(r.id); end if;
 if r.estado<>'ACTIVO' then raise exception 'El hallazgo no está disponible.' using errcode='22023'; end if;
 insert into public.custodias(reporte_id,area,recibido_por) values(r.id,area,auth.uid());
 update public.reportes set estado='EN_CUSTODIA',version=version+1 where id=r.id;
 return privado.reporte_json(r.id);
end; $$;

create or replace function public.resolver_solicitud(solicitud_id uuid,accion text,propiedad_verificada boolean default false)
returns public.solicitudes_recuperacion language plpgsql security definer set search_path='' as $$
declare s public.solicitudes_recuperacion;
begin
 if not privado.es_encargado() then raise exception 'Acceso reservado al encargado.' using errcode='42501'; end if;
 select * into strict s from public.solicitudes_recuperacion where id=solicitud_id for update;
 perform id from public.reportes where id in(s.perdida_id,s.hallazgo_id) order by id for update;
 if s.estado=accion and accion in ('APROBADA','RECHAZADA','ENTREGADA') then return s; end if;
 if accion='RECHAZADA' and s.estado in ('PENDIENTE','APROBADA') then
  update public.solicitudes_recuperacion set estado='RECHAZADA',actualizado_en=now() where id=s.id returning * into s;
 elsif accion='APROBADA' and s.estado='PENDIENTE' and propiedad_verificada then
  if not exists(select 1 from public.reportes where id=s.hallazgo_id and estado='EN_CUSTODIA') then raise exception 'Primero registra la recepción del objeto.' using errcode='22023'; end if;
  if not exists(select 1 from public.reportes where id=s.perdida_id and estado='ACTIVO') then raise exception 'La pérdida ya no está activa.' using errcode='22023'; end if;
  update public.solicitudes_recuperacion set estado='APROBADA',actualizado_en=now() where id=s.id returning * into s;
 elsif accion='ENTREGADA' and s.estado='APROBADA' and propiedad_verificada then
  if not exists(select 1 from public.reportes where id=s.hallazgo_id and estado='EN_CUSTODIA') then raise exception 'El objeto no está en custodia.' using errcode='22023'; end if;
  if not exists(select 1 from public.reportes where id=s.perdida_id and estado='ACTIVO') then raise exception 'La pérdida ya no está activa.' using errcode='22023'; end if;
  update public.reportes set estado='RECUPERADO',version=version+1 where id in(s.perdida_id,s.hallazgo_id);
  insert into public.entregas(solicitud_id,entregado_por,entregado_a) values(s.id,auth.uid(),s.solicitante_id);
  update public.solicitudes_recuperacion set estado='ENTREGADA',actualizado_en=now() where id=s.id returning * into s;
  update public.solicitudes_recuperacion set estado='RECHAZADA',actualizado_en=now() where hallazgo_id=s.hallazgo_id and id<>s.id and estado in ('PENDIENTE','APROBADA');
  update public.coincidencias set activa=false where hallazgo_id=s.hallazgo_id or perdida_id=s.perdida_id;
 else raise exception 'La acción requiere una validación presencial o no corresponde al estado actual.' using errcode='22023';
 end if;
 return s;
end; $$;

insert into storage.buckets(id,name,public,file_size_limit,allowed_mime_types)
values('fotos-reportes','fotos-reportes',false,5242880,array['image/jpeg'])
on conflict(id) do update set public=false,file_size_limit=excluded.file_size_limit,allowed_mime_types=excluded.allowed_mime_types;
drop policy if exists upt_foto_insertar on storage.objects;
create policy upt_foto_insertar on storage.objects for insert to authenticated with check(
 bucket_id='fotos-reportes' and split_part(name,'/',1)=(select auth.uid())::text
 and name ~ '^[a-f0-9-]{36}/[a-f0-9-]{36}/[a-f0-9]{64}\.jpg$');
drop policy if exists upt_foto_leer on storage.objects;
create policy upt_foto_leer on storage.objects for select to authenticated using(bucket_id='fotos-reportes' and
 (split_part(name,'/',1)=(select auth.uid())::text or (select privado.es_encargado())));

revoke all on all functions in schema privado from public,anon,authenticated;
grant execute on function privado.es_encargado() to authenticated;
revoke all on function public.validar_reporte() from public,anon,authenticated;
revoke all on function public.guardar_reporte(jsonb),public.mis_reportes(integer,integer),public.buscar_coincidencias(uuid),
 public.solicitar_recuperacion(jsonb),public.hallazgos_encargado(),public.solicitudes_encargado(),public.recibir_hallazgo(uuid,text),public.resolver_solicitud(uuid,text,boolean)
 from public,anon,authenticated;
grant execute on function public.guardar_reporte(jsonb),public.mis_reportes(integer,integer),public.buscar_coincidencias(uuid),
 public.solicitar_recuperacion(jsonb),public.hallazgos_encargado(),public.solicitudes_encargado(),public.recibir_hallazgo(uuid,text),public.resolver_solicitud(uuid,text,boolean)
 to authenticated;
notify pgrst,'reload schema';
commit;

-- El rol encargado solo se asigna administrativamente a una cuenta elegida por el responsable.
-- No se cambia el rol de ningún usuario durante esta migración.
