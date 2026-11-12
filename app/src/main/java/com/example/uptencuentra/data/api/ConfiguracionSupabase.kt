package com.example.uptencuentra.data.api

import com.example.uptencuentra.BuildConfig

object ConfiguracionSupabase {
    val url = BuildConfig.SUPABASE_URL
    val clavePublica = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    val disponible get() = url.startsWith("https://") && clavePublica.isNotBlank()
}
