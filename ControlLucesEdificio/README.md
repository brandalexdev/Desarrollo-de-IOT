# SmartBuilding Luces IoT - Control de Iluminación y Seguridad por Piso y Sector (Android Kotlin + SQLite)

Aplicación nativa para Android desarrollada en **Kotlin** con **Jetpack Compose (Material 3)** y persistencia local en **SQLite (`SQLiteOpenHelper`)** para gestionar el encendido y apagado **manual y automático** de las luces de un edificio divididas por **pisos** y **sectores**, incluyendo **módulos completos de Seguridad IoT**.

---

## Secciones de Seguridad Incluidas

1. **Autenticación y Control de Acceso Basado en Roles (RBAC) en SQLite (`usuarios`)**:
   - Contraseñas cifradas mediante hash criptográfico **SHA-256** (`MessageDigest.getInstance("SHA-256")`).
   - Tres perfiles precargados en SQLite listos para usar desde la pantalla de Login:
     - **Administrador (`admin` / `admin123` • PIN `1234`)**: Control total del edificio, alta/baja de pisos, sectores y usuarios, y gestión de protocolos de seguridad.
     - **Guardia / Operador (`guardia` / `guardia123` • PIN `2580`)**: Control de iluminación manual/automático y atención de alarmas de seguridad.
     - **Auditor / Invitado (`auditor` / `123456` • PIN `0000`)**: Acceso de solo lectura para supervisión energética y bitácora sin permiso para alterar estados.
   - Registro automático de intentos de acceso exitosos y fallidos en la bitácora y en alertas de seguridad.

2. **Pestaña Dedicada de Seguridad del Edificio (`SecurityScreens.kt`)**:
   - **Protocolo de Emergencia / Evacuación (100% ON + Bloqueo)**: Enciende de inmediato todos los pisos y sectores del edificio al 100% de intensidad y bloquea el apagado manual hasta que se autorice su desactivación con **PIN de Seguridad**.
   - **Modo Centinela / Alarma por Intrusión PIR**: Cuando está activo y el sensor PIR de cualquier sector detecta movimiento, enciende automáticamente la luz del sector al 100% (modo disuasivo) y genera una **Alerta Crítica de Intrusión** en SQLite.
   - **Simulación de Presencia Nocturna (Antirrobo)**: Activa un patrón intercalado de iluminación en accesos, pasillos y oficinas para simular ocupación.
   - **Bloqueo de Panel de Controles Manuales con PIN**: Permite bloquear los interruptores manuales de la aplicación y exige el PIN de seguridad (`1234`) para desbloquearlos.
   - **Centro de Alertas de Seguridad (`alertas_seguridad`)**: Historial de alertas críticas (intrusiones PIR, emergencias, accesos fallidos) con verificación por el operador.
   - **Gestión de Usuarios en SQLite**: Alta y baja de usuarios con asignación de rol y PIN.

---

## Estructura de la Base de Datos SQLite (`control_luces_edificio_iot.db`)

Implementada de forma nativa con `SQLiteOpenHelper` en `DatabaseHelper.kt` con **6 tablas**:
1. **`pisos`**: `id`, `numero`, `nombre`, `descripcion`.
2. **`sectores`**: `id`, `piso_id`, `nombre`, `tipo_sector`, `encendida`, `intensidad`, `modo_control`, `hora_encendido`, `hora_apagado`, `activar_por_horario`, `activar_por_movimiento`, `activar_por_luz`, `umbral_lux`, `movimiento_detectado`, `lux_actual`, `consumo_watts`, `alarma_intrusion_pir`.
3. **`historial_eventos`**: `id`, `piso_nombre`, `sector_nombre`, `accion`, `origen`, `usuario`, `fecha_hora`.
4. **`usuarios`**: `id`, `username`, `password_hash` (SHA-256), `nombre_completo`, `rol`, `pin_seguridad`, `activo`, `ultimo_acceso`.
5. **`configuracion_seguridad`**: `id`, `modo_centinela`, `emergencia_evacuacion`, `simulacion_presencia`, `bloqueo_controles`, `pin_maestro`.
6. **`alertas_seguridad`**: `id`, `tipo_alerta`, `nivel`, `ubicacion`, `descripcion`, `fecha_hora`, `atendida`.
