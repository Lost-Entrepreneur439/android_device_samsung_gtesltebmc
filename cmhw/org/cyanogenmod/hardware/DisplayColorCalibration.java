package org.cyanogenmod.hardware;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Slog;

import org.cyanogenmod.internal.util.FileUtils;

public class DisplayColorCalibration {

    private static final String TAG = "DisplayColorCalibration";

    private static final String RGB_FILE = "/sys/class/graphics/fb0/rgb"; // Hypothetical path

    private static final boolean sUseGPUMode;

    private static final int MIN = 0;
    private static final int MAX = 255;

    private static final int[] sCurColors = new int[] { MAX, MAX, MAX };

    static {
        // Check if GPU mode is relevant
        sUseGPUMode = SystemProperties.getBoolean("debug.livedisplay.force_gpu", false);
    }

    public static boolean isSupported() {
        return true;
    }

    public static int getMaxValue() {
        return MAX;
    }

    public static int getMinValue() {
        return MIN;
    }

    public static int getDefValue() {
        return getMaxValue();
    }

    public static String getCurColors() {
        if (!sUseGPUMode) {
            return FileUtils.readOneLine(RGB_FILE);
        }

        return String.format("%d %d %d", sCurColors[0], sCurColors[1], sCurColors[2]);
    }

    public static boolean setColors(String colors) {
        if (!sUseGPUMode) {
            return FileUtils.writeLine(RGB_FILE, colors);
        }

        float[] mat = toColorMatrix(colors);

        // Set to null if identity
        if (mat == null || (mat[0] == 1.0f && mat[5] == 1.0f && mat[10] == 1.0f && mat[15] == 1.0f)) {
            return setColorTransform(null);
        }
        return setColorTransform(mat);
    }

    private static float[] toColorMatrix(String rgbString) {
        String[] adj = rgbString == null ? null : rgbString.split(" ");

        if (adj == null || adj.length != 3) {
            return null;
        }

        float[] mat = new float[16];

        // Sanity check
        for (int i = 0; i < 3; i++) {
            int v = Integer.parseInt(adj[i]);

            if (v >= MAX) {
                v = MAX;
            } else if (v < MIN) {
                v = MIN;
            }

            mat[i * 5] = (float)v / (float)MAX;
            sCurColors[i] = v;
        }

        mat[15] = 1.0f;
        return mat;
    }

    /**
     * Sets the SurfaceFlinger's color transformation as a 4x4 matrix. If the
     * matrix is null, color transformations are disabled.
     *
     * @param m the float array that holds the transformation matrix, or null to
     *            disable transformation
     */
    private static boolean setColorTransform(float[] m) {
        try {
            final IBinder flinger = ServiceManager.getService("SurfaceFlinger");
            if (flinger != null) {
                final Parcel data = Parcel.obtain();
                data.writeInterfaceToken("android.ui.ISurfaceComposer");
                if (m != null) {
                    data.writeInt(1);
                    for (int i = 0; i < 16; i++) {
                        data.writeFloat(m[i]);
                    }
                } else {
                    data.writeInt(0);
                }
                flinger.transact(1030, data, null, 0);
                data.recycle();
            }
        } catch (RemoteException ex) {
            Slog.e(TAG, "Failed to set color transform", ex);
            return false;
        }
        return true;
    }
}
