package com.tool.hwtools;

import android.content.Context;
import android.content.pm.Signature;

public class HwTools {
    public static boolean CachedValue = false;

    public static boolean IsDeviceRooted() {
        CachedValue = CheckRoot.isDeviceRooted();
        return CheckRoot.isDeviceRooted();
    }

    public static String getSignatureInfo256(Context context, String packageName) {
        return SignatureUtil.getSignatureInfo256(context, packageName);
    }

    public static String getSignatureInfo1(Context context, String packageName) {
        return SignatureUtil.getSignatureInfo1(context, packageName);
    }

    public static String getSignatureInfo256(Signature[] signatures) {
        return SignatureUtil.getSignatureInfo256(signatures);
    }

    public static String getSignatureInfo1(Signature[] signatures) {
        return SignatureUtil.getSignatureInfo1(signatures);
    }

    public static String getApkHash(Context context, String packageName) {
        return SignatureUtil.getApkHash(context, packageName);
    }
}
