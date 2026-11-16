package com.iot.controlluces.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.iot.controlluces.model.ModoControl
import com.iot.controlluces.model.RolUsuario
import com.iot.controlluces.model.SectorLuz
import com.iot.controlluces.ui.theme.CoralDanger

@Composable
fun DialogAgregarPiso(
    siguienteNumeroSugerido: Int,
    onDismiss: () -> Unit,
    onConfirmar: (numero: Int, nombre: String, descripcion: String) -> Unit
) {
    var numeroStr by remember { mutableStateOf(siguienteNumeroSugerido.toString()) }
    var nombre by remember { mutableStateOf("Piso $siguienteNumeroSugerido - ") }
    var descripcion by remember { mutableStateOf("Sectores generales del piso $siguienteNumeroSugerido") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Piso en SQLite", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = numeroStr,
                    onValueChange = { numeroStr = it },
                    label = { Text("Número de Piso") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Piso") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val num = numeroStr.toIntOrNull() ?: siguienteNumeroSugerido
                    if (nombre.isNotBlank()) {
                        onConfirmar(num, nombre.trim(), descripcion.trim())
                    }
                }
            ) {
                Text("Guardar Piso")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogAgregarSector(
    nombrePiso: String,
    onDismiss: () -> Unit,
    onConfirmar: (nombre: String, tipo: String, watts: Int, modo: ModoControl) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    val tiposDisponibles = listOf("Oficina", "Pasillo", "Acceso", "Reuniones", "Laboratorio", "Exterior/Acceso")
    var tipoSeleccionado by remember { mutableStateOf(tiposDisponibles.first()) }
    var wattsStr by remember { mutableStateOf("60") }
    var modoInicial by remember { mutableStateOf(ModoControl.AUTOMATICO) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Sector Luminoso", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ubicación: $nombrePiso",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Sector (ej. Pasillo Este)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Tipo de Zona:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tiposDisponibles.take(3).forEach { tipo ->
                        FilterChip(
                            selected = tipoSeleccionado == tipo,
                            onClick = { tipoSeleccionado = tipo },
                            label = { Text(tipo) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tiposDisponibles.drop(3).forEach { tipo ->
                        FilterChip(
                            selected = tipoSeleccionado == tipo,
                            onClick = { tipoSeleccionado = tipo },
                            label = { Text(tipo) }
                        )
                    }
                }
                OutlinedTextField(
                    value = wattsStr,
                    onValueChange = { wattsStr = it },
                    label = { Text("Potencia Nominal (Watts)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Iniciar en Modo Automático IoT")
                    Switch(
                        checked = modoInicial == ModoControl.AUTOMATICO,
                        onCheckedChange = {
                            modoInicial = if (it) ModoControl.AUTOMATICO else ModoControl.MANUAL
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val watts = (wattsStr.toIntOrNull() ?: 60).coerceIn(5, 1000)
                    if (nombre.isNotBlank()) {
                        onConfirmar(nombre.trim(), tipoSeleccionado, watts, modoInicial)
                    }
                }
            ) {
                Text("Crear Sector")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogConfigAutomatica(
    sector: SectorLuz,
    onDismiss: () -> Unit,
    onGuardar: (
        horaOn: String,
        horaOff: String,
        actHorario: Boolean,
        actMov: Boolean,
        actLuz: Boolean,
        umbralLux: Int
    ) -> Unit
) {
    var horaEncendido by remember { mutableStateOf(sector.horaEncendido) }
    var horaApagado by remember { mutableStateOf(sector.horaApagado) }
    var activarHorario by remember { mutableStateOf(sector.activarPorHorario) }
    var activarMov by remember { mutableStateOf(sector.activarPorMovimiento) }
    var activarLuz by remember { mutableStateOf(sector.activarPorLuzAmbiente) }
    var umbralLux by remember { mutableFloatStateOf(sector.umbralLux.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reglas Automáticas: ${sector.nombre}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Cuando el sector está en modo AUTOMÁTICO, la luz se encenderá si se cumple cualquiera de las reglas habilitadas:",
                    style = MaterialTheme.typography.bodySmall
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activarHorario, onCheckedChange = { activarHorario = it })
                    Text("Encender por Horario Programado", fontWeight = FontWeight.SemiBold)
                }
                if (activarHorario) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = horaEncendido,
                            onValueChange = { horaEncendido = it },
                            label = { Text("Hora ON (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = horaApagado,
                            onValueChange = { horaApagado = it },
                            label = { Text("Hora OFF (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activarMov, onCheckedChange = { activarMov = it })
                    Text("Encender por Sensor PIR (Presencia)", fontWeight = FontWeight.SemiBold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = activarLuz, onCheckedChange = { activarLuz = it })
                    Text("Encender por Sensor LDR (Poca Luz)", fontWeight = FontWeight.SemiBold)
                }
                if (activarLuz) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            "Umbral de oscuridad: ≤ ${umbralLux.toInt()} Lux",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = umbralLux,
                            onValueChange = { umbralLux = it },
                            valueRange = 20f..500f
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onGuardar(
                        horaEncendido.trim(),
                        horaApagado.trim(),
                        activarHorario,
                        activarMov,
                        activarLuz,
                        umbralLux.toInt()
                    )
                }
            ) {
                Text("Guardar en SQLite")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogRelojSimulador(
    usarHoraSimulada: Boolean,
    horaSimuladaActual: String,
    horaSistema: String,
    onDismiss: () -> Unit,
    onAplicarReloj: (usarSimulado: Boolean, hora: String) -> Unit,
    onSimularAmbiente: (esNoche: Boolean) -> Unit
) {
    var usarSim by remember { mutableStateOf(usarHoraSimulada) }
    var horaInput by remember { mutableStateOf(horaSimuladaActual) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Simulador de Reloj y Ambiente IoT", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Permite probar de inmediato el encendido/apagado automático por horarios y sensores de luz sin esperar a la noche.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Usar Hora Simulada (Real: $horaSistema)")
                    Switch(checked = usarSim, onCheckedChange = { usarSim = it })
                }
                if (usarSim) {
                    OutlinedTextField(
                        value = horaInput,
                        onValueChange = { horaInput = it },
                        label = { Text("Hora Simulada (formato HH:mm, ej. 21:30)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Escenarios Rápidos para todo el Edificio:", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onSimularAmbiente(false)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("☀️ Día (11:00 / 420 lx)")
                    }
                    OutlinedButton(
                        onClick = {
                            onSimularAmbiente(true)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🌙 Noche (21:30 / 35 lx)")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAplicarReloj(usarSim, horaInput.trim())
                    onDismiss()
                }
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun DialogVerificarPin(
    tituloAccion: String,
    validarPin: (String) -> Boolean,
    onDismiss: () -> Unit,
    onPinCorrecto: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Autorización con PIN de Seguridad", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Acción protegida: $tituloAccion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Ingrese el PIN Maestro del edificio (por defecto: 1234) o el PIN de su usuario:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        error = false
                    },
                    label = { Text("PIN de Seguridad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) {
                    Text(
                        text = "PIN de seguridad incorrecto.",
                        color = CoralDanger,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validarPin(pin)) {
                        onPinCorrecto()
                    } else {
                        error = true
                    }
                }
            ) {
                Text("Autorizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DialogCrearUsuarioSeguridad(
    onDismiss: () -> Unit,
    onConfirmar: (username: String, pass: String, nombre: String, rol: RolUsuario, pin: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombreCompleto by remember { mutableStateOf("") }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.OPERADOR) }
    var pin by remember { mutableStateOf("1234") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Usuario en SQLite (SHA-256)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario (ej. guardia2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña (se cifrará con SHA-256)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nombreCompleto,
                    onValueChange = { nombreCompleto = it },
                    label = { Text("Nombre Completo / Cargo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("PIN de Seguridad (4 dígitos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Rol de Seguridad:", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RolUsuario.entries.forEach { rol ->
                        FilterChip(
                            selected = rolSeleccionado == rol,
                            onClick = { rolSeleccionado = rol },
                            label = { Text("${rol.titulo} (${rol.name})") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank() && password.isNotBlank() && nombreCompleto.isNotBlank()) {
                        onConfirmar(username.trim(), password, nombreCompleto.trim(), rolSeleccionado, pin.trim())
                    }
                }
            ) {
                Text("Registrar Usuario")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
