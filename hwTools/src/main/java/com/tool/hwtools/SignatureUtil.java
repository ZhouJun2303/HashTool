package com.tool.hwtools;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignatureUtil {
    private static final String TAG = "SignatureUtil";

    public static String getSignatureInfo256(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
           return getSignatureInfo256(packageInfo.signatures);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSignatureInfo1(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            return getSignatureInfo1(packageInfo.signatures);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSignatureInfo256(Signature[] signatures) {
        if (null == signatures) return "Null";
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            for (Signature signature : signatures) {
                byte[] signatureBytes = signature.toByteArray();
                byte[] hashBytes = md.digest(signatureBytes);
                String hash = bytesToHex(hashBytes);
                sb.append("Signature Hash 256: ").append(hash).append("\n\n");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getSignatureInfo1(Signature[] signatures) {
        if (null == signatures) return "Null";
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (Signature signature : signatures) {
                byte[] signatureBytes = signature.toByteArray();
                byte[] hashBytes = md.digest(signatureBytes);
                String hash = bytesToHex(hashBytes);
                sb.append("Signature Hash 1: ").append(hash).append("\n\n");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String getApkHash(Context context, String packageName) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            String apkFilePath = packageInfo.applicationInfo.sourceDir;
            return calculateHash(apkFilePath);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private static String calculateHash(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte hashByte : hashBytes) {
            sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static String convertFormattedHashToHex(String formattedHash) {
        // 去除冒号
        // formattedHash = formattedHash.replaceAll(":", "");

        // 转换为十六进制表示
        StringBuilder hexHash = new StringBuilder();
        for (int i = 0; i < formattedHash.length(); i += 2) {
            String hexByte = formattedHash.substring(i, i + 2);
            int decimal = Integer.parseInt(hexByte, 16);
            String hex = Integer.toHexString(decimal);
            hexHash.append(hex);
        }

        return hexHash.toString();
    }

}