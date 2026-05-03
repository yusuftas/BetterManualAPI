package com.obsidium.bettermanual;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraApiServer extends NanoHTTPD {
    public static final int PORT = 8080;
    private static final String MIME_JSON = "application/json";

    private final CameraController m_controller;

    public CameraApiServer(CameraController controller) {
        super(PORT);
        m_controller = controller;
    }

    @Override
    public Response serve(IHTTPSession session) {
        final String uri = session.getUri();
        final Method method = session.getMethod();

        if (Method.OPTIONS.equals(method)) {
            Response r = newFixedLengthResponse(Response.Status.OK, MIME_PLAINTEXT, "");
            addCorsHeaders(r);
            return r;
        }

        if (Method.POST.equals(method)) {
            Map<String, String> files = new HashMap<String, String>();
            try {
                session.parseBody(files);
            } catch (IOException e) {
                return errorResponse("Failed to read request body");
            } catch (ResponseException e) {
                return newFixedLengthResponse(e.getStatus(), MIME_PLAINTEXT, e.getMessage());
            }
        }

        final Map<String, String> params = session.getParms();

        try {
            Response r = route(uri, method, params);
            if (r == null)
                r = errorResponse("Unknown endpoint: " + uri);
            addCorsHeaders(r);
            return r;
        } catch (JSONException e) {
            return errorResponse("Internal JSON error");
        }
    }

    private Response route(String uri, Method method, Map<String, String> params) throws JSONException {
        if ("/api/state".equals(uri) && Method.GET.equals(method))
            return getState();

        if ("/api/shutter".equals(uri) && Method.GET.equals(method))
            return getShutter();
        if ("/api/shutter".equals(uri) && Method.POST.equals(method))
            return postShutter(params);
        if ("/api/shutter/increment".equals(uri) && Method.POST.equals(method))
            return postShutterIncrement();
        if ("/api/shutter/decrement".equals(uri) && Method.POST.equals(method))
            return postShutterDecrement();

        if ("/api/iso".equals(uri) && Method.GET.equals(method))
            return getIso();
        if ("/api/iso".equals(uri) && Method.POST.equals(method))
            return postIso(params);

        if ("/api/aperture".equals(uri) && Method.GET.equals(method))
            return getAperture();
        if ("/api/aperture/increment".equals(uri) && Method.POST.equals(method))
            return postApertureIncrement();
        if ("/api/aperture/decrement".equals(uri) && Method.POST.equals(method))
            return postApertureDecrement();

        if ("/api/ev".equals(uri) && Method.GET.equals(method))
            return getEv();
        if ("/api/ev".equals(uri) && Method.POST.equals(method))
            return postEv(params);

        if ("/api/mode".equals(uri) && Method.GET.equals(method))
            return getMode();
        if ("/api/mode".equals(uri) && Method.POST.equals(method))
            return postMode(params);

        if ("/api/drive".equals(uri) && Method.GET.equals(method))
            return getDrive();
        if ("/api/drive".equals(uri) && Method.POST.equals(method))
            return postDrive(params);

        if ("/api/capture".equals(uri) && Method.POST.equals(method))
            return postCapture();

        return null;
    }

    // --- GET handlers ---

    private Response getState() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("shutter", buildShutterJson());
        json.put("iso", m_controller.getCurrentIso());
        json.put("aperture", buildApertureJson());
        json.put("ev", buildEvJson());
        json.put("mode", m_controller.getSceneModeString());
        json.put("drive", m_controller.getDriveModeString());
        JSONArray isoList = new JSONArray();
        List<Integer> isos = m_controller.getSupportedIsos();
        if (isos != null) {
            for (Integer iso : isos)
                isoList.put(iso.intValue());
        }
        json.put("supported_isos", isoList);
        return jsonOk(json);
    }

    private Response getShutter() throws JSONException {
        return jsonOk(buildShutterJson());
    }

    private Response getIso() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("value", m_controller.getCurrentIso());
        JSONArray supported = new JSONArray();
        List<Integer> isos = m_controller.getSupportedIsos();
        if (isos != null) {
            for (Integer iso : isos)
                supported.put(iso.intValue());
        }
        json.put("supported", supported);
        return jsonOk(json);
    }

    private Response getAperture() throws JSONException {
        return jsonOk(buildApertureJson());
    }

    private Response getEv() throws JSONException {
        return jsonOk(buildEvJson());
    }

    private Response getMode() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("mode", m_controller.getSceneModeString());
        return jsonOk(json);
    }

    private Response getDrive() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("mode", m_controller.getDriveModeString());
        return jsonOk(json);
    }

    // --- POST handlers ---

    private Response postShutter(Map<String, String> params) throws JSONException {
        final String nStr = params.get("n");
        final String dStr = params.get("d");
        if (nStr == null || dStr == null)
            return errorResponse("Missing 'n' and/or 'd' parameters");
        final int n, d;
        try {
            n = Integer.parseInt(nStr);
            d = Integer.parseInt(dStr);
        } catch (NumberFormatException e) {
            return errorResponse("Invalid n/d values");
        }
        if (CameraUtil.getShutterValueIndex(n, d) < 0)
            return errorResponse(String.format("Shutter speed %d/%d not in supported list", n, d));
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdSetShutterSpeed(n, d); }
        });
        return statusOk();
    }

    private Response postShutterIncrement() throws JSONException {
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdIncrementShutter(); }
        });
        return statusOk();
    }

    private Response postShutterDecrement() throws JSONException {
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdDecrementShutter(); }
        });
        return statusOk();
    }

    private Response postIso(Map<String, String> params) throws JSONException {
        final String valueStr = params.get("value");
        if (valueStr == null)
            return errorResponse("Missing 'value' parameter");
        final int iso;
        try {
            iso = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return errorResponse("Invalid ISO value: " + valueStr);
        }
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdSetIso(iso); }
        });
        return statusOk();
    }

    private Response postApertureIncrement() throws JSONException {
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdIncrementAperture(); }
        });
        return statusOk();
    }

    private Response postApertureDecrement() throws JSONException {
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdDecrementAperture(); }
        });
        return statusOk();
    }

    private Response postEv(Map<String, String> params) throws JSONException {
        final String valueStr = params.get("value");
        if (valueStr == null)
            return errorResponse("Missing 'value' parameter");
        final int ev;
        try {
            ev = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return errorResponse("Invalid EV value: " + valueStr);
        }
        int min = m_controller.getMinExposureCompensation();
        int max = m_controller.getMaxExposureCompensation();
        if (ev < min || ev > max)
            return errorResponse(String.format("EV %d out of range [%d, %d]", ev, min, max));
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdSetExposureCompensation(ev); }
        });
        return statusOk();
    }

    private Response postMode(Map<String, String> params) throws JSONException {
        final String mode = params.get("mode");
        if (mode == null)
            return errorResponse("Missing 'mode' parameter");
        if (!"manual".equals(mode) && !"aperture".equals(mode) && !"shutter".equals(mode))
            return errorResponse("mode must be 'manual', 'aperture', or 'shutter'");
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdSetSceneMode(mode); }
        });
        return statusOk();
    }

    private Response postDrive(Map<String, String> params) throws JSONException {
        final String mode = params.get("mode");
        if (mode == null)
            return errorResponse("Missing 'mode' parameter");
        if (!"single".equals(mode) && !"burst_high".equals(mode) && !"burst_low".equals(mode))
            return errorResponse("mode must be 'single', 'burst_high', or 'burst_low'");
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdSetDriveMode(mode); }
        });
        return statusOk();
    }

    private Response postCapture() throws JSONException {
        m_controller.getMainHandler().post(new Runnable() {
            @Override public void run() { m_controller.cmdCapture(); }
        });
        return statusOk();
    }

    // --- JSON builders ---

    private JSONObject buildShutterJson() throws JSONException {
        JSONObject json = new JSONObject();
        int n = m_controller.getShutterSpeedN();
        int d = m_controller.getShutterSpeedD();
        json.put("n", n);
        json.put("d", d);
        json.put("formatted", d > 0 ? CameraUtil.formatShutterSpeed(n, d) : "");
        return json;
    }

    private JSONObject buildApertureJson() throws JSONException {
        JSONObject json = new JSONObject();
        boolean hasAperture = m_controller.hasApertureControl();
        json.put("available", hasAperture);
        if (hasAperture) {
            int ap = m_controller.getCurrentAperture();
            json.put("value", ap);
            json.put("formatted", String.format("f%.1f", ap / 100.0f));
        }
        return json;
    }

    private JSONObject buildEvJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("value", m_controller.getExposureCompensation());
        json.put("min", m_controller.getMinExposureCompensation());
        json.put("max", m_controller.getMaxExposureCompensation());
        json.put("step", m_controller.getExposureCompensationStep());
        return json;
    }

    // --- Response helpers ---

    private Response statusOk() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("status", "ok");
        return jsonOk(json);
    }

    private Response jsonOk(JSONObject json) {
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, json.toString());
    }

    private Response errorResponse(String message) {
        try {
            JSONObject json = new JSONObject();
            json.put("error", message);
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, MIME_JSON, json.toString());
        } catch (JSONException e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, message);
        }
    }

    private void addCorsHeaders(Response r) {
        r.addHeader("Access-Control-Allow-Origin", "*");
        r.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        r.addHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
