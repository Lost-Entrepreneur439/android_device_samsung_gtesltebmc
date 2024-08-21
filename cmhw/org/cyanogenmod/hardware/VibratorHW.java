package org.cyanogenmod.hardware;

import org.cyanogenmod.internal.util.FileUtils;

public class VibratorHW {

    private static String LEVEL_PATH = "/sys/class/leds/vibrator/brightness"; // Example path

    public static boolean isSupported() {
        return FileUtils.isFileReadable(LEVEL_PATH) && FileUtils.isFileWritable(LEVEL_PATH);
    }

    public static int getMaxIntensity()  {
        return 10000; // Adjust if necessary
    }

    public static int getMinIntensity()  {
        return 0;
    }

    public static int getWarningThreshold()  {
        return 9000; // Adjust if necessary
    }

    public static int getCurIntensity()  {
        String actualIntensity = FileUtils.readOneLine(LEVEL_PATH);
        return Integer.parseInt(actualIntensity);
    }

    public static int getDefaultIntensity()  {
        return 7500; // Adjust if necessary
    }

    public static boolean setIntensity(int intensity)  {
        return FileUtils.writeLine(LEVEL_PATH, String.valueOf(intensity));
    }
}
