/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view;

import dalvik.system.CloseGuard;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.Surface;
import android.os.IBinder;
import android.os.SystemProperties;
import android.util.Log;


public class SurfaceControl {
    private static final String TAG = "SurfaceControl";

    private static native int nativeCreate(SurfaceSession session, String name,
            int w, int h, int format, int flags)
            throws OutOfResourcesException;
    private static native void nativeRelease(int nativeObject);
    private static native void nativeDestroy(int nativeObject);

    private static native Bitmap nativeScreenshot(IBinder displayToken,
            int width, int height, int minLayer, int maxLayer, boolean allLayers);
    private static native void nativeScreenshot(IBinder displayToken, Surface consumer,
            int width, int height, int minLayer, int maxLayer, boolean allLayers);

    private static native void nativeOpenTransaction();
    private static native void nativeCloseTransaction();
    private static native void nativeSetAnimationTransaction();

    private static native void nativeSetLayer(int nativeObject, int zorder);
    private static native void nativeSetPosition(int nativeObject, float x, float y);
    private static native void nativeSetSize(int nativeObject, int w, int h);
    private static native void nativeSetTransparentRegionHint(int nativeObject, Region region);
    private static native void nativeSetAlpha(int nativeObject, float alpha);
    private static native void nativeSetMatrix(int nativeObject, float dsdx, float dtdx, float dsdy, float dtdy);
    private static native void nativeSetFlags(int nativeObject, int flags, int mask);
    private static native void nativeSetWindowCrop(int nativeObject, int l, int t, int r, int b);
    private static native void nativeSetLayerStack(int nativeObject, int layerStack);

    private static native IBinder nativeGetBuiltInDisplay(int physicalDisplayId);
    private static native IBinder nativeCreateDisplay(String name, boolean secure);
    private static native void nativeSetDisplaySurface(
            IBinder displayToken, int nativeSurfaceObject);
    private static native void nativeSetDisplayLayerStack(
            IBinder displayToken, int layerStack);
    private static native void nativeSetDisplayProjection(
            IBinder displayToken, int orientation,
            int l, int t, int r, int b, 
            int L, int T, int R, int B);
    private static native boolean nativeGetDisplayInfo(
            IBinder displayToken, SurfaceControl.PhysicalDisplayInfo outInfo);
    private static native void nativeBlankDisplay(IBinder displayToken);
    private static native void nativeUnblankDisplay(IBinder displayToken);


    private final CloseGuard mCloseGuard = CloseGuard.get();
    private String mName;
    int mNativeObject; // package visibility only for Surface.java access

    private static final boolean HEADLESS = "1".equals(
        SystemProperties.get("ro.config.headless", "0"));

    
    public static class OutOfResourcesException extends Exception {
        public OutOfResourcesException() {
        }
        public OutOfResourcesException(String name) {
            super(name);
        }
    }

   
    public static final int HIDDEN = 0x00000004;

   
    public static final int SECURE = 0x00000080;

   
    public static final int NON_PREMULTIPLIED = 0x00000100;

   
    public static final int OPAQUE = 0x00000400;

    
    public static final int PROTECTED_APP = 0x00000800;

   
    public static final int FX_SURFACE_NORMAL   = 0x00000000;

   
    public static final int FX_SURFACE_DIM = 0x00020000;

 
    public static final int FX_SURFACE_MASK = 0x000F0000;

    
    public static final int SURFACE_HIDDEN = 0x01;


   
    public static final int BUILT_IN_DISPLAY_ID_MAIN = 0;

   
    public static final int BUILT_IN_DISPLAY_ID_HDMI = 1;



