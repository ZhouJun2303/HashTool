package com.tool.hashtool;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import android.app.AlertDialog;
import android.widget.TextView;
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

        // 创建自定义适配器
        CustomAdapter adapter = new CustomAdapter(this, packageList);
        listView.setAdapter(adapter);

        // 设置ListView的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PackageInfo packageInfo = packageList.get(position);
                Signature[] signatures = packageInfo.signatures;

                // 获取APK的签名信息
                String signatureInfo1 = SignatureUtil.getSignatureInfo1(signatures);
                String signatureInfo256 = SignatureUtil.getSignatureInfo256(signatures);

                // 显示签名信息对话框
                showSignatureDialog(packageInfo.packageName, signatureInfo256, signatureInfo1);
            }
        });
    }

//    private List<PackageInfo> getInstalledPackages() {
//        PackageManager packageManager = getPackageManager();
//        return packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);
//    }

    //排除系统应用
    private List<PackageInfo> getInstalledPackages() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES);

        List<PackageInfo> filteredPackages = new ArrayList<>();

        for (PackageInfo packageInfo : installedPackages) {
            // 检查应用程序是否为系统应用程序
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                filteredPackages.add(packageInfo);
            }
        }

        return filteredPackages;
    }

    private void showSignatureDialog(String packageName, String signatureInfo256, String signatureInfo1) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_signature, null);
        builder.setView(dialogView);

        TextView signatureInfoTextView = dialogView.findViewById(R.id.signatureInfoTextView);
        String combinedSignatureInfo = signatureInfo1 + "\n" + signatureInfo256;
        signatureInfoTextView.setText(combinedSignatureInfo);

        Button hash1Button = dialogView.findViewById(R.id.hash1Button);
        Button hash256Button = dialogView.findViewById(R.id.hash256Button);

        final AlertDialog dialog = builder.create(); // 将对话框实例化并声明为 final

        hash1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHashToast(signatureInfo1);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Signature Hash", getHashFromSignatureInfo(signatureInfo1));
                clipboard.setPrimaryClip(clip);
                dialog.dismiss();
            }
        });

        hash256Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHashToast(signatureInfo256);
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Signature Hash", getHashFromSignatureInfo(signatureInfo256));
                clipboard.setPrimaryClip(clip);
                dialog.dismiss();
            }
        });

        builder.setTitle(packageName).show();
    }

    private void showHashToast(String hash) {
        Toast.makeText(this, "已复制 " + hash, Toast.LENGTH_SHORT).show();
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


    // 自定义适配器
    public class CustomAdapter extends BaseAdapter {
        private List<PackageInfo> packageList;
        private LayoutInflater inflater;
        PackageManager packageManager = getPackageManager();

        public CustomAdapter(Context context, List<PackageInfo> packageList) {
            this.packageList = packageList;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return packageList.size();
        }

        @Override
        public Object getItem(int position) {
            return packageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_layout, parent, false);
            }

            PackageInfo packageInfo = packageList.get(position);

            ImageView imageView = convertView.findViewById(R.id.imageView);
            TextView textView = convertView.findViewById(R.id.textView);
            TextView gameTextView = convertView.findViewById(R.id.gameNameTextView);
            // 设置安装包名称
            textView.setText(packageInfo.packageName);

            // 设置头像
            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            imageView.setImageDrawable(icon);

            // 设置游戏名称
            CharSequence gameName = packageManager.getApplicationLabel(packageInfo.applicationInfo);
            gameTextView.setText(gameName);
            return convertView;
        }
    }
}

