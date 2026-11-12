package com.example.uptencuentra.data.api

object ConfiguracionSupabase {
    // Configuración del cliente; los permisos de cada usuario se validan en Supabase.
    const val url = "https://rngqocatmdoxakqpuzxo.supabase.co/"
    const val clavePublica = "sb_publishable_lAF9qOQ7sV6I4gnAABUXEQ__KxkQ6Oe"
    val disponible get() = url.startsWith("https://") && clavePublica.isNotBlank()
}
