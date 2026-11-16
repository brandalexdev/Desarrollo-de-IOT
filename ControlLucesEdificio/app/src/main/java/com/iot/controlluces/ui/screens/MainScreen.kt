package com.iot.controlluces.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LightbulbOutline
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iot.controlluces.model.EventoHistorial
import com.iot.controlluces.model.ModoControl
import com.iot.controlluces.model.Piso
import com.iot.controlluces.model.ResumenEdificio
import com.iot.controlluces.model.SectorLuz
import com.iot.controlluces.ui.theme.AmberLightOn
import com.iot.controlluces.ui.theme.CoralDanger
import com.iot.controlluces.ui.theme.CyanAuto
import com.iot.controlluces.ui.theme.EmeraldActive
import com.iot.controlluces.ui.theme.IndigoManual
import com.iot.controlluces.ui.theme.SlateCard
import com.iot.controlluces.ui.theme.SlateDark
import com.iot.controlluces.ui.theme.SlateSurface
import com.iot.controlluces.viewmodel.LucesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: LucesViewModel) {
    val state by viewModel.uiState.collectAsState()

    val usuarioActivo = state.usuarioActual
    if (usuarioActivo == null) {
        LoginSeguridadScreen(
            errorLogin = state.errorLogin,
            onLogin = { user, pass -> viewModel.iniciarSesion(user, pass) }
        )
        return
    }

    var tabSeleccionado by remember { mutableIntStateOf(0) }
    var mostrarDialogNuevoPiso by remember { mutableStateOf(false) }
    var mostrarDialogNuevoSector by remember { mutableStateOf(false) }
    var mostrarDialogReloj by remember { mutableStateOf(false) }
    var sectorEditandoAuto by remember { mutableStateOf<SectorLuz?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Control de Luces Edificio IoT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${usuarioActivo.rol.titulo} (${usuarioActivo.username}) • Reloj: ${state.horaEfectiva}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAuto
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { mostrarDialogReloj = true },
                        label = { Text(state.horaEfectiva) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Simular Reloj/Ambiente",
                                tint = AmberLightOn,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    IconButton(onClick = { viewModel.cerrarSesion() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = CoralDanger
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SlateCard) {
                NavigationBarItem(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    icon = { Icon(Icons.Default.Layers, contentDescription = null) },
                    label = { Text("Pisos", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    icon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                    label = { Text("Edificio", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = tabSeleccionado == 2,
                    onClick = { tabSeleccionado = 2 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.resumen.alertasActivas > 0) {
                                    Badge(containerColor = CoralDanger) {
                                        Text("${state.resumen.alertasActivas}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                        }
                    },
                    label = { Text("Seguridad", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = tabSeleccionado == 3,
                    onClick = { tabSeleccionado = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Bitácora", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SlateDark)
        ) {
            if (state.configSeguridad.emergenciaEvacuacionActiva || state.configSeguridad.modoCentinelaActivo || state.configSeguridad.bloqueoControlesManuales) {
                Surface(
                    color = if (state.configSeguridad.emergenciaEvacuacionActiva) CoralDanger.copy(alpha = 0.25f) else CyanAuto.copy(alpha = 0.18f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { tabSeleccionado = 2 }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (state.configSeguridad.emergenciaEvacuacionActiva) Icons.Default.Warning else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (state.configSeguridad.emergenciaEvacuacionActiva) CoralDanger else CyanAuto,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val textoSeg = buildList {
                                if (state.configSeguridad.emergenciaEvacuacionActiva) add("EMERGENCIA 100% ACTIVA")
                                if (state.configSeguridad.modoCentinelaActivo) add("Centinela PIR Activo")
                                if (state.configSeguridad.bloqueoControlesManuales) add("Controles Bloqueados (PIN)")
                            }.joinToString(" • ")
                            Text(
                                text = textoSeg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text("Ir a Seguridad", fontSize = 11.sp, color = AmberLightOn)
                    }
                }
            }

            ResumenEdificioBanner(
                resumen = state.resumen,
                onAbrirSimulador = { mostrarDialogReloj = true }
            )

            when (tabSeleccionado) {
                0 -> TabControlPorPisoYSector(
                    pisos = state.pisos,
                    pisoSeleccionado = state.pisoSeleccionado,
                    sectores = state.sectoresPisoActual,
                    onSeleccionarPiso = { viewModel.seleccionarPiso(it) },
                    onNuevoPisoClick = { mostrarDialogNuevoPiso = true },
                    onEliminarPisoClick = { viewModel.eliminarPisoActual() },
                    onNuevoSectorClick = { mostrarDialogNuevoSector = true },
                    onEncenderPiso = { viewModel.controlarTodoElPisoActual(true) },
                    onApagarPiso = { viewModel.controlarTodoElPisoActual(false) },
                    onModoAutoPiso = { viewModel.cambiarModoTodoElPisoActual(ModoControl.AUTOMATICO) },
                    onModoManualPiso = { viewModel.cambiarModoTodoElPisoActual(ModoControl.MANUAL) },
                    onAlternarLuzManual = { sector, encender ->
                        viewModel.alternarLuzManual(sector, encender)
                    },
                    onCambiarIntensidad = { sector, intensidad ->
                        viewModel.cambiarIntensidad(sector, intensidad)
                    },
                    onCambiarModoSector = { sector, modo ->
                        viewModel.cambiarModoSector(sector, modo)
                    },
                    onConfigurarAutoClick = { sector ->
                        sectorEditandoAuto = sector
                    },
                    onActualizarSensores = { sector, mov, lux ->
                        viewModel.actualizarSensoresSimulados(sector, mov, lux)
                    },
                    onEliminarSector = { sector ->
                        viewModel.eliminarSector(sector)
                    }
                )

                1 -> TabMapaEdificioCompleto(
                    pisos = state.pisos,
                    todosLosSectores = state.todosLosSectores,
                    onControlMaestro = { encender, modo ->
                        viewModel.controlarEdificioCompleto(encender, modo)
                    },
                    onControlarPiso = { piso, encender ->
                        viewModel.controlarPisoEspecifico(piso, encender)
                    },
                    onModoPiso = { piso, modo ->
                        viewModel.cambiarModoPisoEspecifico(piso, modo)
                    },
                    onAlternarSector = { sector, encender ->
                        viewModel.alternarLuzManual(sector, encender)
                    },
                    onIrAPiso = { piso ->
                        viewModel.seleccionarPiso(piso)
                        tabSeleccionado = 0
                    }
                )

                2 -> TabSeguridadEdificio(
                    usuarioActual = usuarioActivo,
                    configSeguridad = state.configSeguridad,
                    alertas = state.alertasSeguridad,
                    usuarios = state.usuariosRegistrados,
                    validarPin = { pin -> viewModel.validarPinSeguridad(pin) },
                    onAlternarCentinela = { viewModel.alternarModoCentinela(it) },
                    onAlternarEmergencia = { viewModel.alternarProtocoloEmergencia(it) },
                    onAlternarSimulacion = { viewModel.alternarSimulacionPresencia(it) },
                    onAlternarBloqueoControles = { viewModel.alternarBloqueoControles(it) },
                    onAtenderAlerta = { viewModel.atenderAlertaSeguridad(it) },
                    onAtenderTodasAlertas = { viewModel.atenderTodasLasAlertas() },
                    onCrearUsuario = { u, p, n, r, pin -> viewModel.registrarNuevoUsuario(u, p, n, r, pin) },
                    onEliminarUsuario = { viewModel.eliminarUsuario(it) }
                )

                3 -> TabHistorialSQLite(
                    historial = state.historial,
                    onLimpiar = { viewModel.limpiarHistorialEventos() }
                )
            }
        }
    }

    if (state.avisoSeguridad != null) {
        AlertDialog(
            onDismissRequest = { viewModel.descartarAvisoSeguridad() },
            title = { Text("Control de Seguridad", fontWeight = FontWeight.Bold) },
            text = { Text(state.avisoSeguridad!!) },
            confirmButton = {
                Button(onClick = { viewModel.descartarAvisoSeguridad() }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (mostrarDialogNuevoPiso) {
        val siguienteNum = (state.pisos.maxOfOrNull { it.numero } ?: 0) + 1
        DialogAgregarPiso(
            siguienteNumeroSugerido = siguienteNum,
            onDismiss = { mostrarDialogNuevoPiso = false },
            onConfirmar = { num, nombre, desc ->
                viewModel.crearNuevoPiso(num, nombre, desc)
                mostrarDialogNuevoPiso = false
            }
        )
    }

    if (mostrarDialogNuevoSector && state.pisoSeleccionado != null) {
        DialogAgregarSector(
            nombrePiso = state.pisoSeleccionado!!.nombre,
            onDismiss = { mostrarDialogNuevoSector = false },
            onConfirmar = { nombre, tipo, watts, modo ->
                viewModel.crearNuevoSector(nombre, tipo, watts, modo)
                mostrarDialogNuevoSector = false
            }
        )
    }

    if (sectorEditandoAuto != null) {
        val sector = sectorEditandoAuto!!
        DialogConfigAutomatica(
            sector = sector,
            onDismiss = { sectorEditandoAuto = null },
            onGuardar = { hOn, hOff, actHor, actMov, actLuz, umbral ->
                viewModel.guardarConfiguracionAutomatica(
                    sector = sector,
                    horaEncendido = hOn,
                    horaApagado = hOff,
                    activarHorario = actHor,
                    activarMovimiento = actMov,
                    activarLuz = actLuz,
                    umbralLux = umbral
                )
                sectorEditandoAuto = null
            }
        )
    }

    if (mostrarDialogReloj) {
        DialogRelojSimulador(
            usarHoraSimulada = state.usarHoraSimulada,
            horaSimuladaActual = state.horaSimulada,
            horaSistema = state.horaActualSistema,
            onDismiss = { mostrarDialogReloj = false },
            onAplicarReloj = { usarSim, hora ->
                viewModel.configurarRelojSimulado(usarSim, hora)
            },
            onSimularAmbiente = { esNoche ->
                viewModel.simularAmbienteDiaNoche(esNoche)
            }
        )
    }
}

@Composable
fun ResumenEdificioBanner(
    resumen: ResumenEdificio,
    onAbrirSimulador: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricItem(
                titulo = "Luces ON",
                valor = "${resumen.sectoresEncendidos}/${resumen.totalSectores}",
                color = AmberLightOn,
                icono = { Icon(Icons.Default.Lightbulb, null, tint = AmberLightOn, modifier = Modifier.size(18.dp)) }
            )
            MetricItem(
                titulo = "Modo Auto",
                valor = "${resumen.sectoresAutomaticos} sect.",
                color = CyanAuto,
                icono = { Icon(Icons.Default.AutoMode, null, tint = CyanAuto, modifier = Modifier.size(18.dp)) }
            )
            MetricItem(
                titulo = "Consumo",
                valor = "${resumen.consumoTotalWatts} W",
                color = EmeraldActive,
                icono = { Icon(Icons.Default.Bolt, null, tint = EmeraldActive, modifier = Modifier.size(18.dp)) }
            )
            OutlinedButton(
                onClick = onAbrirSimulador,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ambiente", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MetricItem(
    titulo: String,
    valor: String,
    color: Color,
    icono: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icono()
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = valor, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
            Text(text = titulo, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
        }
    }
}

@Composable
fun TabControlPorPisoYSector(
    pisos: List<Piso>,
    pisoSeleccionado: Piso?,
    sectores: List<SectorLuz>,
    onSeleccionarPiso: (Piso) -> Unit,
    onNuevoPisoClick: () -> Unit,
    onEliminarPisoClick: () -> Unit,
    onNuevoSectorClick: () -> Unit,
    onEncenderPiso: () -> Unit,
    onApagarPiso: () -> Unit,
    onModoAutoPiso: () -> Unit,
    onModoManualPiso: () -> Unit,
    onAlternarLuzManual: (SectorLuz, Boolean) -> Unit,
    onCambiarIntensidad: (SectorLuz, Int) -> Unit,
    onCambiarModoSector: (SectorLuz, ModoControl) -> Unit,
    onConfigurarAutoClick: (SectorLuz) -> Unit,
    onActualizarSensores: (SectorLuz, Boolean, Int) -> Unit,
    onEliminarSector: (SectorLuz) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pisos.forEach { piso ->
                val seleccionado = piso.id == pisoSeleccionado?.id
                FilterChip(
                    selected = seleccionado,
                    onClick = { onSeleccionarPiso(piso) },
                    label = {
                        Text(
                            text = "Piso ${piso.numero}",
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberLightOn,
                        selectedLabelColor = SlateDark
                    )
                )
            }
            AssistChip(
                onClick = onNuevoPisoClick,
                label = { Text("+ Piso") },
                leadingIcon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (pisoSeleccionado != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PanelControlPisoActual(
                        piso = pisoSeleccionado,
                        sectores = sectores,
                        puedeEliminarPiso = pisos.size > 1,
                        onEncenderPiso = onEncenderPiso,
                        onApagarPiso = onApagarPiso,
                        onModoAutoPiso = onModoAutoPiso,
                        onModoManualPiso = onModoManualPiso,
                        onNuevoSectorClick = onNuevoSectorClick,
                        onEliminarPisoClick = onEliminarPisoClick
                    )
                }

                items(sectores, key = { it.id }) { sector ->
                    SectorLuzCard(
                        sector = sector,
                        onAlternarLuzManual = { encender -> onAlternarLuzManual(sector, encender) },
                        onCambiarIntensidad = { int -> onCambiarIntensidad(sector, int) },
                        onCambiarModo = { modo -> onCambiarModoSector(sector, modo) },
                        onConfigurarAuto = { onConfigurarAutoClick(sector) },
                        onActualizarSensores = { mov, lux -> onActualizarSensores(sector, mov, lux) },
                        onEliminar = { onEliminarSector(sector) }
                    )
                }
            }
        }
    }
}

@Composable
fun PanelControlPisoActual(
    piso: Piso,
    sectores: List<SectorLuz>,
    puedeEliminarPiso: Boolean,
    onEncenderPiso: () -> Unit,
    onApagarPiso: () -> Unit,
    onModoAutoPiso: () -> Unit,
    onModoManualPiso: () -> Unit,
    onNuevoSectorClick: () -> Unit,
    onEliminarPisoClick: () -> Unit
) {
    val encendidos = sectores.count { it.encendida }
    val wattsPiso = sectores.sumOf { it.consumoActualWatts }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = piso.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${piso.descripcion} • $encendidos/${sectores.size} activas ($wattsPiso W)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
                Row {
                    OutlinedButton(
                        onClick = onNuevoSectorClick,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sector", fontSize = 12.sp)
                    }
                    if (puedeEliminarPiso) {
                        IconButton(onClick = onEliminarPisoClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Piso", tint = CoralDanger)
                        }
                    }
                }
            }

            Text(
                text = "Acciones Masivas del Piso:",
                style = MaterialTheme.typography.labelMedium,
                color = AmberLightOn
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onEncenderPiso,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberLightOn, contentColor = SlateDark),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prender Piso", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onApagarPiso,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateDark, contentColor = Color.White),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apagar Piso", fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onModoAutoPiso,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.AutoMode, null, tint = CyanAuto, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Piso en Automático", fontSize = 12.sp, color = CyanAuto)
                }
                OutlinedButton(
                    onClick = onModoManualPiso,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.TouchApp, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Piso en Manual", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SectorLuzCard(
    sector: SectorLuz,
    onAlternarLuzManual: (Boolean) -> Unit,
    onCambiarIntensidad: (Int) -> Unit,
    onCambiarModo: (ModoControl) -> Unit,
    onConfigurarAuto: () -> Unit,
    onActualizarSensores: (Boolean, Int) -> Unit,
    onEliminar: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            sector.encendida && sector.modoControl == ModoControl.AUTOMATICO -> CyanAuto
            sector.encendida -> AmberLightOn
            else -> Color.Transparent
        },
        label = "borderColor"
    )

    var sliderIntensidad by remember(sector.intensidad) { mutableFloatStateOf(sector.intensidad.toFloat()) }
    var sliderLux by remember(sector.luxActual) { mutableFloatStateOf(sector.luxActual.toFloat()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (sector.encendida) AmberLightOn.copy(alpha = 0.2f)
                                else SlateSurface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (sector.encendida) Icons.Default.Lightbulb else Icons.Default.LightbulbOutline,
                            contentDescription = null,
                            tint = if (sector.encendida) AmberLightOn else Color.Gray,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = sector.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${sector.tipoSector} • ${if (sector.encendida) "ENCENDIDA (${sector.consumoActualWatts}W)" else "APAGADA (0W / Nominal ${sector.consumoNominalWatts}W)"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (sector.encendida) AmberLightOn else Color.LightGray
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = sector.encendida,
                        onCheckedChange = { encender -> onAlternarLuzManual(encender) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SlateDark,
                            checkedTrackColor = AmberLightOn
                        )
                    )
                    Text(
                        text = if (sector.encendida) "ON" else "OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (sector.encendida) AmberLightOn else Color.Gray
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = sector.modoControl == ModoControl.MANUAL,
                        onClick = { onCambiarModo(ModoControl.MANUAL) },
                        label = { Text("Modo Manual") },
                        leadingIcon = {
                            Icon(Icons.Default.TouchApp, null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoManual,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = sector.modoControl == ModoControl.AUTOMATICO,
                        onClick = { onCambiarModo(ModoControl.AUTOMATICO) },
                        label = { Text("Modo Automático") },
                        leadingIcon = {
                            Icon(Icons.Default.AutoMode, null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanAuto,
                            selectedLabelColor = SlateDark
                        )
                    )
                }

                Row {
                    IconButton(onClick = onConfigurarAuto) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configurar horario y reglas automáticas",
                            tint = CyanAuto
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar Sector",
                            tint = Color.Gray
                        )
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Intensidad Luminosa (Dimmer): ${sliderIntensidad.toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.LightGray
                    )
                    Text(
                        text = "${((sector.consumoNominalWatts * sliderIntensidad.toInt()) / 100)} W máx",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldActive
                    )
                }
                Slider(
                    value = sliderIntensidad,
                    onValueChange = { sliderIntensidad = it },
                    onValueChangeFinished = {
                        onCambiarIntensidad(sliderIntensidad.toInt())
                    },
                    valueRange = 10f..100f
                )
            }

            HorizontalDivider(color = SlateSurface)

            Surface(
                color = SlateDark.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reglas Auto y Sensores del Sector",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAuto,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Horario: ${sector.horaEncendido} a ${sector.horaApagado}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = if (sector.movimientoDetectado) EmeraldActive else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (sector.movimientoDetectado) "PIR: Presencia Detectada" else "PIR: Sin Movimiento",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (sector.movimientoDetectado) EmeraldActive else Color.LightGray
                            )
                        }
                        Switch(
                            checked = sector.movimientoDetectado,
                            onCheckedChange = { mov ->
                                onActualizarSensores(mov, sector.luxActual)
                            }
                        )
                    }

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = AmberLightOn,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sensor LDR Luz Ambiente: ${sliderLux.toInt()} Lux",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                            Text(
                                text = "Umbral Auto: ≤ ${sector.umbralLux} lx",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (sliderLux.toInt() <= sector.umbralLux) CyanAuto else Color.Gray
                            )
                        }
                        Slider(
                            value = sliderLux,
                            onValueChange = { sliderLux = it },
                            onValueChangeFinished = {
                                onActualizarSensores(sector.movimientoDetectado, sliderLux.toInt())
                            },
                            valueRange = 0f..600f
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabMapaEdificioCompleto(
    pisos: List<Piso>,
    todosLosSectores: List<SectorLuz>,
    onControlMaestro: (encender: Boolean?, modo: ModoControl) -> Unit,
    onControlarPiso: (Piso, Boolean) -> Unit,
    onModoPiso: (Piso, ModoControl) -> Unit,
    onAlternarSector: (SectorLuz, Boolean) -> Unit,
    onIrAPiso: (Piso) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Control Maestro de Todo el Edificio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Opera todos los pisos y sectores registrados en SQLite simultáneamente:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onControlMaestro(true, ModoControl.MANUAL) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberLightOn,
                                contentColor = SlateDark
                            )
                        ) {
                            Text("Prender Todo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onControlMaestro(false, ModoControl.MANUAL) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SlateDark,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Apagar Todo", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onControlMaestro(null, ModoControl.AUTOMATICO) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanAuto,
                                contentColor = SlateDark
                            )
                        ) {
                            Text("Todo Auto IoT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        items(pisos, key = { it.id }) { piso ->
            val sectoresPiso = todosLosSectores.filter { it.pisoId == piso.id }
            val encendidos = sectoresPiso.count { it.encendida }
            val consumoPiso = sectoresPiso.sumOf { it.consumoActualWatts }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onIrAPiso(piso) }
                        ) {
                            Text(
                                text = piso.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "$encendidos/${sectoresPiso.size} sectores encendidos • $consumoPiso W",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberLightOn
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            AssistChip(
                                onClick = { onControlarPiso(piso, true) },
                                label = { Text("ON", fontSize = 11.sp) }
                            )
                            AssistChip(
                                onClick = { onControlarPiso(piso, false) },
                                label = { Text("OFF", fontSize = 11.sp) }
                            )
                            AssistChip(
                                onClick = { onModoPiso(piso, ModoControl.AUTOMATICO) },
                                label = { Text("AUTO", fontSize = 11.sp, color = CyanAuto) }
                            )
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sectoresPiso.forEach { sector ->
                            Surface(
                                color = if (sector.encendida) AmberLightOn.copy(alpha = 0.18f) else SlateSurface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = if (sector.encendida) AmberLightOn else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { onAlternarSector(sector, !sector.encendida) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (sector.encendida) Icons.Default.Lightbulb else Icons.Default.LightbulbOutline,
                                        contentDescription = null,
                                        tint = if (sector.encendida) AmberLightOn else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = sector.nombre,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${sector.modoControl.name} • ${if (sector.encendida) "${sector.intensidad}%" else "OFF"}",
                                            fontSize = 10.sp,
                                            color = if (sector.modoControl == ModoControl.AUTOMATICO) CyanAuto else Color.LightGray
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
}

@Composable
fun TabHistorialSQLite(
    historial: List<EventoHistorial>,
    onLimpiar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bitácora de Auditoría y Eventos SQLite",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tabla: historial_eventos (${historial.size} registros recientes)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
            OutlinedButton(onClick = onLimpiar) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpiar", fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(historial, key = { it.id }) { evento ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${evento.pisoNombre} • ${evento.sectorNombre}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AmberLightOn
                            )
                            Surface(
                                color = when (evento.origen) {
                                    "AUTO IoT" -> CyanAuto.copy(alpha = 0.2f)
                                    "SEGURIDAD", "SEGURIDAD PIR" -> CoralDanger.copy(alpha = 0.25f)
                                    "MANUAL" -> IndigoManual.copy(alpha = 0.25f)
                                    else -> SlateSurface
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${evento.origen} (${evento.usuario})",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    color = when (evento.origen) {
                                        "AUTO IoT" -> CyanAuto
                                        "SEGURIDAD", "SEGURIDAD PIR" -> CoralDanger
                                        else -> Color.White
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = evento.accion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Text(
                            text = evento.fechaHora,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
