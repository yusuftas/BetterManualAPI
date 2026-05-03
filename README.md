# BetterManualAPI

This is a fork of [BetterManual](https://github.com/obs1dium/BetterManual) that adds a **full HTTP REST API** for remote control of the camera over Wi-Fi. Using the API you can read and set shutter speed, ISO, aperture, exposure compensation, shooting mode, and drive mode - and trigger the shutter - from any HTTP client on the same network. This goes well beyond what Sony's built-in Smart Remote app exposes.

See [API.md](API.md) for the full API reference, or jump to the [Quick reference](#quick-reference) below.

---

This app is intended to ease shooting in manual and aperture priority mode with (legacy) prime lenses on the A5x00 camera. It uses the [OpenMemories Framework](https://github.com/ma1co/OpenMemories-Framework).
**While the app may work on other cameras, it was written specifically for use with the A5x00.**
**Do not try to install it on cameras without a dedicated manual mode!**

## Installation ##

Use [Sony-PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE) or install through [sony-pmca.appspot.com](https://sony-pmca.appspot.com/apps).

## Usage and features ##

Features are accessed using either the touch screen or the control wheel.

### Shooting modes ###

Touch the mode indicator on the top left to toggle between manual and aperture priority mode.
The icon directly below it allows you to cycle between single shot, low speed and high speed burst modes.

### Shooting parameters ###

The following parameters can be configured directly: Shutter speed, aperture, ISO, exposure compensation.
Using the control wheel: Press the down button to select the parameter you'd like to change, then turn the wheel to change its value. Additionally, the enter button allows you to toggle between manual and automatic ISO and set a minimum shutter speed in aperture priority mode.
Using the touch screen: Swipe left/right or up/down to change parameters. Tap once to toggle between manual and automatic ISO or set the minimum shutter speed.

### Interface ###

Press the up button to cycle through display modes. This will toggle the histogram, metering display and rule of 3rds grid lines.

### Focus magnification ###

Use the zoom lever to activate focus magnification and zoom in or out. Half-pressing the shutter button ends focus magnification. You can change the position of the preview rectangle by either using the control wheel or swiping the touch screen. The enter button centers the preview rectangle.

### Other features ###

The app also includes simple timelapse and exposure bracketing modes. Touch the icon on the left side and follow the on-screen instructions.

Long exposure noise reduction is disabled (normally forced in single shot mode for exposures >= 1" and in bulb mode on the A5100).

Exit the app using the help/trash button.

The app remembers the configured settings across multiple runs.

## Remote Control API ##

BetterManual exposes an HTTP REST API on port **8080** that allows remote control of all camera settings over Wi-Fi. This extends the limited API offered by Sony's built-in Smart Remote app.

When the app starts, the camera's IP address is briefly displayed on screen (e.g. `API: 192.168.1.10:8080`). Connect a phone, laptop, or any HTTP client to the same Wi-Fi network to use it.

### Quick reference ###

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/state` | All current settings as JSON |
| GET | `/api/shutter` | Current shutter speed |
| POST | `/api/shutter` | Set shutter speed (`n=1&d=100`) |
| POST | `/api/shutter/increment` | Step shutter faster |
| POST | `/api/shutter/decrement` | Step shutter slower |
| GET | `/api/iso` | Current ISO and supported values |
| POST | `/api/iso` | Set ISO (`value=400`, or `value=0` for auto) |
| GET | `/api/aperture` | Current aperture |
| POST | `/api/aperture/increment` | Open aperture one step |
| POST | `/api/aperture/decrement` | Close aperture one step |
| GET | `/api/ev` | Exposure compensation |
| POST | `/api/ev` | Set exposure compensation (`value=3`) |
| GET | `/api/mode` | Current shooting mode |
| POST | `/api/mode` | Set mode (`mode=manual\|aperture\|shutter`) |
| GET | `/api/drive` | Current drive mode |
| POST | `/api/drive` | Set drive mode (`mode=single\|burst_high\|burst_low`) |
| POST | `/api/capture` | Trigger shutter |

See [API.md](API.md) for full documentation.
