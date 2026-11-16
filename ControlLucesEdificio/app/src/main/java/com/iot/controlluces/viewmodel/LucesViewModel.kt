package com.iot.controlluces.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iot.controlluces.data.DatabaseHelper
import com.iot.controlluces.model.AlertaSeguridad
import com.iot.controlluces.model.ConfiguracionSeguridad
import com.iot.controlluces.model.EventoHistorial
import com.iot.controlluces.model.ModoControl
import com.iot.controlluces.model.Piso
import com.iot.controlluces.model.ResumenEdificio
import com.iot.controlluces.model.RolUsuario
import com.iot.controlluces.model.SectorLuz
import com.iot.controlluces.model.UsuarioSeguridad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LucesUiState(
    val usuarioActual: UsuarioSeguridad? = null,
    val errorLogin: String? = null,
    val intentosFallidosLogin: Int = 0,
    val pisos: List<Piso> = emptyList(),
    val pisoSeleccionado: Piso? = null,
    val sectoresPisoActual: List<SectorLuz> = emptyList(),
    val todosLosSectores: List<SectorLuz> = emptyList(),
    val historial: List<EventoHistorial> = emptyList(),
    val resumen: ResumenEdificio = ResumenEdificio(),
    val configSeguridad: ConfiguracionSeguridad = ConfiguracionSeguridad(),
    val alertasSeguridad: List<AlertaSeguridad> = emptyList(),
    val usuariosRegistrados: List<UsuarioSeguridad> = emptyList(),
    val usarHoraSimulada: Boolean = false,
    val horaSimulada: String = "21:30",
    val horaActualSistema: String = "12:00",
    val avisoSeguridad: String? = null
) {
    val horaEfectiva: String
        get() = if (usarHoraSimulada) horaSimulada else horaActualSistema

    val esAdmin: Boolean
        get() = usuarioActual?.rol == RolUsuario.ADMIN

    val esSoloLectura: Boolean
        get() = usuarioActual?.rol == RolUsuario.AUDITOR
}

class LucesViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DatabaseHelper(application.applicationContext)

    private val _uiState = MutableStateFlow(LucesUiState())
    val uiState: StateFlow<LucesUiState> = _uiState.asStateFlow()

    init {
        recargarDatos()
        iniciarMotorAutomaticoIoT()
    }

    private val nombreUsuarioActivo: String
        get() = _uiState.value.usuarioActual?.username ?: "Sistema"

    private fun verificarPermisoOperacion(requiereAdmin: Boolean = false): Boolean {
        val state = _uiState.value
        val user = state.usuarioActual ?: return false
        if (user.rol == RolUsuario.AUDITOR) {
            _uiState.value = state.copy(
                avisoSeguridad = "Rol '${user.rol.titulo}': Modo de solo lectura. No tiene permisos para modificar luces."
            )
            return false
        }
        if (requiereAdmin && user.rol != RolUsuario.ADMIN) {
            _uiState.value = state.copy(
                avisoSeguridad = "Acción restringida: Se requiere rol de Administrador."
            )
            return false
        }
        if (state.configSeguridad.emergenciaEvacuacionActiva) {
            _uiState.value = state.copy(
                avisoSeguridad = "Protocolo de Emergencia Activo: Desactive la emergencia en la pestaña Seguridad con su PIN antes de apagar luces."
            )
            return false
        }
        if (state.configSeguridad.bloqueoControlesManuales) {
            _uiState.value = state.copy(
                avisoSeguridad = "Panel Bloqueado por Seguridad: Desbloquee los controles en la sección Seguridad usando el PIN."
            )
            return false
        }
        return true
    }

    fun descartarAvisoSeguridad() {
        _uiState.value = _uiState.value.copy(avisoSeguridad = null)
    }

    fun iniciarSesion(username: String, passwordPlano: String) {
        viewModelScope.launch {
            val usuario = withContext(Dispatchers.IO) {
                dbHelper.autenticarUsuario(username, passwordPlano)
            }
            if (usuario != null) {
                _uiState.value = _uiState.value.copy(
                    usuarioActual = usuario,
                    errorLogin = null,
                    intentosFallidosLogin = 0
                )
                recargarDatos(evaluarAuto = false)
            } else {
                val fallos = _uiState.value.intentosFallidosLogin + 1
                _uiState.value = _uiState.value.copy(
                    errorLogin = "Usuario o contraseña incorrectos (Intento #$fallos registrado en SQLite)",
                    intentosFallidosLogin = fallos
                )
                recargarDatos(evaluarAuto = false)
            }
        }
    }

    fun cerrarSesion() {
        _uiState.value = _uiState.value.copy(
            usuarioActual = null,
            errorLogin = null,
            avisoSeguridad = null
        )
    }

    fun validarPinSeguridad(pinIngresado: String): Boolean {
        return dbHelper.verificarPinSeguridad(pinIngresado, _uiState.value.usuarioActual)
    }

    private fun iniciarMotorAutomaticoIoT() {
        viewModelScope.launch {
            while (isActive) {
                val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
                val horaSistema = formatoHora.format(Date())
                val estadoActual = _uiState.value
                val horaParaEvaluar = if (estadoActual.usarHoraSimulada) {
                    estadoActual.horaSimulada
                } else {
                    horaSistema
                }

                val cambios = withContext(Dispatchers.IO) {
                    dbHelper.evaluarReglasAutomaticas(horaParaEvaluar)
                }

                _uiState.value = _uiState.value.copy(horaActualSistema = horaSistema)
                if (cambios > 0) {
                    recargarDatos(evaluarAuto = false)
                }
                delay(4000L)
            }
        }
    }

    fun recargarDatos(pisoIdPreferido: Int? = null, evaluarAuto: Boolean = true) {
        viewModelScope.launch {
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            val horaSistema = formatoHora.format(Date())
            val horaParaEvaluar = if (_uiState.value.usarHoraSimulada) {
                _uiState.value.horaSimulada
            } else {
                horaSistema
            }

            withContext(Dispatchers.IO) {
                if (evaluarAuto) {
                    dbHelper.evaluarReglasAutomaticas(horaParaEvaluar)
                }
                val pisos = dbHelper.obtenerPisos()
                val idObjetivo = pisoIdPreferido ?: _uiState.value.pisoSeleccionado?.id ?: pisos.firstOrNull()?.id
                val pisoSel = pisos.find { it.id == idObjetivo } ?: pisos.firstOrNull()
                val sectoresPiso = if (pisoSel != null) {
                    dbHelper.obtenerSectoresPorPiso(pisoSel.id)
                } else {
                    emptyList()
                }
                val todosSectores = dbHelper.obtenerTodosLosSectores()
                val historial = dbHelper.obtenerHistorial()
                val resumen = dbHelper.obtenerResumenEdificio()
                val cfgSeg = dbHelper.obtenerConfiguracionSeguridad()
                val alertas = dbHelper.obtenerAlertasSeguridad()
                val usuarios = dbHelper.obtenerUsuarios()

                _uiState.value = _uiState.value.copy(
                    pisos = pisos,
                    pisoSeleccionado = pisoSel,
                    sectoresPisoActual = sectoresPiso,
                    todosLosSectores = todosSectores,
                    historial = historial,
                    resumen = resumen,
                    configSeguridad = cfgSeg,
                    alertasSeguridad = alertas,
                    usuariosRegistrados = usuarios,
                    horaActualSistema = horaSistema
                )
            }
        }
    }

    fun seleccionarPiso(piso: Piso) {
        recargarDatos(pisoIdPreferido = piso.id, evaluarAuto = false)
    }

    fun alternarLuzManual(sector: SectorLuz, encender: Boolean) {
        if (!verificarPermisoOperacion()) return
        val nombrePiso = obtenerNombrePiso(sector.pisoId)
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarEstadoManualSector(sector, encender, nombrePiso, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun cambiarIntensidad(sector: SectorLuz, intensidad: Int) {
        if (!verificarPermisoOperacion()) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.actualizarIntensidadSector(sector.id, intensidad)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun cambiarModoSector(sector: SectorLuz, nuevoModo: ModoControl) {
        if (!verificarPermisoOperacion()) return
        val nombrePiso = obtenerNombrePiso(sector.pisoId)
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarModoControlSector(sector, nuevoModo, nombrePiso, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = true)
        }
    }

    fun guardarConfiguracionAutomatica(
        sector: SectorLuz,
        horaEncendido: String,
        horaApagado: String,
        activarHorario: Boolean,
        activarMovimiento: Boolean,
        activarLuz: Boolean,
        umbralLux: Int
    ) {
        if (!verificarPermisoOperacion()) return
        val nombrePiso = obtenerNombrePiso(sector.pisoId)
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.actualizarConfiguracionAutomatica(
                sectorId = sector.id,
                pisoNombre = nombrePiso,
                sectorNombre = sector.nombre,
                horaEncendido = horaEncendido,
                horaApagado = horaApagado,
                activarPorHorario = activarHorario,
                activarPorMovimiento = activarMovimiento,
                activarPorLuzAmbiente = activarLuz,
                umbralLux = umbralLux,
                usuario = nombreUsuarioActivo
            )
            recargarDatos(evaluarAuto = true)
        }
    }

    fun actualizarSensoresSimulados(sector: SectorLuz, movimiento: Boolean, luxActual: Int) {
        val nombrePiso = obtenerNombrePiso(sector.pisoId)
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.actualizarSensoresSector(sector, nombrePiso, movimiento, luxActual)
            recargarDatos(evaluarAuto = true)
        }
    }

    fun controlarTodoElPisoActual(encender: Boolean) {
        if (!verificarPermisoOperacion()) return
        val piso = _uiState.value.pisoSeleccionado ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.controlarLucesPorPiso(piso, encender, nombreUsuarioActivo)
            recargarDatos(pisoIdPreferido = piso.id, evaluarAuto = false)
        }
    }

    fun cambiarModoTodoElPisoActual(modo: ModoControl) {
        if (!verificarPermisoOperacion()) return
        val piso = _uiState.value.pisoSeleccionado ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarModoPorPiso(piso, modo, nombreUsuarioActivo)
            recargarDatos(pisoIdPreferido = piso.id, evaluarAuto = (modo == ModoControl.AUTOMATICO))
        }
    }

    fun controlarPisoEspecifico(piso: Piso, encender: Boolean) {
        if (!verificarPermisoOperacion()) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.controlarLucesPorPiso(piso, encender, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun cambiarModoPisoEspecifico(piso: Piso, modo: ModoControl) {
        if (!verificarPermisoOperacion()) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarModoPorPiso(piso, modo, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = (modo == ModoControl.AUTOMATICO))
        }
    }

    fun controlarEdificioCompleto(encender: Boolean?, modo: ModoControl) {
        if (!verificarPermisoOperacion()) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.controlarEdificioCompleto(encender, modo, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = (modo == ModoControl.AUTOMATICO))
        }
    }

    fun alternarModoCentinela(activo: Boolean) {
        if (_uiState.value.esSoloLectura) {
            _uiState.value = _uiState.value.copy(avisoSeguridad = "Rol de solo lectura: no puede cambiar el Modo Centinela.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarModoCentinela(activo, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun alternarProtocoloEmergencia(activo: Boolean) {
        if (_uiState.value.esSoloLectura) {
            _uiState.value = _uiState.value.copy(avisoSeguridad = "Rol de solo lectura: no puede modificar emergencias.")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarProtocoloEmergencia(activo, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun alternarSimulacionPresencia(activo: Boolean) {
        if (!verificarPermisoOperacion()) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarSimulacionPresencia(activo, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun alternarBloqueoControles(bloqueado: Boolean) {
        if (_uiState.value.esSoloLectura) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.cambiarBloqueoControles(bloqueado, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun cambiarPinMaestro(nuevoPin: String) {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.actualizarPinMaestro(nuevoPin, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun atenderAlertaSeguridad(alertaId: Int) {
        if (_uiState.value.esSoloLectura) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.marcarAlertaAtendida(alertaId, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun atenderTodasLasAlertas() {
        if (_uiState.value.esSoloLectura) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.atenderTodasLasAlertas(nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun registrarNuevoUsuario(
        username: String,
        passwordPlano: String,
        nombreCompleto: String,
        rol: RolUsuario,
        pin: String
    ) {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        viewModelScope.launch(Dispatchers.IO) {
            val exito = dbHelper.crearUsuario(username, passwordPlano, nombreCompleto, rol, pin, nombreUsuarioActivo)
            if (!exito) {
                _uiState.value = _uiState.value.copy(avisoSeguridad = "El nombre de usuario '$username' ya existe en SQLite.")
            }
            recargarDatos(evaluarAuto = false)
        }
    }

    fun eliminarUsuario(usuario: UsuarioSeguridad) {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.eliminarUsuario(usuario, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun configurarRelojSimulado(usarSimulado: Boolean, nuevaHoraSimulada: String) {
        _uiState.value = _uiState.value.copy(
            usarHoraSimulada = usarSimulado,
            horaSimulada = nuevaHoraSimulada
        )
        recargarDatos(evaluarAuto = true)
    }

    fun simularAmbienteDiaNoche(esNoche: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.simularAmbienteGlobal(esNoche, nombreUsuarioActivo)
            if (esNoche) {
                _uiState.value = _uiState.value.copy(usarHoraSimulada = true, horaSimulada = "21:30")
            } else {
                _uiState.value = _uiState.value.copy(usarHoraSimulada = true, horaSimulada = "11:00")
            }
            recargarDatos(evaluarAuto = true)
        }
    }

    fun crearNuevoPiso(numero: Int, nombre: String, descripcion: String) {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        viewModelScope.launch(Dispatchers.IO) {
            val nuevoId = dbHelper.agregarPiso(numero, nombre, descripcion, nombreUsuarioActivo).toInt()
            recargarDatos(pisoIdPreferido = if (nuevoId > 0) nuevoId else null, evaluarAuto = false)
        }
    }

    fun eliminarPisoActual() {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        val piso = _uiState.value.pisoSeleccionado ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.eliminarPiso(piso, nombreUsuarioActivo)
            recargarDatos(pisoIdPreferido = null, evaluarAuto = false)
        }
    }

    fun crearNuevoSector(nombre: String, tipoSector: String, consumoWatts: Int, modoInicial: ModoControl) {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        val piso = _uiState.value.pisoSeleccionado ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.agregarSector(piso, nombre, tipoSector, consumoWatts, modoInicial, nombreUsuarioActivo)
            recargarDatos(pisoIdPreferido = piso.id, evaluarAuto = true)
        }
    }

    fun eliminarSector(sector: SectorLuz) {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        val nombrePiso = obtenerNombrePiso(sector.pisoId)
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.eliminarSector(sector, nombrePiso, nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    fun limpiarHistorialEventos() {
        if (!verificarPermisoOperacion(requiereAdmin = true)) return
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.limpiarHistorial(nombreUsuarioActivo)
            recargarDatos(evaluarAuto = false)
        }
    }

    private fun obtenerNombrePiso(pisoId: Int): String {
        return _uiState.value.pisos.find { it.id == pisoId }?.nombre ?: "Piso $pisoId"
    }
}
