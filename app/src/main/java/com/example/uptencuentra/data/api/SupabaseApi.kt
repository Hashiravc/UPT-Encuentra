package com.example.uptencuentra.data.api

import com.example.uptencuentra.data.model.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface SupabaseApi {
    @POST("auth/v1/token?grant_type=password") suspend fun iniciarSesion(@Body datos: AccesoDto): SesionDto
    @POST("auth/v1/signup") suspend fun registrar(@Body datos: RegistroDto): RegistroRespuestaDto
    @POST("auth/v1/token?grant_type=refresh_token") suspend fun renovarSesion(@Body datos: RenovarSesionDto): SesionDto
    @POST("auth/v1/logout?scope=local") suspend fun cerrarSesion(@Header("Authorization") token: String)
    @GET("rest/v1/perfiles") suspend fun obtenerPerfil(@Header("Authorization") token: String,
        @Query("id") id: String, @Query("select") campos: String = "id,nombre,correo,rol"): List<PerfilDto>
    @POST("rest/v1/rpc/guardar_reporte") suspend fun guardarReporte(@Header("Authorization") token: String,
        @Body datos: GuardarReporteRequest): ReporteRemotoDto
    @POST("rest/v1/rpc/mis_reportes") suspend fun obtenerReportes(@Header("Authorization") token: String,
        @Body pagina: PaginaPropia): List<ReporteRemotoDto>
    @POST("storage/v1/object/fotos-reportes/{ruta}") suspend fun subirFoto(@Header("Authorization") token: String,
        @Path(value = "ruta", encoded = true) ruta: String, @Body foto: RequestBody): ResponseBody
    @Streaming @GET("storage/v1/object/authenticated/fotos-reportes/{ruta}")
    suspend fun descargarFoto(@Header("Authorization") token: String,
        @Path(value = "ruta", encoded = true) ruta: String): ResponseBody
    @POST("rest/v1/rpc/buscar_coincidencias") suspend fun buscarCoincidencias(@Header("Authorization") token: String,
        @Body datos: BuscarCoincidenciasRequest): List<CoincidenciaDto>
    @Headers("Accept: application/vnd.pgrst.object+json")
    @POST("rest/v1/rpc/solicitar_recuperacion") suspend fun solicitar(@Header("Authorization") token: String,
        @Body datos: SolicitudRequest): SolicitudDto
    @GET("rest/v1/solicitudes_recuperacion") suspend fun solicitudes(@Header("Authorization") token: String,
        @Query("solicitante_id") cuenta: String? = null, @Query("select") campos: String = "*",
        @Query("order") orden: String = "creado_en.desc", @Query("limit") limite: Int = 200,
        @Query("offset") inicio: Int = 0): List<SolicitudDto>
    @POST("rest/v1/rpc/solicitudes_encargado") suspend fun solicitudesEncargado(@Header("Authorization") token: String,
        @Body vacio: Map<String,String> = emptyMap()): List<SolicitudDto>
    @POST("rest/v1/rpc/hallazgos_encargado") suspend fun hallazgosEncargado(@Header("Authorization") token: String,
        @Body vacio: Map<String,String> = emptyMap()): List<ReporteRemotoDto>
    @POST("rest/v1/rpc/recibir_hallazgo") suspend fun recibir(@Header("Authorization") token: String,
        @Body datos: RecepcionRequest): ReporteRemotoDto
    @Headers("Accept: application/vnd.pgrst.object+json")
    @POST("rest/v1/rpc/resolver_solicitud") suspend fun resolver(@Header("Authorization") token: String,
        @Body datos: ResolverRequest): SolicitudDto
}
