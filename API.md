# BetterManual Remote Control API

BetterManual exposes an HTTP REST API on port **8080** that allows remote control of camera settings over Wi-Fi. It is designed to complement and extend the limited API provided by Sony's built-in Smart Remote app.

## Connection

When the app starts, the camera's IP address is briefly shown on screen:

```
API: 192.168.1.10:8080
```

Connect any HTTP client (curl, browser, script) to the same Wi-Fi network as the camera. All responses are JSON. All POST requests use `application/x-www-form-urlencoded` body parameters.

CORS headers are included on every response, so the API can be called directly from a browser page.

---

## General conventions

- **GET** requests read the current state. Values are read from a cache kept in sync by camera event listeners — they reflect the actual camera state.
- **POST** requests send a command and return `{"status": "ok"}` immediately. Commands are dispatched to the camera asynchronously. Poll the corresponding GET endpoint to confirm the new value.
- Errors return HTTP 400 with `{"error": "<reason>"}`.

---

## Endpoints

### GET /api/state

Returns all current camera settings in a single response. Useful for an initial state sync or building a dashboard.

**Response**

```json
{
  "shutter":  { "n": 1, "d": 100, "formatted": "1/100" },
  "iso":      400,
  "aperture": { "available": true, "value": 180, "formatted": "f1.8" },
  "ev":       { "value": 0, "min": -3, "max": 3, "step": 0.3333 },
  "mode":     "manual",
  "drive":    "single",
  "supported_isos": [0, 100, 200, 400, 800, 1600, 3200, 6400]
}
```

---

### GET /api/shutter

Returns the current shutter speed.

**Response**

```json
{ "n": 1, "d": 100, "formatted": "1/100" }
```

`n/d` is the numerator/denominator fraction. `formatted` is a human-readable string matching the camera's display (e.g. `"1/100"`, `"0.6\""`, `"30\""`, `"BULB"`).

---

### POST /api/shutter

