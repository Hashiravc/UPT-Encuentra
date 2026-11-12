package com.example.uptencuentra.di

import android.content.Context
import androidx.room.Room
import com.example.uptencuentra.data.local.AppDatabase
import com.example.uptencuentra.data.repository.OfflineFirstReporteRepository
import com.example.uptencuentra.domain.repository.ReporteRepository
import com.example.uptencuentra.domain.validation.ValidarReporte
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "upt_encuentra.db").addMigrations(com.example.uptencuentra.data.local.Migraciones.VERSION_1_2, com.example.uptencuentra.data.local.Migraciones.VERSION_2_3).build()

    @Provides @Singleton
    fun gson(): Gson = Gson()

    @Provides
    fun validarReporte(): ValidarReporte = ValidarReporte()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun reportes(implementation: OfflineFirstReporteRepository): ReporteRepository
}
