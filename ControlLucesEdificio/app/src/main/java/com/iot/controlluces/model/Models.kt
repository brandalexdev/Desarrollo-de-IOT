package com.iot.controlluces.model

enum class ModoControl {
    MANUAL,
    AUTOMATICO
}

enum class RolUsuario(val titulo: String, val descripcion: String) {
    ADMIN("Administrador", "Control total del edificio, seguridad, usuarios y base de datos"),
    OPERADOR("Operador / Guardia", "Control de luces, horarios y monitoreo de alarmas de seguridad"),
    AUDITOR("Invitado / Auditor", "Modo solo lectura para supervisión de consumo y bitácora")
}

data class UsuarioSeguridad(
    val id: Int = 0,
    val username: String,
    val nombreCompleto: String,
    val rol: RolUsuario,
    val pinSeguridad: String = "1234",
    val activo: Boolean = true,
    val ultimoAcceso: String = "Nunca"
)

data class AlertaSeguridad(
    val id: Int = 0,
    val tipoAlerta: String,
    val nivel: String,
    val ubicacion: String,
    val descripcion: String,
    val fechaHora: String,
    val atendida: Boolean = false
)

data class ConfiguracionSeguridad(
    val modoCentinelaActivo: Boolean = false,
    val emergenciaEvacuacionActiva: Boolean = false,
    val simulacionPresenciaActiva: Boolean = false,
    val bloqueoControlesManuales: Boolean = false,
    val pinMaestro: String = "1234",
    val alertasSinAtender: Int = 0
)

data class Piso(
    val id: Int = 0,
    val numero: Int,
    val nombre: String,
    val descripcion: String
)

data class SectorLuz(
    val id: Int = 0,
    val pisoId: Int,
    val nombre: String,
    val tipoSector: String,
    val encendida: Boolean = false,
    val intensidad: Int = 100,
    val modoControl: ModoControl = ModoControl.MANUAL,
    val horaEncendido: String = "18:00",
    val horaApagado: String = "06:00",
    val activarPorHorario: Boolean = true,
    val activarPorMovimiento: Boolean = true,
    val activarPorLuzAmbiente: Boolean = true,
    val umbralLux: Int = 150,
    val movimientoDetectado: Boolean = false,
    val luxActual: Int = 300,
    val consumoNominalWatts: Int = 40,
    val alarmaIntrusionPorPir: Boolean = true
) {
    val consumoActualWatts: Int
        get() = if (encendida) ((consumoNominalWatts * intensidad) / 100.0).toInt() else 0
}

data class EventoHistorial(
    val id: Int = 0,
    val pisoNombre: String,
    val sectorNombre: String,
    val accion: String,
    val origen: String,
    val usuario: String = "Sistema",
    val fechaHora: String
)

data class ResumenEdificio(
    val totalPisos: Int = 0,
    val totalSectores: Int = 0,
    val sectoresEncendidos: Int = 0,
    val sectoresAutomaticos: Int = 0,
    val consumoTotalWatts: Int = 0,
    val alertasActivas: Int = 0
)
