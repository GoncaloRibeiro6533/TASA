
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

- **User Authentication**:
  - Login, registration, and secure session management.
  - Token-based authentication for session persistence.

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


### `/frontend` - Client Application
Contains the frontend implementation and user interface components.

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
