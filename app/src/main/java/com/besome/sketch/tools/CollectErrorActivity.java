package com.besome.sketch.tools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import a.a.a.GB;
import a.a.a.xB;
import pro.sketchware.R;
import pro.sketchware.utility.SketchwareUtil;

public class CollectErrorActivity extends BaseAppCompatActivity {
    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            final String error = intent.getStringExtra("error");

            var dialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(xB.b().a(getApplicationContext(), R.string.common_error_an_error_occurred))
                    .setMessage(getString(R.string.an_error_occurred_while_running_sketchware_pro) +
                            getString(R.string.do_you_want_to_report_this_error_log_so_that_we_can_fix_it) +
                            getString(R.string.no_personal_information_will_be_included))
                    .setPositiveButton(getString(R.string.common_word_copy), null)
                    .setNegativeButton(R.string.common_word_cancel, (dialogInterface, which) -> finish())
                    .setNeutralButton(R.string.show_error, null) // null to set proper onClick listeners later without dismissing the AlertDialog
                    .show();

            TextView messageView = dialog.findViewById(android.R.id.message);

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                messageView.setTextIsSelectable(true);
                messageView.setText(error);
            });
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                PackageInfo info;

                try {
                    info = getPackageManager().getPackageInfo(getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    messageView.setTextIsSelectable(true);
                    messageView.setText("Somehow couldn't get package info. Stack trace:\n" + Log.getStackTraceString(e));
                    return;
                }

                long fileSizeInBytes = new File(info.applicationInfo.sourceDir).length();

                String deviceInfo = "Sketchware Pro " + info.versionName + " (" + info.versionCode + ")\n"
                        + "base.apk size: " + Formatter.formatFileSize(this, fileSizeInBytes) + " (" + fileSizeInBytes + " B)\n"
                        + "Locale: " + GB.g(getApplicationContext()) + "\n"
                        + "SDK version: " + Build.VERSION.SDK_INT + "\n"
                        + "Brand: " + Build.BRAND + "\n"
                        + "Manufacturer: " + Build.MANUFACTURER + "\n"
                        + "Model: " + Build.MODEL;

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("error", deviceInfo + "\n\n```\n" + error + "\n```");
                clipboard.setPrimaryClip(clip);
                runOnUiThread(() -> SketchwareUtil.toast(getString(R.string.common_word_copied), Toast.LENGTH_LONG));
            });
        }
    }
}
