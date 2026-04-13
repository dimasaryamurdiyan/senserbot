# AI Usage Disclosure

This document describes how AI (Claude Code) was used during the development of this project.

---

## 1. Prompt Strategy

AI was used as a **collaborative architect and code generator**, not as a black box. The workflow:

1. **Architecture briefing first** — Before any code was written, I had the AI produce a complete architecture brief covering: layer structure, message protocol (full JSON contracts), connection lifecycle, file list, and commit plan. This gave me full visibility before implementation.

2. **Plan approval** — The AI entered "plan mode" and I reviewed/iterated on the plan (added unit test plan, commit strategy, JSON contract details) before approving. No code was written until the plan was finalized.

3. **Incremental commits** — I directed AI to follow a 15-commit plan (one logical unit per commit) instead of generating everything at once. This kept each change reviewable.

4. **Scaffolding infrastructure** — AI was used to scaffold `WebSocketManager`, `TaskRepositoryImpl`, and the Koin DI wiring, which involve boilerplate-heavy but architecturally critical code.

5. **Test generation** — AI generated unit tests mirroring the existing test style (MockK + Turbine + UnconfinedTestDispatcher). I reviewed each test to confirm it tested meaningful behavior.

---

## 2. The Correction Log

### Issue: Dead code in `addTask` method

**What happened:** The first version of `TaskRepositoryImpl.addTask()` contained a dead `buildMap` call that was never used — a remnant from drafting the message-building approach:

```kotlin
// Generated (incorrect) — dead code
override suspend fun addTask(title: String) {
    val payload = buildMap {          // ← this map was built but never used
        put("id", "\"${UUID.randomUUID()}\"")
        ...
    }
    val message = """{"type":"ADD_TASK",...}"""  // built separately anyway
    wsManager.send(message)
}
```

**How I identified it:** Code review after generation — the `payload` variable was assigned but never referenced.

**Fix:** Removed the unused `buildMap` block entirely, keeping only the `UUID` + timestamp locals that were actually used in the message string.

```kotlin
// Fixed
override suspend fun addTask(title: String) {
    val id = UUID.randomUUID().toString()
    val createdAt = System.currentTimeMillis()
    val message = """{"type":"ADD_TASK","payload":{"id":"$id","title":${Json.encodeToString(title)},"is_completed":false,"created_at":$createdAt}}"""
    withContext(Dispatchers.IO) { wsManager.send(message) }
}
```

### Issue: Python 3.9 incompatible union type syntax in `server/main.py`

**What happened:** The generated server code used the `X | Y` union syntax for type hints:

```python
# Generated (incorrect for Python 3.9)
async def broadcast(message: dict[str, Any], exclude: WebSocket | None = None) -> None:
```

**How I identified it:** Runtime crash on startup — uvicorn's subprocess threw `TypeError: unsupported operand type(s) for |: 'ABCMeta' and 'NoneType'`. The `X | Y` union syntax for type hints was only introduced in Python 3.10 (PEP 604). The machine runs Python 3.9 (bundled with Xcode).

**Fix:** Replaced with `Optional[WebSocket]` from `typing`, which is compatible with Python 3.7+:

```python
# Fixed
from typing import Any, Optional

async def broadcast(message: dict[str, Any], exclude: Optional[WebSocket] = None) -> None:
```

---

## 3. Self Code Review: `WebSocketManager`

The `WebSocketManager` (`core/websocket/WebSocketManager.kt`) is the most critical infrastructure component. Here is a production-readiness assessment:

### What it does well

- **Separation of concerns:** The manager handles only connection lifecycle and message passing. It does not parse business logic — that lives in `TaskRepositoryImpl`.
- **Backoff reconnect:** Implements `1s/2s/4s/8s/16s` delays before retrying, preventing server flooding on sustained outages.
- **Flow-based messaging:** Uses `MutableSharedFlow` so multiple collectors can observe the same stream without coupling.
- **User-controlled disconnect:** The `userDisconnected` flag prevents the reconnect loop from firing when the user intentionally closes the connection.

### What would need improvement for production

1. **No retry cap:** The backoff loop iterates through the delay list once then stops. A production implementation would loop the last delay indefinitely until connected or explicitly cancelled.

2. **No connection state on `onMessage` failure:** If `_messages.emit(text)` fails (backpressure), it is silently dropped. Production code should log this or use a bounded buffer with a drop-latest strategy.

3. **`CoroutineScope` lifecycle:** The manager uses a `SupervisorJob` + `IO` scope that lives as long as the Koin singleton. In production, this should be tied to the application lifecycle or be explicitly cancelled on app destruction.

4. **Hardcoded URL:** `WS_URL` is a compile-time constant. Production apps should read this from `BuildConfig` or a remote config source to support different environments (dev/staging/prod).

5. **No TLS:** The URL uses `ws://` (plain WebSocket). Production must use `wss://` (WebSocket Secure) to prevent MITM attacks.
