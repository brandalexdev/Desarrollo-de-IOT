package com.iot.controlluces.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.iot.controlluces.model.AlertaSeguridad
import com.iot.controlluces.model.ConfiguracionSeguridad
import com.iot.controlluces.model.EventoHistorial
import com.iot.controlluces.model.ModoControl
import com.iot.controlluces.model.Piso
import com.iot.controlluces.model.ResumenEdificio
import com.iot.controlluces.model.RolUsuario
import com.iot.controlluces.model.SectorLuz
import com.iot.controlluces.model.UsuarioSeguridad
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor de Base de Datos SQLite nativo (SQLiteOpenHelper) para el sistema IoT
 * de control de iluminación y seguridad del edificio por piso y sector.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "control_luces_edificio_iot.db"
        const val DATABASE_VERSION = 2

        // 1. Tabla Pisos
        const val TABLE_PISOS = "pisos"
        const val COL_PISO_ID = "id"
        const val COL_PISO_NUMERO = "numero"
        const val COL_PISO_NOMBRE = "nombre"
        const val COL_PISO_DESCRIPCION = "descripcion"

        // 2. Tabla Sectores
        const val TABLE_SECTORES = "sectores"
        const val COL_SECTOR_ID = "id"
        const val COL_SECTOR_PISO_ID = "piso_id"
        const val COL_SECTOR_NOMBRE = "nombre"
        const val COL_SECTOR_TIPO = "tipo_sector"
        const val COL_SECTOR_ENCENDIDA = "encendida"
        const val COL_SECTOR_INTENSIDAD = "intensidad"
        const val COL_SECTOR_MODO = "modo_control"
        const val COL_SECTOR_HORA_ON = "hora_encendido"
        const val COL_SECTOR_HORA_OFF = "hora_apagado"
        const val COL_SECTOR_ACT_HORARIO = "activar_por_horario"
        const val COL_SECTOR_ACT_MOV = "activar_por_movimiento"
        const val COL_SECTOR_ACT_LUZ = "activar_por_luz"
        const val COL_SECTOR_UMBRAL_LUX = "umbral_lux"
        const val COL_SECTOR_MOV_DETECTADO = "movimiento_detectado"
        const val COL_SECTOR_LUX_ACTUAL = "lux_actual"
        const val COL_SECTOR_WATTS = "consumo_watts"
        const val COL_SECTOR_ALARMA_PIR = "alarma_intrusion_pir"

        // 3. Tabla Historial de Eventos
        const val TABLE_HISTORIAL = "historial_eventos"
        const val COL_HIST_ID = "id"
        const val COL_HIST_PISO = "piso_nombre"
        const val COL_HIST_SECTOR = "sector_nombre"
        const val COL_HIST_ACCION = "accion"
        const val COL_HIST_ORIGEN = "origen"
        const val COL_HIST_USUARIO = "usuario"
        const val COL_HIST_FECHA = "fecha_hora"

        // 4. Tabla Usuarios de Seguridad (Login y Roles con SHA-256)
        const val TABLE_USUARIOS = "usuarios"
        const val COL_USER_ID = "id"
        const val COL_USER_USERNAME = "username"
        const val COL_USER_PASS_HASH = "password_hash"
        const val COL_USER_NOMBRE = "nombre_completo"
        const val COL_USER_ROL = "rol"
        const val COL_USER_PIN = "pin_seguridad"
        const val COL_USER_ACTIVO = "activo"
        const val COL_USER_ULTIMO_ACCESO = "ultimo_acceso"

        // 5. Tabla Configuración Global de Seguridad del Edificio
        const val TABLE_CONFIG_SEGURIDAD = "configuracion_seguridad"
        const val COL_CFG_ID = "id"
        const val COL_CFG_CENTINELA = "modo_centinela"
        const val COL_CFG_EMERGENCIA = "emergencia_evacuacion"
        const val COL_CFG_SIMULACION = "simulacion_presencia"
        const val COL_CFG_BLOQUEO = "bloqueo_controles"
        const val COL_CFG_PIN_MAESTRO = "pin_maestro"

        // 6. Tabla Alertas de Seguridad e Intrusión
        const val TABLE_ALERTAS = "alertas_seguridad"
        const val COL_ALERTA_ID = "id"
        const val COL_ALERTA_TIPO = "tipo_alerta"
        const val COL_ALERTA_NIVEL = "nivel"
        const val COL_ALERTA_UBICACION = "ubicacion"
        const val COL_ALERTA_DESC = "descripcion"
        const val COL_ALERTA_FECHA = "fecha_hora"
        const val COL_ALERTA_ATENDIDA = "atendida"

        /**
         * Genera hash criptográfico SHA-256 para proteger contraseñas y PINs en SQLite.
         */
        fun hashSHA256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createPisosTable = """
            CREATE TABLE $TABLE_PISOS (
                $COL_PISO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PISO_NUMERO INTEGER NOT NULL,
                $COL_PISO_NOMBRE TEXT NOT NULL,
                $COL_PISO_DESCRIPCION TEXT NOT NULL
            )
        """.trimIndent()

        val createSectoresTable = """
            CREATE TABLE $TABLE_SECTORES (
                $COL_SECTOR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SECTOR_PISO_ID INTEGER NOT NULL,
                $COL_SECTOR_NOMBRE TEXT NOT NULL,
                $COL_SECTOR_TIPO TEXT NOT NULL,
                $COL_SECTOR_ENCENDIDA INTEGER NOT NULL DEFAULT 0,
                $COL_SECTOR_INTENSIDAD INTEGER NOT NULL DEFAULT 100,
                $COL_SECTOR_MODO TEXT NOT NULL DEFAULT 'MANUAL',
                $COL_SECTOR_HORA_ON TEXT NOT NULL DEFAULT '18:00',
                $COL_SECTOR_HORA_OFF TEXT NOT NULL DEFAULT '06:00',
                $COL_SECTOR_ACT_HORARIO INTEGER NOT NULL DEFAULT 1,
                $COL_SECTOR_ACT_MOV INTEGER NOT NULL DEFAULT 1,
                $COL_SECTOR_ACT_LUZ INTEGER NOT NULL DEFAULT 1,
                $COL_SECTOR_UMBRAL_LUX INTEGER NOT NULL DEFAULT 150,
                $COL_SECTOR_MOV_DETECTADO INTEGER NOT NULL DEFAULT 0,
                $COL_SECTOR_LUX_ACTUAL INTEGER NOT NULL DEFAULT 300,
                $COL_SECTOR_WATTS INTEGER NOT NULL DEFAULT 40,
                $COL_SECTOR_ALARMA_PIR INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY($COL_SECTOR_PISO_ID) REFERENCES $TABLE_PISOS($COL_PISO_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createHistorialTable = """
            CREATE TABLE $TABLE_HISTORIAL (
                $COL_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HIST_PISO TEXT NOT NULL,
                $COL_HIST_SECTOR TEXT NOT NULL,
                $COL_HIST_ACCION TEXT NOT NULL,
                $COL_HIST_ORIGEN TEXT NOT NULL,
                $COL_HIST_USUARIO TEXT NOT NULL DEFAULT 'Sistema',
                $COL_HIST_FECHA TEXT NOT NULL
            )
        """.trimIndent()

        val createUsuariosTable = """
            CREATE TABLE $TABLE_USUARIOS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_USERNAME TEXT NOT NULL UNIQUE,
                $COL_USER_PASS_HASH TEXT NOT NULL,
                $COL_USER_NOMBRE TEXT NOT NULL,
                $COL_USER_ROL TEXT NOT NULL,
                $COL_USER_PIN TEXT NOT NULL DEFAULT '1234',
                $COL_USER_ACTIVO INTEGER NOT NULL DEFAULT 1,
                $COL_USER_ULTIMO_ACCESO TEXT NOT NULL DEFAULT 'Nunca'
            )
        """.trimIndent()

        val createConfigSeguridadTable = """
            CREATE TABLE $TABLE_CONFIG_SEGURIDAD (
                $COL_CFG_ID INTEGER PRIMARY KEY,
                $COL_CFG_CENTINELA INTEGER NOT NULL DEFAULT 0,
                $COL_CFG_EMERGENCIA INTEGER NOT NULL DEFAULT 0,
                $COL_CFG_SIMULACION INTEGER NOT NULL DEFAULT 0,
                $COL_CFG_BLOQUEO INTEGER NOT NULL DEFAULT 0,
                $COL_CFG_PIN_MAESTRO TEXT NOT NULL DEFAULT '1234'
            )
        """.trimIndent()

        val createAlertasTable = """
            CREATE TABLE $TABLE_ALERTAS (
                $COL_ALERTA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ALERTA_TIPO TEXT NOT NULL,
                $COL_ALERTA_NIVEL TEXT NOT NULL,
                $COL_ALERTA_UBICACION TEXT NOT NULL,
                $COL_ALERTA_DESC TEXT NOT NULL,
                $COL_ALERTA_FECHA TEXT NOT NULL,
                $COL_ALERTA_ATENDIDA INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createPisosTable)
        db.execSQL(createSectoresTable)
        db.execSQL(createHistorialTable)
        db.execSQL(createUsuariosTable)
        db.execSQL(createConfigSeguridadTable)
        db.execSQL(createAlertasTable)

        insertarDatosIniciales(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTAS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONFIG_SEGURIDAD")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORIAL")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SECTORES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PISOS")
        onCreate(db)
    }

    /**
     * Carga inicial de pisos, sectores, usuarios con roles (SHA-256) y configuración de seguridad.
     */
    private fun insertarDatosIniciales(db: SQLiteDatabase) {
        // Configuración inicial de seguridad
        val cfgValues = ContentValues().apply {
            put(COL_CFG_ID, 1)
            put(COL_CFG_CENTINELA, 0)
            put(COL_CFG_EMERGENCIA, 0)
            put(COL_CFG_SIMULACION, 0)
            put(COL_CFG_BLOQUEO, 0)
            put(COL_CFG_PIN_MAESTRO, "1234")
        }
        db.insert(TABLE_CONFIG_SEGURIDAD, null, cfgValues)

        // Usuarios predeterminados con contraseñas cifradas en SHA-256
        insertarUsuarioInterno(db, "admin", "admin123", "Administrador General IoT", RolUsuario.ADMIN, "1234")
        insertarUsuarioInterno(db, "guardia", "guardia123", "Supervisor de Seguridad", RolUsuario.OPERADOR, "2580")
        insertarUsuarioInterno(db, "auditor", "123456", "Auditor Energético", RolUsuario.AUDITOR, "0000")

        // Pisos del edificio
        val piso1Id = insertarPisoInterno(db, 1, "Piso 1 - Recepción y Acceso", "Lobby principal, pasillos de acceso y estacionamiento")
        val piso2Id = insertarPisoInterno(db, 2, "Piso 2 - Oficinas Corporativas", "Áreas de trabajo abierto, salas de reuniones y comedor")
        val piso3Id = insertarPisoInterno(db, 3, "Piso 3 - Laboratorios IoT & TI", "Servidores, laboratorio de electrónica y desarrollo")
        val piso4Id = insertarPisoInterno(db, 4, "Piso 4 - Dirección y Terraza", "Oficinas ejecutivas, auditorio y terraza exterior")

        // Sectores Piso 1
        insertarSectorInterno(db, piso1Id, "Lobby Principal", "Acceso", true, 100, ModoControl.AUTOMATICO, "18:00", "07:00", true, true, true, 180, true, 120, 80)
        insertarSectorInterno(db, piso1Id, "Pasillo Norte", "Pasillo", false, 80, ModoControl.AUTOMATICO, "18:30", "06:30", true, true, false, 150, false, 250, 45)
        insertarSectorInterno(db, piso1Id, "Sala de Espera", "Común", true, 90, ModoControl.MANUAL, "19:00", "06:00", false, true, true, 150, false, 200, 60)
        insertarSectorInterno(db, piso1Id, "Estacionamiento Subsuelo", "Exterior/Acceso", true, 100, ModoControl.AUTOMATICO, "00:00", "23:59", true, true, true, 200, true, 50, 120)

        // Sectores Piso 2
        insertarSectorInterno(db, piso2Id, "Oficina Abierta A", "Oficina", true, 100, ModoControl.MANUAL, "08:00", "19:00", true, true, true, 200, true, 140, 95)
        insertarSectorInterno(db, piso2Id, "Sala de Juntas Principal", "Reuniones", false, 75, ModoControl.AUTOMATICO, "08:30", "18:30", false, true, false, 150, false, 310, 65)
        insertarSectorInterno(db, piso2Id, "Pasillo Central P2", "Pasillo", false, 70, ModoControl.AUTOMATICO, "18:00", "06:30", true, true, true, 140, false, 280, 40)
        insertarSectorInterno(db, piso2Id, "Cafetería y Comedor", "Común", false, 85, ModoControl.MANUAL, "07:30", "16:00", true, false, false, 160, false, 350, 70)

        // Sectores Piso 3
        insertarSectorInterno(db, piso3Id, "Laboratorio de Hardware IoT", "Laboratorio", true, 100, ModoControl.MANUAL, "08:00", "21:00", true, true, true, 220, true, 180, 110)
        insertarSectorInterno(db, piso3Id, "Sala de Servidores (CPD)", "Técnico", true, 60, ModoControl.AUTOMATICO, "18:00", "06:00", false, true, false, 100, true, 80, 50)
        insertarSectorInterno(db, piso3Id, "Área de Desarrollo", "Oficina", false, 90, ModoControl.AUTOMATICO, "08:00", "20:00", false, true, true, 180, false, 290, 85)
        insertarSectorInterno(db, piso3Id, "Pasillo Sur P3", "Pasillo", false, 75, ModoControl.AUTOMATICO, "18:00", "06:30", true, true, false, 140, false, 260, 40)

        // Sectores Piso 4
        insertarSectorInterno(db, piso4Id, "Oficinas Dirección", "Oficina", false, 100, ModoControl.MANUAL, "09:00", "18:00", false, true, true, 170, false, 320, 75)
        insertarSectorInterno(db, piso4Id, "Auditorio de Conferencias", "Reuniones", false, 80, ModoControl.MANUAL, "09:00", "19:00", false, false, false, 150, false, 240, 130)
        insertarSectorInterno(db, piso4Id, "Terraza y Jardín Exterior", "Exterior/Acceso", false, 100, ModoControl.AUTOMATICO, "18:30", "06:00", true, false, true, 120, false, 400, 90)

        registrarAlertaInterna(
            db,
            tipoAlerta = "SISTEMA DE SEGURIDAD",
            nivel = "INFO",
            ubicacion = "Edificio Central",
            descripcion = "Módulos de Seguridad, Control de Acceso SHA-256 y Sensores PIR listos.",
            atendida = true
        )

        registrarEventoInterno(
            db,
            "Sistema Central",
            "Todos los Pisos",
            "Base de datos SQLite inicializada con 4 pisos, 15 sectores y módulos de seguridad",
            "SISTEMA",
            "Sistema"
        )
    }

    private fun insertarUsuarioInterno(
        db: SQLiteDatabase,
        username: String,
        passwordPlano: String,
        nombreCompleto: String,
        rol: RolUsuario,
        pin: String
    ) {
        val values = ContentValues().apply {
            put(COL_USER_USERNAME, username.lowercase().trim())
            put(COL_USER_PASS_HASH, hashSHA256(passwordPlano))
            put(COL_USER_NOMBRE, nombreCompleto)
            put(COL_USER_ROL, rol.name)
            put(COL_USER_PIN, pin)
            put(COL_USER_ACTIVO, 1)
            put(COL_USER_ULTIMO_ACCESO, "Nunca")
        }
        db.insert(TABLE_USUARIOS, null, values)
    }

    private fun insertarPisoInterno(
        db: SQLiteDatabase,
        numero: Int,
        nombre: String,
        descripcion: String
    ): Int {
        val values = ContentValues().apply {
            put(COL_PISO_NUMERO, numero)
            put(COL_PISO_NOMBRE, nombre)
            put(COL_PISO_DESCRIPCION, descripcion)
        }
        return db.insert(TABLE_PISOS, null, values).toInt()
    }

    private fun insertarSectorInterno(
        db: SQLiteDatabase,
        pisoId: Int,
        nombre: String,
        tipoSector: String,
        encendida: Boolean,
        intensidad: Int,
        modoControl: ModoControl,
        horaEncendido: String,
        horaApagado: String,
        activarHorario: Boolean,
        activarMovimiento: Boolean,
        activarLuz: Boolean,
        umbralLux: Int,
        movimientoDetectado: Boolean,
        luxActual: Int,
        consumoWatts: Int
    ) {
        val values = ContentValues().apply {
            put(COL_SECTOR_PISO_ID, pisoId)
            put(COL_SECTOR_NOMBRE, nombre)
            put(COL_SECTOR_TIPO, tipoSector)
            put(COL_SECTOR_ENCENDIDA, if (encendida) 1 else 0)
            put(COL_SECTOR_INTENSIDAD, intensidad)
            put(COL_SECTOR_MODO, modoControl.name)
            put(COL_SECTOR_HORA_ON, horaEncendido)
            put(COL_SECTOR_HORA_OFF, horaApagado)
            put(COL_SECTOR_ACT_HORARIO, if (activarHorario) 1 else 0)
            put(COL_SECTOR_ACT_MOV, if (activarMovimiento) 1 else 0)
            put(COL_SECTOR_ACT_LUZ, if (activarLuz) 1 else 0)
            put(COL_SECTOR_UMBRAL_LUX, umbralLux)
            put(COL_SECTOR_MOV_DETECTADO, if (movimientoDetectado) 1 else 0)
            put(COL_SECTOR_LUX_ACTUAL, luxActual)
            put(COL_SECTOR_WATTS, consumoWatts)
            put(COL_SECTOR_ALARMA_PIR, 1)
        }
        db.insert(TABLE_SECTORES, null, values)
    }

    private fun registrarEventoInterno(
        db: SQLiteDatabase,
        pisoNombre: String,
        sectorNombre: String,
        accion: String,
        origen: String,
        usuario: String = "Sistema"
    ) {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val values = ContentValues().apply {
            put(COL_HIST_PISO, pisoNombre)
            put(COL_HIST_SECTOR, sectorNombre)
            put(COL_HIST_ACCION, accion)
            put(COL_HIST_ORIGEN, origen)
            put(COL_HIST_USUARIO, usuario)
            put(COL_HIST_FECHA, formato.format(Date()))
        }
        db.insert(TABLE_HISTORIAL, null, values)
    }

    private fun registrarAlertaInterna(
        db: SQLiteDatabase,
        tipoAlerta: String,
        nivel: String,
        ubicacion: String,
        descripcion: String,
        atendida: Boolean = false
    ) {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val values = ContentValues().apply {
            put(COL_ALERTA_TIPO, tipoAlerta)
            put(COL_ALERTA_NIVEL, nivel)
            put(COL_ALERTA_UBICACION, ubicacion)
            put(COL_ALERTA_DESC, descripcion)
            put(COL_ALERTA_FECHA, formato.format(Date()))
            put(COL_ALERTA_ATENDIDA, if (atendida) 1 else 0)
        }
        db.insert(TABLE_ALERTAS, null, values)
    }

    // ==================== MÓDULO DE SEGURIDAD: LOGIN, USUARIOS Y PIN ====================

    fun autenticarUsuario(usernameInput: String, passwordPlano: String): UsuarioSeguridad? {
        val db = writableDatabase
        val userClean = usernameInput.lowercase().trim()
        val hashCalculado = hashSHA256(passwordPlano)

        val cursor = db.query(
            TABLE_USUARIOS,
            null,
            "$COL_USER_USERNAME = ? AND $COL_USER_PASS_HASH = ? AND $COL_USER_ACTIVO = 1",
            arrayOf(userClean, hashCalculado),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val usuario = mapearCursorAUsuario(it)
                val ahora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val updateValues = ContentValues().apply {
                    put(COL_USER_ULTIMO_ACCESO, ahora)
                }
                db.update(TABLE_USUARIOS, updateValues, "$COL_USER_ID = ?", arrayOf(usuario.id.toString()))
                registrarEventoInterno(
                    db,
                    "Seguridad / Acceso",
                    "Login Exitoso",
                    "Inicio de sesión autorizado (${usuario.rol.titulo})",
                    "SEGURIDAD",
                    usuario.username
                )
                return usuario.copy(ultimoAcceso = ahora)
            }
        }

        // Si falló la autenticación, registrar intento fallido en historial y en alertas de seguridad
        registrarEventoInterno(
            db,
            "Seguridad / Acceso",
            "Login Fallido",
            "Intento de acceso rechazado para el usuario '$userClean'",
            "SEGURIDAD",
            userClean.ifBlank { "Desconocido" }
        )
        registrarAlertaInterna(
            db,
            tipoAlerta = "ACCESO FALLIDO",
            nivel = "MEDIA",
            ubicacion = "Terminal Android",
            descripcion = "Credenciales incorrectas al intentar ingresar con usuario '$userClean'"
        )
        return null
    }

    fun verificarPinSeguridad(pinIngresado: String, usuarioActual: UsuarioSeguridad?): Boolean {
        val cfg = obtenerConfiguracionSeguridad()
        val pinUsuario = usuarioActual?.pinSeguridad
        return pinIngresado.trim() == cfg.pinMaestro || (pinUsuario != null && pinIngresado.trim() == pinUsuario)
    }

    fun obtenerUsuarios(): List<UsuarioSeguridad> {
        val lista = mutableListOf<UsuarioSeguridad>()
        val db = readableDatabase
        val cursor = db.query(TABLE_USUARIOS, null, null, null, null, null, "$COL_USER_ID ASC")
        cursor.use {
            while (it.moveToNext()) {
                lista.add(mapearCursorAUsuario(it))
            }
        }
        return lista
    }

    private fun mapearCursorAUsuario(cursor: Cursor): UsuarioSeguridad {
        val rolStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ROL))
        val rol = try {
            RolUsuario.valueOf(rolStr)
        } catch (_: Exception) {
            RolUsuario.OPERADOR
        }
        return UsuarioSeguridad(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)),
            username = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_USERNAME)),
            nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NOMBRE)),
            rol = rol,
            pinSeguridad = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PIN)),
            activo = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ACTIVO)) == 1,
            ultimoAcceso = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ULTIMO_ACCESO))
        )
    }

    fun crearUsuario(
        username: String,
        passwordPlano: String,
        nombreCompleto: String,
        rol: RolUsuario,
        pin: String,
        creadoPor: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_USERNAME, username.lowercase().trim())
            put(COL_USER_PASS_HASH, hashSHA256(passwordPlano))
            put(COL_USER_NOMBRE, nombreCompleto.trim())
            put(COL_USER_ROL, rol.name)
            put(COL_USER_PIN, pin.ifBlank { "1234" })
            put(COL_USER_ACTIVO, 1)
            put(COL_USER_ULTIMO_ACCESO, "Nunca")
        }
        val id = db.insertWithOnConflict(TABLE_USUARIOS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        if (id > 0) {
            registrarEventoInterno(
                db,
                "Seguridad / Usuarios",
                username,
                "Usuario creado con rol ${rol.titulo} y hash SHA-256",
                "SEGURIDAD",
                creadoPor
            )
            return true
        }
        return false
    }

    fun eliminarUsuario(usuario: UsuarioSeguridad, eliminadoPor: String) {
        if (usuario.username == "admin") return // Proteger cuenta admin principal
        val db = writableDatabase
        db.delete(TABLE_USUARIOS, "$COL_USER_ID = ?", arrayOf(usuario.id.toString()))
        registrarEventoInterno(
            db,
            "Seguridad / Usuarios",
            usuario.username,
            "Usuario eliminado del sistema SQLite",
            "SEGURIDAD",
            eliminadoPor
        )
    }

    // ==================== PROTOCOLOS DE SEGURIDAD DEL EDIFICIO ====================

    fun obtenerConfiguracionSeguridad(): ConfiguracionSeguridad {
        val db = readableDatabase
        var modoCentinela = false
        var emergencia = false
        var simulacion = false
        var bloqueo = false
        var pinMaestro = "1234"

        val cursor = db.query(
            TABLE_CONFIG_SEGURIDAD,
            null,
            "$COL_CFG_ID = 1",
            null,
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                modoCentinela = it.getInt(it.getColumnIndexOrThrow(COL_CFG_CENTINELA)) == 1
                emergencia = it.getInt(it.getColumnIndexOrThrow(COL_CFG_EMERGENCIA)) == 1
                simulacion = it.getInt(it.getColumnIndexOrThrow(COL_CFG_SIMULACION)) == 1
                bloqueo = it.getInt(it.getColumnIndexOrThrow(COL_CFG_BLOQUEO)) == 1
                pinMaestro = it.getString(it.getColumnIndexOrThrow(COL_CFG_PIN_MAESTRO))
            }
        }

        val cursorAlertas = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_ALERTAS WHERE $COL_ALERTA_ATENDIDA = 0",
            null
        )
        var sinAtender = 0
        cursorAlertas.use {
            if (it.moveToFirst()) {
                sinAtender = it.getInt(0)
            }
        }

        return ConfiguracionSeguridad(
            modoCentinelaActivo = modoCentinela,
            emergenciaEvacuacionActiva = emergencia,
            simulacionPresenciaActiva = simulacion,
            bloqueoControlesManuales = bloqueo,
            pinMaestro = pinMaestro,
            alertasSinAtender = sinAtender
        )
    }

    /**
     * Activa o desactiva el Modo Centinela (Vigilancia por sensores PIR con encendido disuasivo y alerta).
     */
    fun cambiarModoCentinela(activo: Boolean, usuario: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CFG_CENTINELA, if (activo) 1 else 0)
        }
        db.update(TABLE_CONFIG_SEGURIDAD, values, "$COL_CFG_ID = 1", null)
        registrarEventoInterno(
            db,
            "Seguridad Edificio",
            "Modo Centinela / Vigilancia PIR",
            if (activo) "Modo Centinela ACTIVADO (Monitoreo de intrusión por sensores PIR)" else "Modo Centinela DESACTIVADO",
            "SEGURIDAD",
            usuario
        )
    }

    /**
     * Protocolo de Emergencia / Evacuación:
     * Si se activa, enciende absolutamente todas las luces del edificio al 100% de intensidad,
     * bloquea el apagado accidental y registra una alerta crítica en SQLite.
     */
    fun cambiarProtocoloEmergencia(activo: Boolean, usuario: String) {
        val db = writableDatabase
        val cfgValues = ContentValues().apply {
            put(COL_CFG_EMERGENCIA, if (activo) 1 else 0)
            if (activo) {
                put(COL_CFG_BLOQUEO, 1)
            } else {
                put(COL_CFG_BLOQUEO, 0)
            }
        }
        db.update(TABLE_CONFIG_SEGURIDAD, cfgValues, "$COL_CFG_ID = 1", null)

        if (activo) {
            val secValues = ContentValues().apply {
                put(COL_SECTOR_ENCENDIDA, 1)
                put(COL_SECTOR_INTENSIDAD, 100)
                put(COL_SECTOR_MODO, ModoControl.MANUAL.name)
            }
            db.update(TABLE_SECTORES, secValues, null, null)
            registrarAlertaInterna(
                db,
                tipoAlerta = "EMERGENCIA / EVACUACIÓN",
                nivel = "CRÍTICA",
                ubicacion = "Edificio Completo (Todos los Pisos)",
                descripcion = "Protocolo de Emergencia activado por $usuario: Todas las luces al 100% para evacuación."
            )
            registrarEventoInterno(
                db,
                "Seguridad Edificio",
                "Todos los Pisos",
                "EMERGENCIA ACTIVADA: Todas las luces encendidas al 100%",
                "SEGURIDAD",
                usuario
            )
        } else {
            registrarEventoInterno(
                db,
                "Seguridad Edificio",
                "Todos los Pisos",
                "Protocolo de Emergencia desactivado con PIN de seguridad",
                "SEGURIDAD",
                usuario
            )
        }
    }

    /**
     * Modo Simulación de Presencia Nocturna (Antirrobo):
     * Enciende sectores estratégicos de acceso, lobby y oficinas para simular ocupación.
     */
    fun cambiarSimulacionPresencia(activo: Boolean, usuario: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CFG_SIMULACION, if (activo) 1 else 0)
        }
        db.update(TABLE_CONFIG_SEGURIDAD, values, "$COL_CFG_ID = 1", null)

        if (activo) {
            // Encender sectores de Acceso, Pasillo y Oficina de forma intercalada
            val sectores = obtenerTodosLosSectores()
            sectores.forEachIndexed { index, sector ->
                val encenderDisuasivo = (index % 2 == 0) || sector.tipoSector.contains("Acceso", ignoreCase = true)
                val sv = ContentValues().apply {
                    put(COL_SECTOR_ENCENDIDA, if (encenderDisuasivo) 1 else 0)
                    put(COL_SECTOR_INTENSIDAD, if (encenderDisuasivo) 85 else 70)
                }
                db.update(TABLE_SECTORES, sv, "$COL_SECTOR_ID = ?", arrayOf(sector.id.toString()))
            }
        }

        registrarEventoInterno(
            db,
            "Seguridad Edificio",
            "Simulación Antirrobo",
            if (activo) "Simulación de presencia nocturna ACTIVADA" else "Simulación de presencia DESACTIVADA",
            "SEGURIDAD",
            usuario
        )
    }

    /**
     * Bloqueo de Seguridad de Controles Manuales (requiere PIN para desbloquear).
     */
    fun cambiarBloqueoControles(bloqueado: Boolean, usuario: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CFG_BLOQUEO, if (bloqueado) 1 else 0)
        }
        db.update(TABLE_CONFIG_SEGURIDAD, values, "$COL_CFG_ID = 1", null)
        registrarEventoInterno(
            db,
            "Seguridad Edificio",
            "Panel de Control",
            if (bloqueado) "Controles manuales BLOQUEADOS por seguridad" else "Controles manuales DESBLOQUEADOS",
            "SEGURIDAD",
            usuario
        )
    }

    fun actualizarPinMaestro(nuevoPin: String, usuario: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CFG_PIN_MAESTRO, nuevoPin.trim())
        }
        db.update(TABLE_CONFIG_SEGURIDAD, values, "$COL_CFG_ID = 1", null)
        registrarEventoInterno(
            db,
            "Seguridad Edificio",
            "PIN Maestro",
            "PIN Maestro de seguridad actualizado en SQLite",
            "SEGURIDAD",
            usuario
        )
    }

    fun obtenerAlertasSeguridad(limite: Int = 50): List<AlertaSeguridad> {
        val lista = mutableListOf<AlertaSeguridad>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ALERTAS,
            null,
            null,
            null,
            null,
            null,
            "$COL_ALERTA_ATENDIDA ASC, $COL_ALERTA_ID DESC",
            limite.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    AlertaSeguridad(
                        id = it.getInt(it.getColumnIndexOrThrow(COL_ALERTA_ID)),
                        tipoAlerta = it.getString(it.getColumnIndexOrThrow(COL_ALERTA_TIPO)),
                        nivel = it.getString(it.getColumnIndexOrThrow(COL_ALERTA_NIVEL)),
                        ubicacion = it.getString(it.getColumnIndexOrThrow(COL_ALERTA_UBICACION)),
                        descripcion = it.getString(it.getColumnIndexOrThrow(COL_ALERTA_DESC)),
                        fechaHora = it.getString(it.getColumnIndexOrThrow(COL_ALERTA_FECHA)),
                        atendida = it.getInt(it.getColumnIndexOrThrow(COL_ALERTA_ATENDIDA)) == 1
                    )
                )
            }
        }
        return lista
    }

    fun marcarAlertaAtendida(alertaId: Int, usuario: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ALERTA_ATENDIDA, 1)
        }
        db.update(TABLE_ALERTAS, values, "$COL_ALERTA_ID = ?", arrayOf(alertaId.toString()))
        registrarEventoInterno(
            db,
            "Seguridad Edificio",
            "Alerta #$alertaId",
            "Alerta de seguridad marcada como atendida/verificada",
            "SEGURIDAD",
            usuario
        )
    }

    fun atenderTodasLasAlertas(usuario: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_ALERTA_ATENDIDA, 1)
        }
        db.update(TABLE_ALERTAS, values, null, null)
        registrarEventoInterno(
            db,
            "Seguridad Edificio",
            "Todas las Alertas",
            "Todas las alertas de seguridad fueron marcadas como atendidas",
            "SEGURIDAD",
            usuario
        )
    }

    // ==================== CONSULTAS Y OPERACIONES DE PISOS ====================

    fun obtenerPisos(): List<Piso> {
        val lista = mutableListOf<Piso>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PISOS,
            null,
            null,
            null,
            null,
            null,
            "$COL_PISO_NUMERO ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(
                    Piso(
                        id = it.getInt(it.getColumnIndexOrThrow(COL_PISO_ID)),
                        numero = it.getInt(it.getColumnIndexOrThrow(COL_PISO_NUMERO)),
                        nombre = it.getString(it.getColumnIndexOrThrow(COL_PISO_NOMBRE)),
                        descripcion = it.getString(it.getColumnIndexOrThrow(COL_PISO_DESCRIPCION))
                    )
                )
            }
        }
        return lista
    }

    fun agregarPiso(numero: Int, nombre: String, descripcion: String, usuario: String = "admin"): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_PISO_NUMERO, numero)
            put(COL_PISO_NOMBRE, nombre)
            put(COL_PISO_DESCRIPCION, descripcion)
        }
        val id = db.insert(TABLE_PISOS, null, values)
        if (id > 0) {
            registrarEventoInterno(db, nombre, "Nuevo Piso", "Se creó el piso #$numero ($nombre)", "ADMIN", usuario)
        }
        return id
    }

    fun eliminarPiso(piso: Piso, usuario: String = "admin") {
        val db = writableDatabase
        db.delete(TABLE_PISOS, "$COL_PISO_ID = ?", arrayOf(piso.id.toString()))
        registrarEventoInterno(db, piso.nombre, "Piso Completo", "Se eliminó el piso y sus sectores asociados", "ADMIN", usuario)
    }

    // ==================== CONSULTAS Y OPERACIONES DE SECTORES ====================

    fun obtenerSectoresPorPiso(pisoId: Int): List<SectorLuz> {
        val lista = mutableListOf<SectorLuz>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SECTORES,
            null,
            "$COL_SECTOR_PISO_ID = ?",
            arrayOf(pisoId.toString()),
            null,
            null,
            "$COL_SECTOR_ID ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(mapearCursorASector(it))
            }
        }
        return lista
    }

    fun obtenerTodosLosSectores(): List<SectorLuz> {
        val lista = mutableListOf<SectorLuz>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SECTORES,
            null,
            null,
            null,
            null,
            null,
            "$COL_SECTOR_PISO_ID ASC, $COL_SECTOR_ID ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(mapearCursorASector(it))
            }
        }
        return lista
    }

    private fun mapearCursorASector(cursor: Cursor): SectorLuz {
        val modoStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_SECTOR_MODO))
        val modo = try {
            ModoControl.valueOf(modoStr)
        } catch (_: Exception) {
            ModoControl.MANUAL
        }

        val alarmaIdx = cursor.getColumnIndex(COL_SECTOR_ALARMA_PIR)
        val alarmaPir = if (alarmaIdx >= 0) cursor.getInt(alarmaIdx) == 1 else true

        return SectorLuz(
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_ID)),
            pisoId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_PISO_ID)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(COL_SECTOR_NOMBRE)),
            tipoSector = cursor.getString(cursor.getColumnIndexOrThrow(COL_SECTOR_TIPO)),
            encendida = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_ENCENDIDA)) == 1,
            intensidad = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_INTENSIDAD)),
            modoControl = modo,
            horaEncendido = cursor.getString(cursor.getColumnIndexOrThrow(COL_SECTOR_HORA_ON)),
            horaApagado = cursor.getString(cursor.getColumnIndexOrThrow(COL_SECTOR_HORA_OFF)),
            activarPorHorario = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_ACT_HORARIO)) == 1,
            activarPorMovimiento = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_ACT_MOV)) == 1,
            activarPorLuzAmbiente = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_ACT_LUZ)) == 1,
            umbralLux = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_UMBRAL_LUX)),
            movimientoDetectado = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_MOV_DETECTADO)) == 1,
            luxActual = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_LUX_ACTUAL)),
            consumoNominalWatts = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTOR_WATTS)),
            alarmaIntrusionPorPir = alarmaPir
        )
    }

    fun agregarSector(
        piso: Piso,
        nombre: String,
        tipoSector: String,
        consumoWatts: Int,
        modoInicial: ModoControl,
        usuario: String = "admin"
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_PISO_ID, piso.id)
            put(COL_SECTOR_NOMBRE, nombre)
            put(COL_SECTOR_TIPO, tipoSector)
            put(COL_SECTOR_ENCENDIDA, 0)
            put(COL_SECTOR_INTENSIDAD, 100)
            put(COL_SECTOR_MODO, modoInicial.name)
            put(COL_SECTOR_HORA_ON, "18:00")
            put(COL_SECTOR_HORA_OFF, "06:00")
            put(COL_SECTOR_ACT_HORARIO, 1)
            put(COL_SECTOR_ACT_MOV, 1)
            put(COL_SECTOR_ACT_LUZ, 1)
            put(COL_SECTOR_UMBRAL_LUX, 150)
            put(COL_SECTOR_MOV_DETECTADO, 0)
            put(COL_SECTOR_LUX_ACTUAL, 300)
            put(COL_SECTOR_WATTS, consumoWatts)
            put(COL_SECTOR_ALARMA_PIR, 1)
        }
        val id = db.insert(TABLE_SECTORES, null, values)
        if (id > 0) {
            registrarEventoInterno(
                db,
                piso.nombre,
                nombre,
                "Sector creado (${consumoWatts}W, modo ${modoInicial.name})",
                "ADMIN",
                usuario
            )
        }
        return id
    }

    fun eliminarSector(sector: SectorLuz, pisoNombre: String, usuario: String = "admin") {
        val db = writableDatabase
        db.delete(TABLE_SECTORES, "$COL_SECTOR_ID = ?", arrayOf(sector.id.toString()))
        registrarEventoInterno(db, pisoNombre, sector.nombre, "Sector eliminado", "ADMIN", usuario)
    }

    /**
     * Acción manual sobre un sector: cambia a modo MANUAL y enciende/apaga la luz.
     */
    fun cambiarEstadoManualSector(
        sector: SectorLuz,
        encender: Boolean,
        pisoNombre: String,
        usuario: String = "Operador"
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_ENCENDIDA, if (encender) 1 else 0)
            put(COL_SECTOR_MODO, ModoControl.MANUAL.name)
        }
        db.update(TABLE_SECTORES, values, "$COL_SECTOR_ID = ?", arrayOf(sector.id.toString()))
        val estadoStr = if (encender) "ENCENDIDA" else "APAGADA"
        registrarEventoInterno(
            db,
            pisoNombre,
            sector.nombre,
            "Luz $estadoStr manualmente (${sector.intensidad}%)",
            "MANUAL",
            usuario
        )
    }

    /**
     * Ajusta la intensidad lumínica (dimmer 10% - 100%) de un sector.
     */
    fun actualizarIntensidadSector(sectorId: Int, nuevaIntensidad: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_INTENSIDAD, nuevaIntensidad.coerceIn(10, 100))
        }
        db.update(TABLE_SECTORES, values, "$COL_SECTOR_ID = ?", arrayOf(sectorId.toString()))
    }

    /**
     * Cambia el modo de un sector entre MANUAL y AUTOMATICO.
     */
    fun cambiarModoControlSector(
        sector: SectorLuz,
        nuevoModo: ModoControl,
        pisoNombre: String,
        usuario: String = "Operador"
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_MODO, nuevoModo.name)
        }
        db.update(TABLE_SECTORES, values, "$COL_SECTOR_ID = ?", arrayOf(sector.id.toString()))
        registrarEventoInterno(
            db,
            pisoNombre,
            sector.nombre,
            "Modo cambiado a ${nuevoModo.name}",
            "CONFIGURACIÓN",
            usuario
        )
    }

    /**
     * Guarda los parámetros de automatización (horarios y reglas de sensores) de un sector.
     */
    fun actualizarConfiguracionAutomatica(
        sectorId: Int,
        pisoNombre: String,
        sectorNombre: String,
        horaEncendido: String,
        horaApagado: String,
        activarPorHorario: Boolean,
        activarPorMovimiento: Boolean,
        activarPorLuzAmbiente: Boolean,
        umbralLux: Int,
        usuario: String = "Operador"
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_HORA_ON, horaEncendido)
            put(COL_SECTOR_HORA_OFF, horaApagado)
            put(COL_SECTOR_ACT_HORARIO, if (activarPorHorario) 1 else 0)
            put(COL_SECTOR_ACT_MOV, if (activarPorMovimiento) 1 else 0)
            put(COL_SECTOR_ACT_LUZ, if (activarPorLuzAmbiente) 1 else 0)
            put(COL_SECTOR_UMBRAL_LUX, umbralLux.coerceIn(10, 1000))
        }
        db.update(TABLE_SECTORES, values, "$COL_SECTOR_ID = ?", arrayOf(sectorId.toString()))
        registrarEventoInterno(
            db,
            pisoNombre,
            sectorNombre,
            "Reglas Auto actualizadas ($horaEncendido-$horaApagado, Umbral ${umbralLux}lx)",
            "CONFIGURACIÓN",
            usuario
        )
    }

    /**
     * Actualiza los valores simulados de los sensores IoT (PIR movimiento y LDR lux) de un sector.
     * Si el Modo Centinela está activo y se detecta movimiento, dispara protocolo de seguridad.
     */
    fun actualizarSensoresSector(
        sector: SectorLuz,
        pisoNombre: String,
        movimientoDetectado: Boolean,
        luxActual: Int
    ) {
        val cfgSeguridad = obtenerConfiguracionSeguridad()
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_MOV_DETECTADO, if (movimientoDetectado) 1 else 0)
            put(COL_SECTOR_LUX_ACTUAL, luxActual.coerceIn(0, 1000))
            // Si el modo Centinela está activo y se detecta presencia PIR, encender luz al 100% por seguridad
            if (cfgSeguridad.modoCentinelaActivo && movimientoDetectado) {
                put(COL_SECTOR_ENCENDIDA, 1)
                put(COL_SECTOR_INTENSIDAD, 100)
            }
        }
        db.update(TABLE_SECTORES, values, "$COL_SECTOR_ID = ?", arrayOf(sector.id.toString()))

        if (cfgSeguridad.modoCentinelaActivo && movimientoDetectado && !sector.movimientoDetectado) {
            registrarAlertaInterna(
                db,
                tipoAlerta = "INTRUSIÓN PIR DETECTADA",
                nivel = "CRÍTICA",
                ubicacion = "$pisoNombre • ${sector.nombre}",
                descripcion = "Movimiento detectado con Modo Centinela activo. Luz encendida al 100% de forma disuasiva."
            )
            registrarEventoInterno(
                db,
                pisoNombre,
                sector.nombre,
                "ALARMA CENTINELA: Luz encendida al 100% por detección PIR en vigilancia",
                "SEGURIDAD PIR",
                "Sensor PIR"
            )
        }
    }

    // ==================== ACCIONES MASIVAS POR PISO Y EDIFICIO ====================

    fun controlarLucesPorPiso(piso: Piso, encender: Boolean, usuario: String = "Operador") {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_ENCENDIDA, if (encender) 1 else 0)
            put(COL_SECTOR_MODO, ModoControl.MANUAL.name)
        }
        val afectados = db.update(
            TABLE_SECTORES,
            values,
            "$COL_SECTOR_PISO_ID = ?",
            arrayOf(piso.id.toString())
        )
        val accion = if (encender) "ENCENDIDAS" else "APAGADAS"
        registrarEventoInterno(
            db,
            piso.nombre,
            "Todos los sectores ($afectados)",
            "Luces del piso $accion en modo MANUAL",
            "MASIVO PISO",
            usuario
        )
    }

    fun cambiarModoPorPiso(piso: Piso, modo: ModoControl, usuario: String = "Operador") {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SECTOR_MODO, modo.name)
        }
        val afectados = db.update(
            TABLE_SECTORES,
            values,
            "$COL_SECTOR_PISO_ID = ?",
            arrayOf(piso.id.toString())
        )
        registrarEventoInterno(
            db,
            piso.nombre,
            "Todos los sectores ($afectados)",
            "Piso completo configurado en modo ${modo.name}",
            "MASIVO PISO",
            usuario
        )
    }

    fun controlarEdificioCompleto(encender: Boolean?, modo: ModoControl, usuario: String = "Operador") {
        val db = writableDatabase
        val values = ContentValues().apply {
            if (encender != null) {
                put(COL_SECTOR_ENCENDIDA, if (encender) 1 else 0)
            }
            put(COL_SECTOR_MODO, modo.name)
        }
        db.update(TABLE_SECTORES, values, null, null)
        val detalle = when {
            modo == ModoControl.AUTOMATICO -> "Todo el edificio activado en modo AUTOMÁTICO IoT"
            encender == true -> "Todas las luces del edificio ENCENDIDAS manualmente"
            else -> "Todas las luces del edificio APAGADAS manualmente"
        }
        registrarEventoInterno(db, "Edificio Completo", "Todos los Sectores", detalle, "CONTROL MAESTRO", usuario)
    }

    fun simularAmbienteGlobal(esNoche: Boolean, usuario: String = "Simulador") {
        val db = writableDatabase
        val luxObjetivo = if (esNoche) 35 else 420
        val values = ContentValues().apply {
            put(COL_SECTOR_LUX_ACTUAL, luxObjetivo)
        }
        db.update(TABLE_SECTORES, values, null, null)
        registrarEventoInterno(
            db,
            "Sensores del Edificio",
            "Todos los sensores LDR",
            if (esNoche) "Simulación ambiental: NOCHE (${luxObjetivo} lx)" else "Simulación ambiental: DÍA (${luxObjetivo} lx)",
            "SIMULADOR IoT",
            usuario
        )
    }

    // ==================== MOTOR DE AUTOMATIZACIÓN IoT ====================

    fun evaluarReglasAutomaticas(horaActualHHMM: String): Int {
        val cfgSeguridad = obtenerConfiguracionSeguridad()
        // Si hay emergencia de evacuación activa, las luces permanecen encendidas al 100%
        if (cfgSeguridad.emergenciaEvacuacionActiva) return 0

        val db = writableDatabase
        val mapaPisos = obtenerPisos().associateBy({ it.id }, { it.nombre })
        val sectoresAuto = obtenerTodosLosSectores().filter { it.modoControl == ModoControl.AUTOMATICO }
        var cambiosRealizados = 0

        for (sector in sectoresAuto) {
            val motivosEncendido = mutableListOf<String>()

            if (sector.activarPorHorario && estaDentroDeHorario(horaActualHHMM, sector.horaEncendido, sector.horaApagado)) {
                motivosEncendido.add("Horario (${sector.horaEncendido}-${sector.horaApagado})")
            }
            if (sector.activarPorMovimiento && sector.movimientoDetectado) {
                motivosEncendido.add("Sensor PIR Movimiento")
            }
            if (sector.activarPorLuzAmbiente && sector.luxActual <= sector.umbralLux) {
                motivosEncendido.add("Sensor Luz (${sector.luxActual}lx ≤ ${sector.umbralLux}lx)")
            }

            val debeEncender = motivosEncendido.isNotEmpty()
            if (sector.encendida != debeEncender) {
                val values = ContentValues().apply {
                    put(COL_SECTOR_ENCENDIDA, if (debeEncender) 1 else 0)
                }
                db.update(TABLE_SECTORES, values, "$COL_SECTOR_ID = ?", arrayOf(sector.id.toString()))
                val nombrePiso = mapaPisos[sector.pisoId] ?: "Piso ${sector.pisoId}"
                val accionTexto = if (debeEncender) {
                    "ENCENDIDA automáticamente por: ${motivosEncendido.joinToString(", ")}"
                } else {
                    "APAGADA automáticamente (sin condiciones activas a las $horaActualHHMM)"
                }
                registrarEventoInterno(db, nombrePiso, sector.nombre, accionTexto, "AUTO IoT", "Motor Auto")
                cambiosRealizados++
            }
        }
        return cambiosRealizados
    }

    private fun estaDentroDeHorario(actual: String, inicio: String, fin: String): Boolean {
        val minActual = convertirAMinutos(actual) ?: return false
        val minInicio = convertirAMinutos(inicio) ?: return false
        val minFin = convertirAMinutos(fin) ?: return false

        return if (minInicio <= minFin) {
            minActual in minInicio..minFin
        } else {
            minActual >= minInicio || minActual <= minFin
        }
    }

    private fun convertirAMinutos(horaHHMM: String): Int? {
        val partes = horaHHMM.trim().split(":")
        if (partes.size != 2) return null
        val h = partes[0].toIntOrNull() ?: return null
        val m = partes[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    // ==================== HISTORIAL Y RESUMEN ====================

    fun obtenerHistorial(limite: Int = 80): List<EventoHistorial> {
        val lista = mutableListOf<EventoHistorial>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_HISTORIAL,
            null,
            null,
            null,
            null,
            null,
            "$COL_HIST_ID DESC",
            limite.toString()
        )
        cursor.use {
            while (it.moveToNext()) {
                val userIdx = it.getColumnIndex(COL_HIST_USUARIO)
                val usuario = if (userIdx >= 0) it.getString(userIdx) else "Sistema"
                lista.add(
                    EventoHistorial(
                        id = it.getInt(it.getColumnIndexOrThrow(COL_HIST_ID)),
                        pisoNombre = it.getString(it.getColumnIndexOrThrow(COL_HIST_PISO)),
                        sectorNombre = it.getString(it.getColumnIndexOrThrow(COL_HIST_SECTOR)),
                        accion = it.getString(it.getColumnIndexOrThrow(COL_HIST_ACCION)),
                        origen = it.getString(it.getColumnIndexOrThrow(COL_HIST_ORIGEN)),
                        usuario = usuario,
                        fechaHora = it.getString(it.getColumnIndexOrThrow(COL_HIST_FECHA))
                    )
                )
            }
        }
        return lista
    }

    fun limpiarHistorial(usuario: String = "admin") {
        val db = writableDatabase
        db.delete(TABLE_HISTORIAL, null, null)
        registrarEventoInterno(db, "Sistema Central", "Bitácora SQLite", "Historial limpiado por $usuario", "ADMIN", usuario)
    }

    fun obtenerResumenEdificio(): ResumenEdificio {
        val pisos = obtenerPisos()
        val sectores = obtenerTodosLosSectores()
        val cfgSeguridad = obtenerConfiguracionSeguridad()
        return ResumenEdificio(
            totalPisos = pisos.size,
            totalSectores = sectores.size,
            sectoresEncendidos = sectores.count { it.encendida },
            sectoresAutomaticos = sectores.count { it.modoControl == ModoControl.AUTOMATICO },
            consumoTotalWatts = sectores.sumOf { it.consumoActualWatts },
            alertasActivas = cfgSeguridad.alertasSinAtender
        )
    }
}
