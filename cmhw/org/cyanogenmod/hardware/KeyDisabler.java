package org.cyanogenmod.hardware;

import org.cyanogenmod.internal.util.FileUtils;

/*
 * Disable capacitive keys
 *
 * This is intended for use on devices in which the capacitive keys
 * can be fully disabled for replacement with a soft navbar. You
 * really should not be using this on a device with mechanical or
 * otherwise visible-when-inactive keys
 */

public class KeyDisabler {

    private static String CONTROL_PATH = "/sys/class/leds/button-back/brightness"; // Example path

    public static boolean isSupported() {
        return FileUtils.isFileWritable(CONTROL_PATH);
    }

    public static boolean isActive() {
        return (FileUtils.readOneLine(CONTROL_PATH).equals("0"));
    }

    public static boolean setActive(boolean state) {
        return FileUtils.writeLine(CONTROL_PATH, (state ? "0" : "1"));
    }

}