    public SurfaceControl(SurfaceSession session,
            String name, int w, int h, int format, int flags)
                    throws OutOfResourcesException {
        if (session == null) {
            throw new IllegalArgumentException("session must not be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        if ((flags & SurfaceControl.HIDDEN) == 0) {
            Log.w(TAG, "Surfaces should always be created with the HIDDEN flag set "
                    + "to ensure that they are not made visible prematurely before "
                    + "all of the surface's properties have been configured.  "
                    + "Set the other properties and make the surface visible within "
                    + "a transaction.  New surface name: " + name,
                    new Throwable());
        }

        checkHeadless();

        mName = name;
        mNativeObject = nativeCreate(session, name, w, h, format, flags);
        if (mNativeObject == 0) {
            throw new OutOfResourcesException(
                    "Couldn't allocate SurfaceControl native object");
        }
        
        mCloseGuard.open("release");
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            if (mCloseGuard != null) {
                mCloseGuard.warnIfOpen();
            }
            if (mNativeObject != 0) {
                nativeRelease(mNativeObject);
            }
        } finally {
            super.finalize();
        }
    }

    @Override
    public String toString() {
        return "Surface(name=" + mName + ")";
    }

 
    public void release() {
        if (mNativeObject != 0) {
            nativeRelease(mNativeObject);
            mNativeObject = 0;
        }
        mCloseGuard.close();
    }

   
    public void destroy() {
        if (mNativeObject != 0) {
            nativeDestroy(mNativeObject);
            mNativeObject = 0;
        }
        mCloseGuard.close();
    }

    private void checkNotReleased() {
        if (mNativeObject == 0) throw new NullPointerException(
                "mNativeObject is null. Have you called release() already?");
    }
    
   
    public static void openTransaction() {
        nativeOpenTransaction();
    }

   
    public static void closeTransaction() {
        nativeCloseTransaction();
    }

  
    public static void setAnimationTransaction() {
        nativeSetAnimationTransaction();
    }

    public void setLayer(int zorder) {
        checkNotReleased();
        nativeSetLayer(mNativeObject, zorder);
    }

    public void setPosition(float x, float y) {
        checkNotReleased();
        nativeSetPosition(mNativeObject, x, y);
    }

    public void setSize(int w, int h) {
        checkNotReleased();
        nativeSetSize(mNativeObject, w, h);
    }

    public void hide() {
        checkNotReleased();
        nativeSetFlags(mNativeObject, SURFACE_HIDDEN, SURFACE_HIDDEN);
    }

    public void show() {
        checkNotReleased();
        nativeSetFlags(mNativeObject, 0, SURFACE_HIDDEN);
    }

    public void setTransparentRegionHint(Region region) {
        checkNotReleased();
        nativeSetTransparentRegionHint(mNativeObject, region);
    }

    public void setAlpha(float alpha) {
        checkNotReleased();
        nativeSetAlpha(mNativeObject, alpha);
    }

    public void setMatrix(float dsdx, float dtdx, float dsdy, float dtdy) {
        checkNotReleased();
        nativeSetMatrix(mNativeObject, dsdx, dtdx, dsdy, dtdy);
    }

    public void setFlags(int flags, int mask) {
        checkNotReleased();
        nativeSetFlags(mNativeObject, flags, mask);
    }

    public void setWindowCrop(Rect crop) {
        checkNotReleased();
        if (crop != null) {
            nativeSetWindowCrop(mNativeObject, 
                crop.left, crop.top, crop.right, crop.bottom);
        } else {
            nativeSetWindowCrop(mNativeObject, 0, 0, 0, 0);
        }
    }

    public void setLayerStack(int layerStack) {
        checkNotReleased();
        nativeSetLayerStack(mNativeObject, layerStack);
    }

  
    public static final class PhysicalDisplayInfo {
        public int width;
        public int height;
        public float refreshRate;
        public float density;
        public float xDpi;
        public float yDpi;
        public boolean secure;
    
        public PhysicalDisplayInfo() {
        }
    
        public PhysicalDisplayInfo(PhysicalDisplayInfo other) {
            copyFrom(other);
        }
    
        @Override
        public boolean equals(Object o) {
            return o instanceof PhysicalDisplayInfo && equals((PhysicalDisplayInfo)o);
        }
    
        public boolean equals(PhysicalDisplayInfo other) {
            return other != null
                    && width == other.width
                    && height == other.height
                    && refreshRate == other.refreshRate
                    && density == other.density
                    && xDpi == other.xDpi
                    && yDpi == other.yDpi
                    && secure == other.secure;
        }
    
