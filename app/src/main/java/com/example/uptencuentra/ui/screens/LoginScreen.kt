package com.example.uptencuentra.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.uptencuentra.R
import com.example.uptencuentra.ui.components.AppScaffold
import com.example.uptencuentra.ui.theme.Dimensions
import com.example.uptencuentra.ui.viewmodel.SessionViewModel

@Composable
fun LoginScreen(viewModel: SessionViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var registro by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var validacion by remember { mutableStateOf(false) }
    AppScaffold(title = if (registro) "Crear cuenta" else "UPT Encuentra") { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState())
            .padding(Dimensions.screenPadding), verticalArrangement = Arrangement.spacedBy(Dimensions.itemSpacing)) {
            Text(if (registro) "Registra tus datos" else "Iniciar sesión", style = MaterialTheme.typography.headlineSmall)
            if (registro) OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") },
                singleLine = true, enabled = !state.procesando, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(correo, { correo = it }, label = { Text("Correo electrónico") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), enabled = !state.procesando,
                modifier = Modifier.fillMaxWidth())
            OutlinedTextField(clave, { clave = it }, label = { Text("Contraseña (mínimo 8 caracteres)") },
                visualTransformation = PasswordVisualTransformation(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !state.procesando,
                modifier = Modifier.fillMaxWidth())
            if (registro) OutlinedTextField(confirmacion, { confirmacion = it }, label = { Text("Repite la contraseña") },
                visualTransformation = PasswordVisualTransformation(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), enabled = !state.procesando,
                modifier = Modifier.fillMaxWidth())
            if (validacion) Text(stringResource(R.string.access_validation), color = MaterialTheme.colorScheme.error)
            if (state.error) Text(stringResource(R.string.access_error), color = MaterialTheme.colorScheme.error)
            Button(enabled = !state.procesando, modifier = Modifier.fillMaxWidth(), onClick = {
                validacion = !android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() ||
                    clave.length < 8 || (registro && (nombre.trim().length !in 1..100 || clave != confirmacion))
                if (!validacion) viewModel.acceder(nombre, correo, clave, registro)
            }) { Text(if (registro) "Registrarme" else "Ingresar") }
            TextButton(enabled = !state.procesando, onClick = { registro = !registro; validacion = false; clave = ""; confirmacion = "" }) {
                Text(if (registro) "Ya tengo una cuenta" else "Crear una cuenta")
            }
            OutlinedButton(onClick = viewModel::entrarDemostracion, enabled = !state.procesando,
                modifier = Modifier.fillMaxWidth()) { Text("Entrar sin internet") }
            Text("El registro y el primer ingreso necesitan internet. La sesión y tus reportes se conservan localmente.")
            if (state.procesando) CircularProgressIndicator()
        }
    }
}
