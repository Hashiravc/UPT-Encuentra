-- Consultas de solo lectura del escenario; no cambian estados ni usuarios.
select 'Reportes totales' as verificacion,count(*)::text as resultado from public.reportes
union all select 'Reportes nuevos de exposición',count(*)::text from public.reportes where id::text like 'e1200000-%'
union all select 'Fotos vinculadas y disponibles',count(*)::text from public.reporte_detalles d join storage.objects o on o.name=d.foto_ruta and o.bucket_id='fotos-reportes' where d.reporte_id::text like 'e1200000-%'
union all select 'Candidatos de mi cuenta',count(*)::text from public.coincidencias where solicitante_id='f8086ec1-1c12-4901-abc3-a8cb9c69de81' and perdida_id::text like 'e1200000-%' and activa
union all select 'Solicitudes del escenario',count(*)::text from public.solicitudes_recuperacion where id::text like 'e1210000-%'
union all select 'Recepciones listas',count(*)::text from public.custodias where reporte_id::text like 'e1200000-%';
