# The Sync Protocol — Shared Task List

A real-time synchronized task list Android app where two devices share and modify the same list via WebSocket.

## Architecture

**Clean Architecture + MVVM** with a FastAPI WebSocket backend.

```
Presentation (Compose + ViewModel)
      ↓
Domain (Use Cases + Repository Interface)
      ↓
Data (RepositoryImpl + WebSocketManager + DTOs)
      ↓
FastAPI WebSocket Server (Single Source of Truth)
```

**Key principle:** The server is the Single Source of Truth. All mutations go to the server, which broadcasts the result back to all connected clients (including the sender). This eliminates race conditions and conflicting local states.

### Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| State | `StateFlow` / `MutableStateFlow` |
| DI | Koin 3.x |
| WebSocket | OkHttp 4.x (built-in) |
| Serialization | `kotlinx.serialization` |
| Navigation | Navigation Compose (type-safe routes) |
| Server | FastAPI + uvicorn |

### WebSocket Message Flow

```
Device A adds task  → Server broadcasts TASK_ADDED   → Both devices update
Device B toggles    → Server broadcasts TASK_TOGGLED → Both devices update
New device connects → Server sends SYNC_STATE        → Full list restored
Network drops       → Reconnect backoff: 1s/2s/4s/8s/16s
```

---

## Setup

### 1. Start the Server

```bash
cd server
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

WebSocket endpoint: `ws://localhost:8000/ws`

### 2. Configure Android App

The WebSocket URL is in `core/websocket/WebSocketManager.kt`:

```kotlin
const val WS_URL = "ws://10.0.2.2:8000/ws"   // Android emulator default
```

**For a real device:** Replace `10.0.2.2` with your machine's local IP (e.g., `192.168.1.x`).

### 3. Run the App

Open in Android Studio. Run on two emulators or one emulator + one physical device pointing at the same server. Both show the same list in real time.

---

## Project Structure

```
app/src/main/java/com/dimasarya/senserbot/
├── core/
│   ├── common/Result.kt
│   ├── di/AppModule.kt
│   ├── network/RetrofitClient.kt
│   └── websocket/
│       ├── ConnectionState.kt      ← Connecting/Connected/Disconnected/Reconnecting/Error
│       └── WebSocketManager.kt     ← OkHttp WebSocket + exponential backoff reconnect
├── data/
│   ├── remote/dto/
│   │   ├── TaskDto.kt              ← @Serializable + .toDomain()
│   │   └── WsMessageDto.kt         ← Generic message envelope {type, payload}
│   └── repository/
│       └── TaskRepositoryImpl.kt   ← Parses WS messages, exposes StateFlow<List<Task>>
├── domain/
│   ├── model/Task.kt
│   ├── repository/TaskRepository.kt
│   └── usecase/                    ← ObserveTasks, AddTask, ToggleTask, RemoveTask
└── presentation/
    ├── navigation/NavGraph.kt
    └── task/
        ├── TaskUiState.kt
        ├── TaskViewModel.kt
        └── TaskScreen.kt           ← Connection banner + input row + task list
```

---

## Running Tests

```bash
./gradlew test
```

Unit tests cover:
- `TaskRepositoryImplTest` — WebSocket message parsing and outgoing JSON format
- `TaskDtoMappingTest` — DTO serialization and `.toDomain()` field mapping
- `TaskUseCasesTest` — Use case delegation to repository
- `TaskViewModelTest` — State management and user action handling
- `KoinModuleCheckTest` — DI wiring verification for all modules
