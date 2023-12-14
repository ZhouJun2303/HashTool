package com.tool.hashtool;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.app.AlertDialog;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

public class HashActivity extends Activity {

    private List<PackageInfo> packageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hash);

        ListView listView = findViewById(R.id.listView);

        // 获取设备上已安装的所有APK
        packageList = getInstalledPackages();

        // 将APK名称添加到列表中
        List<String> packageNameList = new ArrayList<>();
        for (PackageInfo packageInfo : packageList) {
            packageNameList.add(packageInfo.packageName);
        }

        // 使用ArrayAdapter将列表显示在ListView中
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, packageNameList);
        listView.setAdapter(adapter);

        // 设置ListView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PackageInfo packageInfo = packageList.get(position);
                Signature[] signatures = packageInfo.signatures;

                // 获取APK的签名信息
                String signatureInfo = SignatureUtil.getSignatureInfo256(signatures);

                // 显示签名信息对话框
                showSignatureDialog(packageInfo.packageName, signatureInfo);
            }
        });
    }

    private List<PackageInfo> getInstalledPackages() {
        PackageManager packageManager = getPackageManager();
        return packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
    }

    private void showSignatureDialog(String packageName, String signatureInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(packageName)
                .setMessage(signatureInfo)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 将签名信息的哈希值设置到剪贴板
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Signature Hash", getHashFromSignatureInfo(signatureInfo));
                        clipboard.setPrimaryClip(clip);

                        // 显示提示消息
                        Toast.makeText(HashActivity.this, "已将哈希值复制到剪贴板", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private String getHashFromSignatureInfo(String signatureInfo) {
        // 在这里实现从签名信息中提取哈希值的逻辑
        // 假设签名信息的格式为 "Signature Hash: [哈希值]\n\n"
        int startIndex = signatureInfo.indexOf(":") + 1;
        int endIndex = signatureInfo.indexOf("\n\n");
        if (startIndex >= 0 && endIndex >= 0 && endIndex > startIndex) {
            return signatureInfo.substring(startIndex, endIndex).trim();
        }
        return "";
    }
}