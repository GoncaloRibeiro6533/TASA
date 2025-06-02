
<h1 align="center">
  <br>
  <img src="docs/imgs/logo.png" alt="" width="200">
  <br>TASA - Theater Auto Silence App<br>
</h1>

<h4 align="center">Mobile app that silences your phone based on location or scheduled events.</h4>

TASA – Theater Auto Silence App is an Android mobile application developed to manage automatic silencing of the device based on user-defined conditions such as geographic location or scheduled calendar events.

The application supports multiple modes of operation. In location-based mode, the device is silenced upon entering predefined areas. In event-based mode, silencing is triggered when the user is participating in specific calendar events, such as meetings, lectures, or cultural activities.

It integrates with system services such as the AlarmManager and NotificationManager to ensure reliable and context-aware sound profile control.

This approach enhances user experience by reducing interruptions in contexts where silence is expected, while maintaining compliance with Android system policies and respecting user permissions.

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
- [Material3](https://developer.android.com/jetpack/androidx/releases/compose-material3?hl=en)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel?hl=en)
- [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow?hl=en)
- [Repository Pattern](https://developer.android.com/topic/architecture/data-layer?hl=en)
- [Ktor client](https://ktor.io/docs/client-create-and-configure.html)
- [Preferences Datastore](https://developer.android.com/topic/libraries/architecture/datastore)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [osmdroid](https://github.com/osmdroid/osmdroid)
- [AlarmManager](https://developer.android.com/reference/android/app/AlarmManager)
- [Notification Manager](https://developer.android.com/reference/android/app/NotificationManager)
- [BroadcastReceiver](https://developer.android.com/reference/android/content/BroadcastReceiver)

---

## Project organization

### `/jvm` - Backend Application

The JVM directory contains the backend implementation, organized using a modular architecture:

- **`host/`** - Application hosting and infrastructure configuration.
- **`domain/`** - Domain.
- **`service/`** - Application services and business logic.
- **`http-api/`** - HTTP API endpoints and controllers.
- **`repository/`** - Data access layer abstractions.
- **`http-pipeline/`** - HTTP request/response processing pipeline.
- **`repository-jdbi/`** - JDBI-specific repository implementations.

### `/docs` - Documentation and Assets

Contains project documentation, diagrams, and related materials:

- **`imgs/`** - Image assets including project logo
- `logo.svg` - Vector format project logo
- `deploy.md` - Deployment instructions
- `*.pdf` - Project reports, presentations, and documentation
- `er.drawio.svg` - Entity-relationship diagram
- `Endpoints.postman_collection.json` - API testing collection

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
