-- Ejecutar después de subir las cinco imágenes de supabase/fixtures al bucket privado fotos-reportes.
-- Solo añade el escenario e120; conserva registros, credenciales y roles existentes.
begin;
do $$
declare fila record; datos jsonb; expositor uuid:='f8086ec1-1c12-4901-abc3-a8cb9c69de81'; apoyo uuid:='accced61-6879-47a4-ad01-aa38eaf4f4c5'; c public.coincidencias; s public.solicitudes_recuperacion;
begin
 if not exists(select 1 from auth.users where id=expositor) or not exists(select 1 from auth.users where id=apoyo) then
  raise exception 'El escenario requiere las dos cuentas registradas indicadas.'; end if;
 if not exists(select 1 from public.perfiles where id=expositor and rol='encargado') then
  raise exception 'La cuenta expositora debe tener el rol encargado autorizado.'; end if;
 for fila in select * from (values
('f8086ec1-1c12-4901-abc3-a8cb9c69de81'::uuid,2,'{"id": "e1200000-0000-4000-8000-000000000001", "tipo": "PERDIDO", "categoria": "Celular", "descripcion": "Celular Samsung negro perdido después de la práctica.", "zona": "Lab-A", "referencia": "Segunda fila de mesas", "caracteristicas": {"marca": "Samsung", "modelo": "Galaxy A54", "color": "Negro"}, "areaEntrega": null, "estado": "ACTIVO", "version": 1, "particulares": {"senal": "rayón diagonal esquina superior", "accesorios": "funda azul con borde blanco", "inscripcion": "estrella pequeña en la funda"}}'::jsonb),
('accced61-6879-47a4-ad01-aa38eaf4f4c5'::uuid,1,'{"id": "e1200000-0000-4000-8000-000000000002", "tipo": "ENCONTRADO", "categoria": "Celular", "descripcion": "Celular negro encontrado sobre una mesa del laboratorio.", "zona": "Lab-A", "referencia": "Mesa cercana a la ventana", "caracteristicas": {"marca": "Samsung", "modelo": "Galaxy A54", "color": "Negro"}, "areaEntrega": "Encargado de Lab-A", "estado": "ACTIVO", "version": 1, "particulares": {"senal": "rayón diagonal esquina superior", "accesorios": "funda azul con borde blanco", "inscripcion": "estrella pequeña en la funda"}, "fotoRuta": "accced61-6879-47a4-ad01-aa38eaf4f4c5/e1200000-0000-4000-8000-000000000002/d7506e2e9d3096a0480e0d54c1a477f081d95b644a8cc2f64b343341ec1a66a8.jpg"}'::jsonb),
('f8086ec1-1c12-4901-abc3-a8cb9c69de81'::uuid,3,'{"id": "e1200000-0000-4000-8000-000000000003", "tipo": "PERDIDO", "categoria": "Mochila", "descripcion": "Mochila gris extraviada al terminar las clases.", "zona": "Lab-B", "referencia": "Al lado de la última mesa", "caracteristicas": {"marca": "Adidas", "modelo": "Urbana", "color": "Gris"}, "areaEntrega": null, "estado": "ACTIVO", "version": 1, "particulares": {"senal": "costura reparada en bolsillo izquierdo", "accesorios": "cinta roja amarrada al cierre", "inscripcion": "iniciales RP en etiqueta interior"}}'::jsonb),
('accced61-6879-47a4-ad01-aa38eaf4f4c5'::uuid,2,'{"id": "e1200000-0000-4000-8000-000000000004", "tipo": "ENCONTRADO", "categoria": "Mochila", "descripcion": "Mochila gris hallada junto a una silla.", "zona": "Lab-B", "referencia": "Silla próxima a la puerta", "caracteristicas": {"marca": "Adidas", "modelo": "Urbana", "color": "Gris"}, "areaEntrega": "Encargado de Lab-B", "estado": "ACTIVO", "version": 1, "particulares": {"senal": "costura reparada en bolsillo izquierdo", "accesorios": "cinta roja amarrada al cierre", "inscripcion": "iniciales RP en etiqueta interior"}, "fotoRuta": "accced61-6879-47a4-ad01-aa38eaf4f4c5/e1200000-0000-4000-8000-000000000004/b95e0f382cb1a03dcc0bcbf82745c40a98486ad178f8a22ca7272b3f102c5e7f.jpg"}'::jsonb),
('f8086ec1-1c12-4901-abc3-a8cb9c69de81'::uuid,2,'{"id": "e1200000-0000-4000-8000-000000000005", "tipo": "PERDIDO", "categoria": "Cuaderno", "descripcion": "Cuaderno azul de apuntes perdido durante una práctica.", "zona": "Lab-C", "referencia": "Primera fila", "caracteristicas": {"marca": "Stanford", "modelo": "A4", "color": "Azul"}, "areaEntrega": null, "estado": "ACTIVO", "version": 1, "particulares": {"senal": "esquina inferior derecha doblada", "accesorios": "separador amarillo", "inscripcion": "estrella dorada en portada"}}'::jsonb),
('accced61-6879-47a4-ad01-aa38eaf4f4c5'::uuid,1,'{"id": "e1200000-0000-4000-8000-000000000006", "tipo": "ENCONTRADO", "categoria": "Cuaderno", "descripcion": "Cuaderno azul recogido en una mesa del laboratorio.", "zona": "Lab-C", "referencia": "Mesa del pasillo central", "caracteristicas": {"marca": "Stanford", "modelo": "A4", "color": "Azul"}, "areaEntrega": "Encargado de Lab-C", "estado": "ACTIVO", "version": 1, "particulares": {"senal": "esquina inferior derecha doblada", "accesorios": "separador amarillo", "inscripcion": "estrella dorada en portada"}, "fotoRuta": "accced61-6879-47a4-ad01-aa38eaf4f4c5/e1200000-0000-4000-8000-000000000006/ba049c28349ec06a14e6b2bb9f9e5e64e5d23b2f7dd0feefdcb95e1f563b4165.jpg"}'::jsonb),
('f8086ec1-1c12-4901-abc3-a8cb9c69de81'::uuid,1,'{"id": "e1200000-0000-4000-8000-000000000007", "tipo": "PERDIDO", "categoria": "Audífonos", "descripcion": "Audífonos blancos perdidos al guardar mis materiales.", "zona": "Lab-E", "referencia": "Zona de ingreso", "caracteristicas": {"marca": "Sony", "modelo": "WI-C100", "color": "Blanco"}, "areaEntrega": null, "estado": "ACTIVO", "version": 1, "particulares": {"senal": "marca roja junto al botón derecho", "accesorios": "bolsa de tela verde", "inscripcion": "inicial R bajo el conector"}}'::jsonb),
('f8086ec1-1c12-4901-abc3-a8cb9c69de81'::uuid,1,'{"id": "e1200000-0000-4000-8000-000000000008", "tipo": "ENCONTRADO", "categoria": "Llaves", "descripcion": "Juego de tres llaves recogido cerca de la entrada.", "zona": "Lab-D", "referencia": "Junto a la puerta", "caracteristicas": {"marca": "", "modelo": "", "color": "Dorado"}, "areaEntrega": "Encargado de Lab-D", "estado": "ACTIVO", "version": 1, "particulares": {"senal": "una llave con cabeza triangular", "accesorios": "argolla plateada con cinta verde", "inscripcion": "número 27 en llave pequeña"}, "fotoRuta": "f8086ec1-1c12-4901-abc3-a8cb9c69de81/e1200000-0000-4000-8000-000000000008/5c630ac072f70c9b46a3c3f07451ebb7c401d6298da3b06239f2386c5fbb60ae.jpg"}'::jsonb),
('accced61-6879-47a4-ad01-aa38eaf4f4c5'::uuid,1,'{"id": "e1200000-0000-4000-8000-000000000009", "tipo": "ENCONTRADO", "categoria": "Celular", "descripcion": "Otro celular Samsung negro encontrado en el laboratorio.", "zona": "Lab-A", "referencia": "Mesa junto a la puerta", "caracteristicas": {"marca": "Samsung", "modelo": "Galaxy A54", "color": "Negro"}, "areaEntrega": "Encargado de Lab-A", "estado": "ACTIVO", "version": 1, "particulares": {"senal": "pegatina circular roja en tapa", "accesorios": "funda transparente sin borde", "inscripcion": "círculo blanco dibujado"}, "fotoRuta": "accced61-6879-47a4-ad01-aa38eaf4f4c5/e1200000-0000-4000-8000-000000000009/4af34ee1c2cea3fb5e08d0e101b380d99a50ab2b7b31f269b7ddf7e886bef74e.jpg"}'::jsonb),
('accced61-6879-47a4-ad01-aa38eaf4f4c5'::uuid,2,'{"id": "e1200000-0000-4000-8000-000000000010", "tipo": "PERDIDO", "categoria": "Llaves", "descripcion": "Perdí tres llaves al salir del laboratorio.", "zona": "Lab-D", "referencia": "Cerca de la entrada", "caracteristicas": {"marca": "", "modelo": "", "color": "Dorado"}, "areaEntrega": null, "estado": "ACTIVO", "version": 1, "particulares": {"senal": "una llave con cabeza triangular", "accesorios": "argolla plateada con cinta verde", "inscripcion": "número 27 en llave pequeña"}}'::jsonb)
 ) as escenario(cuenta,dias,payload) loop
  datos:=fila.payload||jsonb_build_object('fechaEvento',coalesce((select fecha_evento from public.reportes where id=(fila.payload->>'id')::uuid),(now() at time zone 'America/Lima')::date-fila.dias)::text);
  perform set_config('request.jwt.claim.sub',fila.cuenta::text,true);
  perform public.guardar_reporte(datos);
 end loop;
 -- Construye candidatos sin exponer las características privadas de otros usuarios.
 perform set_config('request.jwt.claim.sub',expositor::text,true);
 for fila in select id from public.reportes where autor_id=expositor and tipo='PERDIDO' and estado='ACTIVO' and id::text like 'e1200000-%' loop
  perform public.buscar_coincidencias(fila.id);
 end loop;
 perform set_config('request.jwt.claim.sub',apoyo::text,true);
 if exists(select 1 from public.reportes where id='e1200000-0000-4000-8000-000000000010' and estado='ACTIVO') then
  perform public.buscar_coincidencias('e1200000-0000-4000-8000-000000000010');
 end if;
 -- Una solicitud pendiente de otra persona permite mostrar la revisión sin cambiar de cuenta.
 select * into strict c from public.coincidencias where perdida_id='e1200000-0000-4000-8000-000000000010' and hallazgo_id='e1200000-0000-4000-8000-000000000008';
 perform public.solicitar_recuperacion(jsonb_build_object('id','e1210000-0000-4000-8000-000000000003','coincidenciaId',c.id,'explicacion','Puedo describir la llave triangular, la cinta verde y el número grabado.'));
 perform set_config('request.jwt.claim.sub',expositor::text,true);
 select * into strict c from public.coincidencias where perdida_id='e1200000-0000-4000-8000-000000000003' and hallazgo_id='e1200000-0000-4000-8000-000000000004';
 perform public.solicitar_recuperacion(jsonb_build_object('id','e1210000-0000-4000-8000-000000000001','coincidenciaId',c.id,'explicacion','Mi mochila tiene una costura reparada, cinta roja y mis iniciales en la etiqueta.'));
 select * into strict c from public.coincidencias where perdida_id='e1200000-0000-4000-8000-000000000005' and hallazgo_id='e1200000-0000-4000-8000-000000000006';
 s:=public.solicitar_recuperacion(jsonb_build_object('id','e1210000-0000-4000-8000-000000000002','coincidenciaId',c.id,'explicacion','El cuaderno tiene una esquina doblada, un separador amarillo y una estrella dorada.'));
 if s.estado='PENDIENTE' then
  perform public.recibir_hallazgo('e1200000-0000-4000-8000-000000000006','Encargado de Lab-C');
  perform public.resolver_solicitud(s.id,'APROBADA',true);
 end if;
 -- El celular queda libre para crear la solicitud durante la exposición; no se simula su entrega.
end; $$;
commit;
