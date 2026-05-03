package com.obsidium.bettermanual;

import android.os.Handler;
import java.util.List;

public interface CameraController {
    int getShutterSpeedN();
    int getShutterSpeedD();
    int getCurrentIso();
    boolean hasApertureControl();
    int getCurrentAperture();
    int getExposureCompensation();
    int getMinExposureCompensation();
    int getMaxExposureCompensation();
    float getExposureCompensationStep();
    String getSceneModeString();
    String getDriveModeString();
    List<Integer> getSupportedIsos();
    Handler getMainHandler();

    void cmdIncrementShutter();
    void cmdDecrementShutter();
    void cmdSetShutterSpeed(int n, int d);
    void cmdSetIso(int iso);
    void cmdIncrementAperture();
    void cmdDecrementAperture();
    void cmdSetExposureCompensation(int value);
    void cmdSetSceneMode(String mode);
    void cmdSetDriveMode(String mode);
    void cmdCapture();
}
