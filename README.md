 # Android Clean Architecture Boilerplate

A modern Android boilerplate project demonstrating **Clean Architecture** with **Jetpack Compose**, **Koin** dependency injection, and **Kotlin Coroutines/Flow**.

## Architecture

The project follows a three-layer Clean Architecture pattern:

```
Presentation → Domain → Data
```

| Layer | Responsibility | Key Components |
|-------|---------------|----------------|
| **Presentation** | UI and state management | Composables, ViewModels, UI State |
| **Domain** | Business logic | Use Cases, Models, Repository interfaces |
| **Data** | Data access | Repository implementations, API clients, DTOs |

## Project Structure

```
app/src/main/java/com/dimasarya/boilerplatecode/
├── core/
│   ├── common/         # Result wrapper (Success/Error/Loading)
│   ├── di/             # Koin dependency injection modules
│   └── network/        # Retrofit client configuration
├── data/
│   └── remote/
│       ├── api/        # Retrofit API interfaces
│       ├── dto/        # Data Transfer Objects + mapping extensions
│       └── repository/ # Repository implementations
├── domain/
│   ├── model/          # Domain entities
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
├── presentation/
│   ├── navigation/     # Compose Navigation (type-safe routes)
│   └── user/           # User feature (ViewModel, Screen, UiState)
└── ui/
    └── theme/          # Material 3 theming
```

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** Clean Architecture + MVVM
- **DI:** Koin 3.5.6 (`main`) / Hilt (`feature/hilt`)
- **Networking:** Retrofit 2.11 + OkHttp 4.12
- **Serialization:** Kotlin Serialization
- **Navigation:** Compose Navigation (type-safe)
- **Image Loading:** Coil 2.6
- **Async:** Coroutines + Flow
- **Min SDK:** 24 | **Target SDK:** 35 | **Compile SDK:** 36

## Getting Started

### Prerequisites

- Android Studio Ladybug or newer
- JDK 11+
- Android SDK 36

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/BoilerplateCode.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run on an emulator or device.

## Sample API

The project uses [JSONPlaceholder](https://jsonplaceholder.typicode.com/) as a demo API to fetch and display a list of users.

## Key Patterns

- **Result\<T\> sealed class** for consistent loading/success/error state handling
- **DTO-to-Domain mapping** via extension functions to keep layers decoupled
- **StateFlow-based UI state** for reactive, lifecycle-aware UI updates
- **Callable use cases** using Kotlin `operator fun invoke()`

## Branches

| Branch | DI Framework | Description |
|--------|-------------|-------------|
| `main` | Koin | Default setup using Koin for lightweight DI |
| `feature/hilt` | Hilt | Alternative setup using Hilt (Dagger) for compile-time DI |

Choose the branch that fits your preferred dependency injection approach.

## License

This project is available as open source for learning and reference purposes.