# ⏱️ Control de Horas y Ganancias (Android)

Una aplicación nativa para Android construida con **Kotlin**, **Jetpack Compose** y **Material 3** para registrar tus horas laborales (entrada y salida), configurar cuánto ganas por hora y calcular automáticamente tus ingresos acumulados (diarios, semanales y mensuales).

---

## 🌟 Características Principales

1. **Configuración de Tarifa por Hora y Moneda**:
   - Ajusta tu valor por hora en cualquier momento (ej: `$2,500` / `$15.00`).
   - Elige el símbolo de moneda (`$`, `€`, `USD`, `ARS`, etc.).
2. **Registro Directo de Entrada y Salida**:
   - Selección ágil de hora de Entrada y Salida mediante selectores de hora nativos o botón "Ahora" de un solo toque.
   - Selector de fecha ("Hoy" por defecto, con opción de elegir fechas pasadas con calendario).
   - Cálculo instantáneo de horas trabajadas y dinero acumulado a cobrar en tiempo real.
   - Detección inteligente de turnos nocturnos que cruzan la medianoche.
   - Opción para añadir una nota al turno (ej. "Turno tarde, horas extra").
3. **Cálculo Automático de Ingresos**:
   - **Hoy**: Total de dinero ganado y horas trabajadas en el día actual.
   - **Esta Semana**: Acumulado de la semana en curso (lunes a domingo).
   - **Este Mes**: Acumulado del mes calendario.
4. **Historial Completo de Jornadas**:
   - Filtros por: *Todos*, *Esta Semana* y *Este Mes*.
   - Resumen totalizador de horas e ingresos según el filtro seleccionado.
   - Tarjetas detalladas por turno: fecha, rango horario, duración exacta, tarifa aplicada y ganancia final.
   - Botón de eliminación de registros con confirmación.
5. **Privacidad y Rendimiento 100% Offline**:
   - Base de datos local **Room (SQLite)**: no requiere internet ni envía tus datos a servidores externos.

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
│           │   ├── WorkShift.kt        # Entidad de la base de datos Room
│           │   ├── WorkShiftDao.kt     # Consultas SQL / Room DAO
│           │   ├── AppDatabase.kt      # Singleton de la base de datos Room
│           │   └── UserPreferences.kt  # Almacenamiento local de tarifas
│           ├── ui/
│           │   ├── theme/              # Paleta de colores, tipografía y tema Material 3
│           │   ├── components/
│           │   │   ├── DirectShiftCard.kt      # Tarjeta interactiva de horas entrada/salida y cálculo
│           │   │   ├── EarningsSummaryCard.kt  # Resumen de ganancias Hoy / Semana / Mes
│           │   │   ├── HourlyRateDialog.kt     # Diálogo para cambiar el valor de la hora
│           │   │   └── ManualShiftDialog.kt    # Diálogo para cargar turnos pasados desde historial
│           │   └── screens/
│           │       ├── HomeScreen.kt           # Pantalla principal con registro directo
│           │       ├── HistoryScreen.kt        # Pantalla de historial con filtros
│           │       └── SettingsScreen.kt       # Configuración y estadísticas históricas
│           └── viewmodel/
│               └── WorkHoursViewModel.kt       # Lógica reactiva de cálculo de ingresos y estados
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
