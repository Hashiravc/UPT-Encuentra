package com.example.uptencuentra.di

import com.example.uptencuentra.data.api.*
import com.example.uptencuentra.data.repository.SupabaseSesionRepository
import com.example.uptencuentra.domain.repository.SesionRepository
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun api(gson: Gson): SupabaseApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { cadena ->
                val peticion = cadena.request().newBuilder()
                    .header("apikey", ConfiguracionSupabase.clavePublica).build()
                cadena.proceed(peticion)
            }.build()
        return Retrofit.Builder().baseUrl(ConfiguracionSupabase.url)
            .client(cliente).addConverterFactory(GsonConverterFactory.create(gson))
            .build().create(SupabaseApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    abstract fun programar(implementation: com.example.uptencuentra.data.sync.ProgramadorSincronizacion):
        com.example.uptencuentra.domain.service.ProgramarSincronizacion

    @Binds @Singleton
    abstract fun sesion(implementation: SupabaseSesionRepository): SesionRepository
}
