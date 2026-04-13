from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from typing import Any, Optional

app = FastAPI(title="Sync Protocol Server")

connected: set[WebSocket] = set()
tasks: list[dict] = []


async def broadcast(message: dict[str, Any], exclude: Optional[WebSocket] = None) -> None:
    disconnected = set()
    for client in connected:
        if client is exclude:
            continue
        try:
            await client.send_json(message)
        except Exception:
            disconnected.add(client)
    connected.difference_update(disconnected)


@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket) -> None:
    await websocket.accept()
    connected.add(websocket)
    print(f"Client connected. Total: {len(connected)}")

    await websocket.send_json({"type": "SYNC_STATE", "payload": {"tasks": tasks}})

    try:
        while True:
            data = await websocket.receive_json()
            msg_type: str = data.get("type", "")
            payload: dict = data.get("payload", {})

            if msg_type == "ADD_TASK":
                task = {
                    "id": payload["id"],
                    "title": payload["title"],
                    "is_completed": payload.get("is_completed", False),
                    "created_at": payload["created_at"],
                }
                tasks.append(task)
                await broadcast({"type": "TASK_ADDED", "payload": task})

            elif msg_type == "TOGGLE_TASK":
                task_id = payload["id"]
                for task in tasks:
                    if task["id"] == task_id:
                        task["is_completed"] = not task["is_completed"]
                        await broadcast({
                            "type": "TASK_TOGGLED",
                            "payload": {"id": task_id, "is_completed": task["is_completed"]},
                        })
                        break
                else:
                    await websocket.send_json({"type": "ERROR", "payload": {"message": "Task not found"}})

            elif msg_type == "REMOVE_TASK":
                task_id = payload["id"]
                before = len(tasks)
                tasks[:] = [t for t in tasks if t["id"] != task_id]
                if len(tasks) < before:
                    await broadcast({"type": "TASK_REMOVED", "payload": {"id": task_id}})
                else:
                    await websocket.send_json({"type": "ERROR", "payload": {"message": "Task not found"}})

            else:
                await websocket.send_json({"type": "ERROR", "payload": {"message": f"Unknown type: {msg_type}"}})

    except WebSocketDisconnect:
        connected.discard(websocket)
        print(f"Client disconnected. Total: {len(connected)}")
