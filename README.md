
<h1 align="center">
  <br>
  <img src="docs/imgs/logo.png" alt="" width="150">
  <br>
  <br>TASA - Theater Auto Silence App<br>
</h1>

&nbsp;

<h4 align="center">Mobile app that silences your phone based on location or scheduled events.</h4>
<space></space>
TASA – Theater Auto Silence App is an Android mobile application developed to manage automatic silencing of the device based on user-defined conditions such as geographic location or scheduled calendar events.

The application triggers silencing based on predefined rules, either when the user enters specific locations or during scheduled events such as meetings, lectures, or cultural activities.

---

## Functionalities

### User Authentication
- User login and registration.

### 📍 Location-Based Automation
- Create geofenced zones (e.g., home, school, gym).
- Automatically triggers DND mode when the user enters the location.

### 🛑 Do Not Disturb Management
- Mutes the device when entering a geofence.
- Automatically restores sound when exiting.

### 🧠 Activity Recognition
- Detects if the user is walking, biking, or driving using Google’s Activity API.

### 🌐 Rule Management
- Add, update, or delete rules for geofences.
- Manage geofence radius, location name, and time ranges.


## ⚙️ Background Execution

Tasa aims for low-power execution:

- The app **does not run continuously in the background**.
- It is only activated by the **Geofencing API** when a transition is detected.
- On **entry**, it starts a **foreground service** to monitor real-time location.
- On **exit**, it **stops the foreground service**, conserving resources.
- If location is disabled or permissions are missing, Tasa notifies the user appropriately.

---

> ⚠️ The app notifies the user when location is off and rules may not be applied.

---

## Technologies & Tools

- [Kotlin](https://kotlinlang.org/)
- [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Compose](https://developer.android.com/develop/ui/compose?hl=en)
- [Ktor client](https://ktor.io/docs/client-create-and-configure.html)
- [PostgreSQL](https://www.postgresql.org/)
- [Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3?hl=en)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel?hl=en)
- [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow?hl=en)
- [Repository Pattern](https://developer.android.com/topic/architecture/data-layer?hl=en)
- [Preferences Datastore](https://developer.android.com/topic/libraries/architecture/datastore)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [osmdroid](https://github.com/osmdroid/osmdroid)
- [AlarmManager](https://developer.android.com/reference/android/app/AlarmManager)
- [Notification Manager](https://developer.android.com/reference/android/app/NotificationManager)
- [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver)
- [FusedLocationProviderClient](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient.html)
- [Geofence](https://developer.android.com/develop/sensors-and-location/location/geofencing)
- [Foreground Services](https://developer.android.com/develop/background-work/services/fgs)

---

## Project organization

### `/docs` - Documentation and Assets

Contains project documentation, diagrams, and related materials:

- **`imgs/`** - Image assets.

### `/jvm` - Backend Application

  The JVM directory contains the backend implementation, organized using a modular architecture:

- **`host/`** - Application hosting and infrastructure configuration.
- **`domain/`** - Domain.
- **`service/`** - Application services and business logic.
- **`http-api/`** - HTTP API endpoints and controllers.
- **`repository/`** - Data access layer abstractions.
- **`http-pipeline/`** - HTTP request/response processing pipeline.
- **`repository-jdbi/`** - JDBI-specific repository implementations.

##### API documentation available [here](https://github.com/GoncaloRibeiro6533/TASA/blob/main/docs/api-docs.yaml)

### `/frontend` - Client Application

Contains the Android app implementation and UI components.

#### 📂 `/app/src`

Main application source code and tests.

- **`/main/java/com/tasa`** — Root package.
  - **`/ui`** — Jetpack Compose screens and UI logic.
    - **`/screens`** — Organized per feature (e.g. `homepage`, `map`).
    - **`/components`** — Reusable composables (e.g. dialogs, buttons, layout containers).
  - **`/activity`** — Activity Recognition manager & broadcast receivers.
  - **`/location`** — Location tracking, geofencing, and foreground service.
  - **`/geofence`** — Geofence manager and broadcast handler.
  - **`/silence`** — Do Not Disturb (DND) logic and system audio control.
  - **`/utils`** — Utility classes (e.g. permissions, constants).
  - **`/domain`** — Business models like `Rule`, `Location`, etc.
  - **`/repository`** — Repositories.
  - **`/storage`** - Local Room database data access objects and entities.
  - **`/service`** - Service to comunicate wiht API.
  - **`/workers`** - Schedule work via WorkManager.
  - **`/alarm`** - Alarm scheduler via AlarmManager.


- **`/res`** — Resources.
  - **`/drawable`**, **`/layout`**, **`/xml`**, **`/values`**, etc.
  - Includes map icons, vector assets, themes, and translations.

- **`AndroidManifest.xml`** — Declares permissions, services, receivers, and activities.

### Deployment

The deployment guide for the Tasa api is described in
the [deployment guide](https://github.com/GoncaloRibeiro6533/TASA/blob/main/docs/deploy.md).

---

## Developers

- Gonçalo Ribeiro
- João Marques

### Supervisors

- Artur Ferreira

@ISEL<br>
Bachelor in Computer Science and Computer Engineering<br>
Project and Seminary - Group 23<br>
Summer Semester of 2024/2025
