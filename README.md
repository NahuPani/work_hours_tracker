# ⏱️ Control de Horas y Ganancias (Android)

Una aplicación nativa para Android construida con **Kotlin**, **Jetpack Compose** y **Material 3** para registrar tus horas laborales (entrada y salida), configurar cuánto ganas por hora y calcular automáticamente tus ingresos acumulados (diarios, semanales y mensuales).

---

## 🌟 Características Principales

1. **Configuración de Tarifa por Hora y Moneda**:
   - Ajusta tu valor por hora en cualquier momento (ej: `$2,500` / `$15.00`).
   - Elige el símbolo de moneda (`$`, `€`, `USD`, `ARS`, etc.).
2. **Registro Directo y Persistente (Borrador Protegido)**:
   - Selección ágil de hora de Entrada y Salida con selectores nativos o botón "Ahora".
   - Soporte nativo para **turnos nocturnos** (ej: 22:00 a 06:00 del día siguiente).
   - Los datos en pantalla nunca se pierden al cambiar de pestaña en la aplicación.
   - **Jornadas limpias sin deducción de descansos**: pensado para trabajos donde los descansos son 100% pagos.
   - **Horas extras automáticas y cálculo por día**: si trabajas en **turnos partidos** (ej: mañana y tarde el mismo día), las horas diarias se acumulan correctamente antes de calcular horas extras.
   - Cálculo instantáneo de horas trabajadas y dinero acumulado a cobrar en tiempo real con vibración háptica.
3. **Ciclo Laboral Personalizado (Miércoles a Martes)**:
   - **Hoy**: Total de dinero ganado y horas trabajadas en el día actual.
   - **Esta Semana**: Acumulado exacto de tu ciclo laboral (por defecto de **Miércoles a Martes**, configurable desde Ajustes).
   - **Gráfico de Barras Semanal**: Visualización interactiva de tu rendimiento día a día (Mié, Jue, Vie, Sáb, Dom, Lun, Mar).
   - **Este Mes**: Acumulado del mes calendario.
4. **Historial Completo con Edición y Reportes**:
   - **Edición completa**: toca cualquier turno del historial para modificar fecha, horarios o tarifa si cometiste un error.
   - Filtros avanzados: *Todos*, *Esta Semana (Mié-Mar)*, *Este Mes*, *1ª Quincena (1-15)*, *2ª Quincena (16-fin)* y *Mes Pasado*.
   - **Compartir por WhatsApp**: genera un resumen formateado listo para enviar a tu empleador o cliente.
   - **Exportar a CSV (Excel)**: descarga o comparte un archivo tabular con todas las jornadas desglosadas.
   - Eliminación de registros con diálogo de confirmación.
5. **Copia de Seguridad Inteligente (Sin Duplicados)**:
   - Exporta todos tus datos y configuraciones a un archivo JSON.
   - Al restaurar, fusiona de forma segura **omitiendo turnos duplicados** o permite reemplazar todo limpiamente.
6. **Buscador de Actualizaciones Integrado**:
   - Consulta automáticamente a GitHub si hay una nueva versión disponible y permite descargarla directamente.
7. **Privacidad y Seguridad de Base de Datos**:
   - Base de datos local **Room (SQLite)** con migraciones seguras y exportación de esquemas (nunca borra tus datos en actualizaciones). 100% offline y privado.

---

## 📁 Estructura del Proyecto

```text
work_hours_tracker/
├── app/
│   ├── build.gradle.kts                # Dependencias (Compose, Room, Navigation, KSP)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                        # Iconos adaptativos, colores y temas
│       └── java/com/example/workhourstracker/
│           ├── MainActivity.kt         # Punto de entrada y barra de navegación inferior
│           ├── data/
│           │   ├── WorkShift.kt        # Entidad de la BD con horas extras y descansos
│           │   ├── WorkShiftDao.kt     # Consultas SQL / Room DAO con operaciones masivas
│           │   ├── AppDatabase.kt      # Singleton de la base de datos Room v2
│           │   └── UserPreferences.kt  # Almacenamiento local de tarifas, horas extras y descansos
│           ├── ui/
│           │   ├── theme/              # Paleta de colores, tipografía y tema Material 3
│           │   ├── components/
│           │   │   ├── DirectShiftCard.kt      # Carga directa de horas, descanso, extras y háptico
│           │   │   ├── EarningsSummaryCard.kt  # Resumen de ganancias Hoy / Semana / Mes
│           │   │   ├── HourlyRateDialog.kt     # Diálogo para cambiar el valor de la hora
│           │   │   ├── EditShiftDialog.kt      # Diálogo para editar turnos existentes
│           │   │   ├── OvertimeDialog.kt       # Diálogo para configurar horas extras y recargo
│           │   │   ├── RestoreDialog.kt        # Diálogo para restaurar backup JSON
│           │   │   └── ManualShiftDialog.kt    # Diálogo para cargar turnos pasados desde FAB
│           │   └── screens/
│           │       ├── HomeScreen.kt           # Pantalla principal de registro
│           │       ├── HistoryScreen.kt        # Pantalla de historial con edición y compartir
│           │       └── SettingsScreen.kt       # Configuración, backup/restore y estadísticas
│           └── viewmodel/
│               └── WorkHoursViewModel.kt       # Lógica reactiva, horas extras, backup y reportes
```├── build.gradle.kts                    # Plugins del proyecto (Gradle, Kotlin, KSP)
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 Cómo abrir y compilar en Android Studio

### Requisitos Previos:
- **Android Studio** (Recomendado: Iguana, Jellyfish, Koala o superior).
  - Si aún no lo tienes instalado, descárgalo gratis de: [developer.android.com/studio](https://developer.android.com/studio).
- **JDK 17** (Viene incluido directamente con Android Studio).

### Pasos:
1. Abre **Android Studio**.
2. Selecciona **Open** (Abrir) y busca la carpeta del proyecto:
   `C:\Users\Nahu\.gemini\antigravity-ide\scratch\work_hours_tracker`
3. Espera a que Gradle termine de sincronizar automáticamente las dependencias.
4. Conecta tu teléfono Android mediante cable USB (con *Depuración por USB* activada) o inicia un Emulador desde el *Device Manager*.
5. Presiona el botón verde de **Run** ▶️ (`Shift + F10`). ¡La aplicación se instalará y abrirá en tu dispositivo!

---

## 📦 Cómo generar un archivo APK para instalar en tu celular

Si quieres instalar la app directamente en tu teléfono sin mantenerlo conectado:
1. En Android Studio, ve al menú superior: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Android Studio compilará el proyecto y mostrará una notificación en la esquina inferior derecha con un enlace: **locate**.
3. El archivo generado estará en:
   `app/build/outputs/apk/debug/app-debug.apk`
4. Pasa ese archivo `.apk` a tu celular (por WhatsApp, Google Drive, cable USB, Telegram, etc.) y pulsa sobre él para instalarlo.
