package com.iot.controlluces.model

/**
 * Modo de operación de un sector de iluminación del edificio.
 */
enum class ModoControl {
    MANUAL,
    AUTOMATICO
}

/**
 * Roles de seguridad para el control de acceso a la aplicación IoT.
 */
enum class RolUsuario(val titulo: String, val descripcion: String) {
    ADMIN("Administrador", "Control total del edificio, seguridad, usuarios y estructura SQLite"),
    OPERADOR("Operador / Guardia", "Control de luces, horarios y monitoreo de alarmas de seguridad"),
    AUDITOR("Invitado / Auditor", "Modo solo lectura para supervisión de consumo y bitácora")
}

/**
 * Usuario registrado en la tabla `usuarios` de SQLite (con contraseña cifrada SHA-256).
 */
data class UsuarioSeguridad(
    val id: Int = 0,
    val username: String,
    val nombreCompleto: String,
    val rol: RolUsuario,
    val pinSeguridad: String = "1234",
    val activo: Boolean = true,
    val ultimoAcceso: String = "Nunca"
)

/**
 * Alerta de seguridad registrada en la tabla `alertas_seguridad` de SQLite.
 */
data class AlertaSeguridad(
    val id: Int = 0,
    val tipoAlerta: String, // INTRUSIÓN PIR, EMERGENCIA, ACCESO FALLIDO, BLOQUEO
    val nivel: String,      // CRÍTICA, ALTA, MEDIA, INFO
    val ubicacion: String,
    val descripcion: String,
    val fechaHora: String,
    val atendida: Boolean = false
)

/**
 * Estado global del sistema de seguridad del edificio almacenado en SQLite.
 */
data class ConfiguracionSeguridad(
    val modoCentinelaActivo: Boolean = false,
    val emergenciaEvacuacionActiva: Boolean = false,
    val simulacionPresenciaActiva: Boolean = false,
    val bloqueoControlesManuales: Boolean = false,
    val pinMaestro: String = "1234",
    val alertasSinAtender: Int = 0
)

/**
 * Representa un Piso o Nivel dentro del edificio inteligente.
 */
data class Piso(
    val id: Int = 0,
    val numero: Int,
    val nombre: String,
    val descripcion: String
)

/**
 * Representa un Sector o Zona luminosa perteneciente a un Piso.
 * Incluye estado manual, intensidad (dimmer), configuración de horario automático,
 * parámetros de sensores IoT (PIR movimiento y LDR luminosidad en Lux) y bandera de zona crítica de seguridad.
 */
data class SectorLuz(
    val id: Int = 0,
    val pisoId: Int,
    val nombre: String,
    val tipoSector: String,
    val encendida: Boolean = false,
    val intensidad: Int = 100, // 10% a 100%
    val modoControl: ModoControl = ModoControl.MANUAL,
    val horaEncendido: String = "18:00",
    val horaApagado: String = "06:00",
    val activarPorHorario: Boolean = true,
    val activarPorMovimiento: Boolean = true,
    val activarPorLuzAmbiente: Boolean = true,
    val umbralLux: Int = 150, // Si luxActual <= umbralLux, hay poca luz natural
    val movimientoDetectado: Boolean = false,
    val luxActual: Int = 300,
    val consumoNominalWatts: Int = 40,
    val alarmaIntrusionPorPir: Boolean = true // Si el modo centinela está activo y detecta PIR, dispara alarma
) {
    /**
     * Consumo energético real en Watts considerando si la luz está encendida y su porcentaje de intensidad.
     */
    val consumoActualWatts: Int
        get() = if (encendida) ((consumoNominalWatts * intensidad) / 100.0).toInt() else 0
}

/**
 * Registro de auditoría en la base de datos SQLite cuando una luz cambia de estado
 * de forma manual, por piso, por protocolo de seguridad o mediante automatización IoT.
 */
data class EventoHistorial(
    val id: Int = 0,
    val pisoNombre: String,
    val sectorNombre: String,
    val accion: String,
    val origen: String,
    val usuario: String = "Sistema",
    val fechaHora: String
)

/**
 * Métricas globales del edificio calculadas desde SQLite.
 */
data class ResumenEdificio(
    val totalPisos: Int = 0,
    val totalSectores: Int = 0,
    val sectoresEncendidos: Int = 0,
    val sectoresAutomaticos: Int = 0,
    val consumoTotalWatts: Int = 0,
    val alertasActivas: Int = 0
)