Sets the shutter speed to an absolute value. The `n` and `d` values must exactly match an entry in the supported shutter speed table (1/4000 through 30").

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `n` | integer | Numerator |
| `d` | integer | Denominator |

**Examples**

```bash
# 1/100s
curl -X POST http://camera:8080/api/shutter -d "n=1&d=100"

# 1/1000s
curl -X POST http://camera:8080/api/shutter -d "n=1&d=1000"

# 2 seconds
curl -X POST http://camera:8080/api/shutter -d "n=2&d=1"

# 30 seconds
curl -X POST http://camera:8080/api/shutter -d "n=30&d=1"
```

**Supported shutter speeds** (n/d pairs)

1/4000, 1/3200, 1/2500, 1/2000, 1/1600, 1/1250, 1/1000, 1/800, 1/640, 1/500, 1/400, 1/320, 1/250, 1/200, 1/160, 1/125, 1/100, 1/80, 1/60, 1/50, 1/40, 1/30, 1/25, 1/20, 1/15, 1/13, 1/10, 1/8, 1/6, 1/5, 1/4, 1/3, 10/25, 1/2, 10/16, 4/5, 1/1, 13/10, 16/10, 2/1, 25/10, 16/5, 4/1, 5/1, 6/1, 8/1, 10/1, 13/1, 15/1, 20/1, 25/1, 30/1

---

### POST /api/shutter/increment

Steps the shutter speed one stop faster (shorter exposure).

```bash
curl -X POST http://camera:8080/api/shutter/increment
```

---

### POST /api/shutter/decrement

Steps the shutter speed one stop slower (longer exposure).

```bash
curl -X POST http://camera:8080/api/shutter/decrement
```

---

### GET /api/iso

Returns the current ISO and the list of supported values.

**Response**

```json
{
  "value": 400,
  "supported": [0, 100, 200, 400, 800, 1600, 3200, 6400]
}
```

`value` of `0` means Auto ISO is active.

---

### POST /api/iso

Sets the ISO. Use `value=0` to enable Auto ISO.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `value` | integer | ISO value, or `0` for auto |

**Examples**

```bash
curl -X POST http://camera:8080/api/iso -d "value=800"
curl -X POST http://camera:8080/api/iso -d "value=0"    # auto ISO
```

---

### GET /api/aperture

Returns the current aperture. `available` is `false` if the mounted lens has no electronic aperture control (e.g. manual/adapted lenses).

**Response**

```json
{ "available": true, "value": 180, "formatted": "f1.8" }
```

`value` is the raw integer from the camera (divide by 100 for the f-number).

---

### POST /api/aperture/increment

Opens the aperture one step (lower f-number).

```bash
curl -X POST http://camera:8080/api/aperture/increment
```

---

### POST /api/aperture/decrement

Closes the aperture one step (higher f-number).

```bash
curl -X POST http://camera:8080/api/aperture/decrement
```

---

### GET /api/ev

Returns the current exposure compensation setting.

**Response**

```json
{ "value": 0, "min": -3, "max": 3, "step": 0.3333 }
```

`value` is in integer steps. Multiply by `step` for the EV value in stops (e.g. `value=3, step=0.3333` → +1.0 EV).

---

### POST /api/ev

Sets the exposure compensation.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `value` | integer | Steps of exposure compensation (must be within `min`/`max`) |

**Examples**

```bash
curl -X POST http://camera:8080/api/ev -d "value=3"    # +1.0 EV
curl -X POST http://camera:8080/api/ev -d "value=-3"   # -1.0 EV
curl -X POST http://camera:8080/api/ev -d "value=0"    # reset
```

---

### GET /api/mode

Returns the current shooting mode.

**Response**

```json
{ "mode": "manual" }
```

| Value | Description |
|-------|-------------|
| `manual` | Full manual (M) |
| `aperture` | Aperture priority (A) |
| `shutter` | Shutter priority (S) |

---

### POST /api/mode

Sets the shooting mode.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `mode` | string | `manual`, `aperture`, or `shutter` |

**Examples**

```bash
curl -X POST http://camera:8080/api/mode -d "mode=manual"
curl -X POST http://camera:8080/api/mode -d "mode=aperture"
curl -X POST http://camera:8080/api/mode -d "mode=shutter"
```

---

### GET /api/drive

Returns the current drive mode.

**Response**

```json
{ "mode": "single" }
```

| Value | Description |
|-------|-------------|
| `single` | Single shot |
| `burst_high` | High speed burst |
| `burst_low` | Low speed burst |

---

### POST /api/drive

Sets the drive mode.

**Parameters**

| Name | Type | Description |
|------|------|-------------|
| `mode` | string | `single`, `burst_high`, or `burst_low` |

**Examples**

```bash
curl -X POST http://camera:8080/api/drive -d "mode=single"
curl -X POST http://camera:8080/api/drive -d "mode=burst_high"
```

---

### POST /api/capture

Triggers the shutter. The camera stops preview, takes the photo, then automatically restores preview. Will not fire if a timelapse or bracketing sequence is already in progress.

```bash
curl -X POST http://camera:8080/api/capture
```

**Response**

```json
{ "status": "ok" }
```

---

## Example: automated exposure bracketing script

```bash
BASE="http://192.168.1.10:8080"

# Set up: manual mode, ISO 400, 1/100s base exposure
curl -s -X POST $BASE/api/mode -d "mode=manual"
curl -s -X POST $BASE/api/iso  -d "value=400"
curl -s -X POST $BASE/api/shutter -d "n=1&d=100"

# Take three shots at -1, 0, +1 EV
for ev in -3 0 3; do
  curl -s -X POST $BASE/api/ev -d "value=$ev"
  sleep 0.5
  curl -s -X POST $BASE/api/capture
  sleep 2
done

# Reset EV
curl -s -X POST $BASE/api/ev -d "value=0"
```

---

## Notes

- The API server starts automatically when BetterManual is open and stops when the app is paused or closed.
- The aperture endpoints have no effect if the lens does not support electronic aperture control (`"available": false`).
- Shutter speed commands only work in `manual` or `shutter` mode. In `aperture` mode the camera controls shutter speed automatically.
- ISO can be set to any value regardless of mode, but in Auto ISO mode (`value=0`) the displayed ISO is the live metered value.
