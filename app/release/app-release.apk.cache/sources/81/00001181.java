package com.tool.hashtool;

import android.os.Build;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class CheckRoot {
    private static String TAG = "com.tool.hashtool.CheckRoot";

    public static boolean isDeviceRooted() {
        return checkDeviceDebuggable() || checkSuperuserApk() || checkRootPathSU() || checkRootWhichSU() || checkBusybox() || checkAccessRootData() || checkGetRootAuth();
    }

    public static boolean checkSuperuserApk() {
        try {
            if (!new File("/system/app/SuperSU/SuperSU.apk").exists()) {
                return false;
            }
            Log.w(TAG, "/system/app/SuperSU/SuperSU.apk exist");
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    public static boolean checkDeviceDebuggable() {
        String str = Build.TAGS;
        if (str == null || !str.contains("test-keys")) {
            return false;
        }
        Log.i(TAG, "buildTags=" + str);
        return true;
    }

    public static boolean checkRootPathSU() {
        String[] strArr = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/"};
        for (int i = 0; i < 5; i++) {
            try {
                if (new File(strArr[i] + "su").exists()) {
                    Log.i(TAG, "find su in : " + strArr[i]);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean checkRootWhichSU() {
        ArrayList<String> executeCommand = executeCommand(new String[]{"/system/xbin/which", "su"});
        if (executeCommand != null) {
            Log.i(TAG, "execResult=" + executeCommand.toString());
            return true;
        }
        Log.i(TAG, "execResult=null");
        return false;
    }

    public static ArrayList<String> executeCommand(String[] strArr) {
        ArrayList<String> arrayList = new ArrayList<>();
        try {
            Log.i(TAG, "to shell exec which for find su :");
            Process exec = Runtime.getRuntime().exec(strArr);
            new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            while (true) {
                try {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    Log.i(TAG, "–> Line received: " + readLine);
                    arrayList.add(readLine);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "–> Full response was: " + arrayList);
            return arrayList;
        } catch (Exception unused) {
            return null;
        }
    }

    public static synchronized boolean checkGetRootAuth() {
        Process process;
        synchronized (CheckRoot.class) {
            DataOutputStream dataOutputStream = null;
            try {
                try {
                    Log.i(TAG, "to exec su");
                    process = Runtime.getRuntime().exec("su");
                    try {
                        DataOutputStream dataOutputStream2 = new DataOutputStream(process.getOutputStream());
                        try {
                            dataOutputStream2.writeBytes("exit\n");
                            dataOutputStream2.flush();
                            int waitFor = process.waitFor();
                            Log.i(TAG, "exitValue=" + waitFor);
                            if (waitFor == 0) {
                                try {
                                    dataOutputStream2.close();
                                    process.destroy();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return true;
                            }
                            try {
                                dataOutputStream2.close();
                                process.destroy();
                            } catch (Exception e2) {
                                e2.printStackTrace();
                            }
                            return false;
                        } catch (Exception e3) {
                            e = e3;
                            dataOutputStream = dataOutputStream2;
                            Log.i(TAG, "Unexpected error - Here is what I know: " + e.getMessage());
                            if (dataOutputStream != null) {
                                try {
                                    dataOutputStream.close();
                                } catch (Exception e4) {
                                    e4.printStackTrace();
                                    return false;
                                }
                            }
                            process.destroy();
                            return false;
                        } catch (Throwable th) {
                            th = th;
                            dataOutputStream = dataOutputStream2;
                            if (dataOutputStream != null) {
                                try {
                                    dataOutputStream.close();
                                } catch (Exception e5) {
                                    e5.printStackTrace();
                                    throw th;
                                }
                            }
                            process.destroy();
                            throw th;
                        }
                    } catch (Exception e6) {
                        e = e6;
                    }
                } catch (Exception e7) {
                    e = e7;
                    process = null;
                } catch (Throwable th2) {
                    th = th2;
                    process = null;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    public static synchronized boolean checkBusybox() {
        synchronized (CheckRoot.class) {
            try {
                Log.i(TAG, "to exec busybox df");
                ArrayList<String> executeCommand = executeCommand(new String[]{"busybox", "df"});
                if (executeCommand != null) {
                    Log.i(TAG, "execResult=" + executeCommand.toString());
                    return true;
                }
                Log.i(TAG, "execResult=null");
                return false;
            } catch (Exception e) {
                Log.i(TAG, "Unexpected error - Here is what I know: " + e.getMessage());
                return false;
            }
        }
    }

    public static synchronized boolean checkAccessRootData() {
        synchronized (CheckRoot.class) {
            try {
                Log.i(TAG, "to write /data");
                if (writeFile("/data/su_test", "test_ok").booleanValue()) {
                    Log.i(TAG, "write ok");
                } else {
                    Log.i(TAG, "write failed");
                }
                Log.i(TAG, "to read /data");
                String readFile = readFile("/data/su_test");
                Log.i(TAG, "strRead=" + readFile);
                return "test_ok".equals(readFile);
            } catch (Exception e) {
                Log.i(TAG, "Unexpected error - Here is what I know: " + e.getMessage());
                return false;
            }
        }
    }

    public static Boolean writeFile(String str, String str2) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(str);
            fileOutputStream.write(str2.getBytes());
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String readFile(String str) {
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(str));
            byte[] bArr = new byte[1024];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while (true) {
                int read = fileInputStream.read(bArr);
                if (read > 0) {
                    byteArrayOutputStream.write(bArr, 0, read);
                } else {
                    String str2 = new String(byteArrayOutputStream.toByteArray());
                    Log.i(TAG, str2);
                    return str2;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}