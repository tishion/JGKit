package com.sheen.jgkit.ntv;

/**
 * Created by Sheen (Shen) Tian on 8/27/2016.
 */
public class Common {
    private static boolean mIsNativeLoades =  false;
    static {
        try {
            System.loadLibrary("Common");
            mIsNativeLoades = true;
        }
        catch (SecurityException | UnsatisfiedLinkError | NullPointerException e) {
            mIsNativeLoades = false;
        }
    }

    public static boolean isNativeLoaded()
    {
        return  mIsNativeLoades;
    }

    public static native String getVersion();
}