        @Override
        public int hashCode() {
            return 0; // don't care
        }
    
        public void copyFrom(PhysicalDisplayInfo other) {
            width = other.width;
            height = other.height;
            refreshRate = other.refreshRate;
            density = other.density;
            xDpi = other.xDpi;
            yDpi = other.yDpi;
            secure = other.secure;
        }
    
        // For debugging purposes
        @Override
        public String toString() {
            return "PhysicalDisplayInfo{" + width + " x " + height + ", " + refreshRate + " fps, "
                    + "density " + density + ", " + xDpi + " x " + yDpi + " dpi, secure " + secure
                    + "}";
        }
    }

    public static void unblankDisplay(IBinder displayToken) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        nativeUnblankDisplay(displayToken);
    }

    public static void blankDisplay(IBinder displayToken) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        nativeBlankDisplay(displayToken);
    }

    public static boolean getDisplayInfo(IBinder displayToken, SurfaceControl.PhysicalDisplayInfo outInfo) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        if (outInfo == null) {
            throw new IllegalArgumentException("outInfo must not be null");
        }
        return nativeGetDisplayInfo(displayToken, outInfo);
    }

    public static void setDisplayProjection(IBinder displayToken,
            int orientation, Rect layerStackRect, Rect displayRect) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        if (layerStackRect == null) {
            throw new IllegalArgumentException("layerStackRect must not be null");
        }
        if (displayRect == null) {
            throw new IllegalArgumentException("displayRect must not be null");
        }
        nativeSetDisplayProjection(displayToken, orientation,
                layerStackRect.left, layerStackRect.top, layerStackRect.right, layerStackRect.bottom, 
                displayRect.left, displayRect.top, displayRect.right, displayRect.bottom);
    }

    public static void setDisplayLayerStack(IBinder displayToken, int layerStack) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        nativeSetDisplayLayerStack(displayToken, layerStack);
    }

    public static void setDisplaySurface(IBinder displayToken, Surface surface) {
        if (displayToken == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }

        if (surface != null) {
            synchronized (surface.mLock) {
                nativeSetDisplaySurface(displayToken, surface.mNativeSurface);
            }
        } else {
            nativeSetDisplaySurface(displayToken, 0);
        }
    }

    public static IBinder createDisplay(String name, boolean secure) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        return nativeCreateDisplay(name, secure);
    }

    public static IBinder getBuiltInDisplay(int builtInDisplayId) {
        return nativeGetBuiltInDisplay(builtInDisplayId);
    }


    
    public static void screenshot(IBinder display, Surface consumer,
            int width, int height, int minLayer, int maxLayer) {
        screenshot(display, consumer, width, height, minLayer, maxLayer, false);
    }

   
    public static void screenshot(IBinder display, Surface consumer,
            int width, int height) {
        screenshot(display, consumer, width, height, 0, 0, true);
    }

 
    public static void screenshot(IBinder display, Surface consumer) {
        screenshot(display, consumer, 0, 0, 0, 0, true);
    }


    
    public static Bitmap screenshot(int width, int height, int minLayer, int maxLayer) {
        // TODO: should take the display as a parameter
        IBinder displayToken = SurfaceControl.getBuiltInDisplay(
                SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN);
        return nativeScreenshot(displayToken, width, height, minLayer, maxLayer, false);
    }

   
    public static Bitmap screenshot(int width, int height) {
        // TODO: should take the display as a parameter
        IBinder displayToken = SurfaceControl.getBuiltInDisplay(
                SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN);
        return nativeScreenshot(displayToken, width, height, 0, 0, true);
    }
    
    private static void screenshot(IBinder display, Surface consumer,
            int width, int height, int minLayer, int maxLayer, boolean allLayers) {
        if (display == null) {
            throw new IllegalArgumentException("displayToken must not be null");
        }
        if (consumer == null) {
            throw new IllegalArgumentException("consumer must not be null");
        }
        nativeScreenshot(display, consumer, width, height, minLayer, maxLayer, allLayers);
    }

    private static void checkHeadless() {
        if (HEADLESS) {
            throw new UnsupportedOperationException("Device is headless");
        }
    }
}
