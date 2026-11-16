package com.iot.controlluces.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddModerator
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iot.controlluces.model.AlertaSeguridad
import com.iot.controlluces.model.ConfiguracionSeguridad
import com.iot.controlluces.model.RolUsuario
import com.iot.controlluces.model.UsuarioSeguridad
import com.iot.controlluces.ui.theme.AmberLightOn
import com.iot.controlluces.ui.theme.CoralDanger
import com.iot.controlluces.ui.theme.CyanAuto
import com.iot.controlluces.ui.theme.EmeraldActive
import com.iot.controlluces.ui.theme.IndigoManual
import com.iot.controlluces.ui.theme.SlateCard
import com.iot.controlluces.ui.theme.SlateDark
import com.iot.controlluces.ui.theme.SlateSurface

/**
 * Pantalla de Inicio de Sesión y Control de Acceso Basado en Roles (RBAC) con SQLite + SHA-256.
 */
@Composable
fun LoginSeguridadScreen(
    errorLogin: String?,
    onLogin: (username: String, pass: String) -> Unit
) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin123") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AmberLightOn.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AmberLightOn,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Text(
                    text = "SmartBuilding Luces IoT",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Acceso Seguro • Autenticación SQLite SHA-256",
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAuto
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorLogin != null) {
                    Surface(
                        color = CoralDanger.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorLogin,
                            color = CoralDanger,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Button(
                    onClick = { onLogin(username, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberLightOn,
                        contentColor = SlateDark
                    )
                ) {
                    Icon(Icons.Default.Key, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar Sesión Segura", fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = SlateSurface)

                Text(
                    text = "Perfiles de Prueba Precargados en SQLite (Toca para autocompletar):",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {
                            username = "admin"
                            password = "admin123"
                        },
                        label = { Text("Admin (Total)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = {
                            username = "guardia"
                            password = "guardia123"
                        },
                        label = { Text("Guardia (Operador)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = {
                            username = "auditor"
                            password = "123456"
                        },
                        label = { Text("Auditor (Lectura)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Pestaña dedicada a las Secciones de Seguridad del Edificio:
 * 1. Protocolo de Emergencia / Evacuación (100% Luces + Bloqueo PIN)
 * 2. Modo Centinela / Vigilancia Nocturna por Sensores PIR
 * 3. Simulación de Presencia Antirrobo y Bloqueo de Controles Manuales
 * 4. Centro de Alertas de Seguridad e Intrusión en SQLite
 * 5. Gestión de Usuarios y Roles con contraseñas cifradas SHA-256
 */
@Composable
fun TabSeguridadEdificio(
    usuarioActual: UsuarioSeguridad,
    configSeguridad: ConfiguracionSeguridad,
    alertas: List<AlertaSeguridad>,
    usuarios: List<UsuarioSeguridad>,
    validarPin: (String) -> Boolean,
    onAlternarCentinela: (Boolean) -> Unit,
    onAlternarEmergencia: (Boolean) -> Unit,
    onAlternarSimulacion: (Boolean) -> Unit,
    onAlternarBloqueoControles: (Boolean) -> Unit,
    onAtenderAlerta: (Int) -> Unit,
    onAtenderTodasAlertas: () -> Unit,
    onCrearUsuario: (String, String, String, RolUsuario, String) -> Unit,
    onEliminarUsuario: (UsuarioSeguridad) -> Unit
) {
    var accionPendientePin by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }
    var mostrarDialogNuevoUsuario by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Tarjeta de Perfil y Nivel de Acceso Actual
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyanAuto,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sesión Activa: ${usuarioActual.nombreCompleto} (${usuarioActual.username})",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Rol: ${usuarioActual.rol.titulo} • PIN de Seguridad: ${usuarioActual.pinSeguridad}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberLightOn
                            )
                        }
                    }
                }
            }
        }

        // 2. Protocolos Críticos de Seguridad Lumínica del Edificio
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (configSeguridad.emergenciaEvacuacionActiva) 2.dp else 1.dp,
                        color = if (configSeguridad.emergenciaEvacuacionActiva) CoralDanger else SlateSurface,
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Protocolos de Seguridad y Emergencia IoT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // A) Botón de Emergencia / Evacuación
                    Surface(
                        color = if (configSeguridad.emergenciaEvacuacionActiva) CoralDanger.copy(alpha = 0.22f) else SlateDark,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = CoralDanger,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Emergencia y Evacuación (100% ON)",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Enciende todas las luces del edificio al 100% y bloquea el apagado manual.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                            Button(
                                onClick = {
                                    if (configSeguridad.emergenciaEvacuacionActiva) {
                                        accionPendientePin = "Desactivar Protocolo de Emergencia" to {
                                            onAlternarEmergencia(false)
                                        }
                                    } else {
                                        onAlternarEmergencia(true)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (configSeguridad.emergenciaEvacuacionActiva) EmeraldActive else CoralDanger,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (configSeguridad.emergenciaEvacuacionActiva) "Desactivar (PIN)" else "Activar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // B) Modo Centinela (Vigilancia por Sensores PIR)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Modo Centinela / Alarma por Intrusión PIR",
                                fontWeight = FontWeight.SemiBold,
                                color = CyanAuto
                            )
                            Text(
                                text = "Si un sensor PIR detecta movimiento, enciende la luz al 100% y registra alerta crítica en SQLite.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                        Switch(
                            checked = configSeguridad.modoCentinelaActivo,
                            onCheckedChange = { activar ->
                                if (!activar) {
                                    accionPendientePin = "Desactivar Modo Centinela PIR" to {
                                        onAlternarCentinela(false)
                                    }
                                } else {
                                    onAlternarCentinela(true)
                                }
                            }
                        )
                    }

                    HorizontalDivider(color = SlateSurface)

                    // C) Simulación de Presencia Nocturna (Antirrobo)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Simulación de Presencia Antirrobo",
                                fontWeight = FontWeight.SemiBold,
                                color = AmberLightOn
                            )
                            Text(
                                text = "Ilumina sectores intercalados de acceso y oficinas para simular ocupación del edificio.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                        Switch(
                            checked = configSeguridad.simulacionPresenciaActiva,
                            onCheckedChange = { onAlternarSimulacion(it) }
                        )
                    }

                    HorizontalDivider(color = SlateSurface)

                    // D) Bloqueo de Seguridad del Panel Manual (Requiere PIN)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (configSeguridad.bloqueoControlesManuales) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (configSeguridad.bloqueoControlesManuales) CoralDanger else EmeraldActive,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bloqueo de Controles Manuales con PIN",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Impide que terceros enciendan o apaguen luces sin autorización por PIN.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                        Switch(
                            checked = configSeguridad.bloqueoControlesManuales,
                            onCheckedChange = { bloquear ->
                                if (!bloquear) {
                                    accionPendientePin = "Desbloquear Controles Manuales" to {
                                        onAlternarBloqueoControles(false)
                                    }
                                } else {
                                    onAlternarBloqueoControles(true)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 3. Centro de Alertas de Seguridad e Intrusión (Tabla SQLite `alertas_seguridad`)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = CoralDanger,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Alertas de Seguridad e Intrusión (SQLite)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${configSeguridad.alertasSinAtender} alertas pendientes de revisión",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (configSeguridad.alertasSinAtender > 0) CoralDanger else EmeraldActive
                                )
                            }
                        }

                        if (configSeguridad.alertasSinAtender > 0) {
                            OutlinedButton(
                                onClick = {
                                    accionPendientePin = "Reconocer y Atender Todas las Alertas" to {
                                        onAtenderTodasAlertas()
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Atender Todas", fontSize = 11.sp)
                            }
                        }
                    }

                    alertas.take(8).forEach { alerta ->
                        Surface(
                            color = if (!alerta.atendida) CoralDanger.copy(alpha = 0.15f) else SlateSurface.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "[${alerta.nivel}] ${alerta.tipoAlerta} • ${alerta.ubicacion}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (!alerta.atendida) CoralDanger else AmberLightOn
                                    )
                                    Text(
                                        text = alerta.descripcion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                    Text(
                                        text = alerta.fechaHora,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                if (!alerta.atendida) {
                                    AssistChip(
                                        onClick = { onAtenderAlerta(alerta.id) },
                                        label = { Text("Atender", fontSize = 11.sp) }
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Atendida",
                                        tint = EmeraldActive,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Gestión de Usuarios y Control de Acceso (Tabla SQLite `usuarios` con SHA-256)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Usuarios y Roles en SQLite (SHA-256)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Control de acceso basado en roles (RBAC)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                        if (usuarioActual.rol == RolUsuario.ADMIN) {
                            OutlinedButton(
                                onClick = { mostrarDialogNuevoUsuario = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AddModerator, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Usuario", fontSize = 12.sp)
                            }
                        }
                    }

                    usuarios.forEach { u ->
                        Surface(
                            color = SlateSurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${u.nombreCompleto} (@${u.username})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Rol: ${u.rol.titulo} • Último acceso: ${u.ultimoAcceso}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanAuto
                                    )
                                }
                                if (usuarioActual.rol == RolUsuario.ADMIN && u.username != "admin") {
                                    IconButton(onClick = { onEliminarUsuario(u) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar usuario",
                                            tint = CoralDanger
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (accionPendientePin != null) {
        val (titulo, accion) = accionPendientePin!!
        DialogVerificarPin(
            tituloAccion = titulo,
            validarPin = validarPin,
            onDismiss = { accionPendientePin = null },
            onPinCorrecto = {
                accion()
                accionPendientePin = null
            }
        )
    }

    if (mostrarDialogNuevoUsuario) {
        DialogCrearUsuarioSeguridad(
            onDismiss = { mostrarDialogNuevoUsuario = false },
            onConfirmar = { username, pass, nombre, rol, pin ->
                onCrearUsuario(username, pass, nombre, rol, pin)
                mostrarDialogNuevoUsuario = false
            }
        )
    }
}
