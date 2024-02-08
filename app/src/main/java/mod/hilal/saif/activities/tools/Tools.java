package mod.hilal.saif.activities.tools;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.lib.ui.EasyDeleteEditText;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sketchware.remod.R;

import java.io.File;

import a.a.a.aB;
import dev.aldi.sayuti.editor.manage.ManageLocalLibraryActivity;
import kellinwood.security.zipsigner.ZipSigner;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.alucard.tn.apksigner.ApkSigner;
import mod.hey.studios.code.SrcCodeEditor;
import mod.hey.studios.code.SrcCodeEditorLegacy;
import mod.hey.studios.util.Helper;
import mod.khaled.logcat.LogReaderActivity;

public class Tools extends AppCompatActivity {

    private LinearLayout base;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout.LayoutParams _lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout _base = new LinearLayout(this);
        _base.setOrientation(LinearLayout.VERTICAL);
        _base.setLayoutParams(_lp);
        newToolbar(_base);
        ScrollView _scroll = new ScrollView(this);

        base = new LinearLayout(this);
        base.setOrientation(LinearLayout.VERTICAL);
        base.setLayoutParams(_lp);
        _scroll.setLayoutParams(_lp);
        _scroll.addView(base);
        _base.addView(_scroll);
        setupViews();
        setContentView(_base);
    }

    private void makeup(View parent, int iconResourceId, String title, String subtitle) {
        View inflate = getLayoutInflater().inflate(R.layout.manage_library_base_item, null);
        ImageView icon = inflate.findViewById(R.id.lib_icon);
        inflate.findViewById(R.id.tv_enable).setVisibility(View.GONE);
        ((LinearLayout) icon.getParent()).setGravity(Gravity.CENTER);
        icon.setImageResource(iconResourceId);
        ((TextView) inflate.findViewById(R.id.lib_title)).setText(title);
        ((TextView) inflate.findViewById(R.id.lib_desc)).setText(subtitle);
        ((ViewGroup) parent).addView(inflate);
    }

    private MaterialCardView newCard(int width, int height, float weight) {
        MaterialCardView cardView = new MaterialCardView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height, weight);
        layoutParams.setMargins(
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(2),
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(2)
        );
        cardView.setLayoutParams(layoutParams);
        cardView.setPadding(
                (int) SketchwareUtil.getDip(2),
                (int) SketchwareUtil.getDip(2),
                (int) SketchwareUtil.getDip(2),
                (int) SketchwareUtil.getDip(2)
        );
        cardView.setCardBackgroundColor(Color.WHITE);
        cardView.setRadius(SketchwareUtil.getDip(4));
        return cardView;
    }

    private LinearLayout newLayout(int width, int height, float weight) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(width, height, weight));
        linearLayout.setPadding(
                (int) SketchwareUtil.getDip(1),
                (int) SketchwareUtil.getDip(1),
                (int) SketchwareUtil.getDip(1),
                (int) SketchwareUtil.getDip(1)
        );
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.WHITE);
        linearLayout.setBackground(new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{Color.parseColor("#64B5F6")}), gradientDrawable, null));
        linearLayout.setClickable(true);
        linearLayout.setFocusable(true);
        return linearLayout;
    }

    private void newToolbar(View parent) {
        View toolbar = getLayoutInflater().inflate(R.layout.toolbar, null);
        toolbar.findViewById(R.id.layout_main_logo).setVisibility(View.GONE);
        setSupportActionBar((Toolbar) toolbar);
        getSupportActionBar().setTitle(R.string.developer_tools);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        ((Toolbar) toolbar).setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        parent.setPadding(0, 0, 0, 0);
        ((ViewGroup) parent).addView(toolbar, 0);
    }

    private void openWorkingDirectory() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT;
        properties.root = getFilesDir().getParentFile();
        properties.error_dir = getExternalCacheDir();
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle(getString(R.string.select_an_entry_to_modify));
        dialog.setDialogSelectionListener(files -> {
            final boolean isDirectory = new File(files[0]).isDirectory();
            if (files.length > 1 || isDirectory) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.select_an_action)
                        .setSingleChoiceItems(new String[]{getString(R.string.common_word_delete)}, -1, (actionDialog, which) -> {
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.common_word_delete + (isDirectory ? "folder" : "file") + "?")
                                    .setMessage("Are you sure you want to delete this " + (isDirectory ? "folder" : "file") + " permanently? This cannot be undone.")
                                    .setPositiveButton(R.string.common_word_delete, (deleteConfirmationDialog, pressedButton) -> {
                                        for (String file : files) {
                                            FileUtil.deleteFile(file);
                                            deleteConfirmationDialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton(R.string.common_word_cancel, null)
                                    .show();
                            actionDialog.dismiss();
                        })
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.select_an_action)
                        .setSingleChoiceItems(new String[]{getString(R.string.common_word_edit), getString(R.string.common_word_delete)}, -1, (actionDialog, which) -> {
                            switch (which) {
                                case 0:
                                    Intent intent = new Intent(getApplicationContext(), ConfigActivity.isLegacyCeEnabled() ?
                                            SrcCodeEditorLegacy.class
                                            : SrcCodeEditor.class);
                                    intent.putExtra("title", Uri.parse(files[0]).getLastPathSegment());
                                    intent.putExtra("content", files[0]);
                                    intent.putExtra("xml", "");
                                    startActivity(intent);
                                    break;

                                case 1:
                                    new AlertDialog.Builder(this)
                                            .setTitle(R.string.delete_file)
                                            .setMessage(R.string.delete_file_permanently)
                                            .setPositiveButton(R.string.common_word_delete, (deleteDialog, pressedButton) ->
                                                    FileUtil.deleteFile(files[0]))
                                            .setNegativeButton(R.string.common_word_cancel, null)
                                            .show();
                                    break;
                            }
                            actionDialog.dismiss();
                        })
                        .show();
            }
        });
        dialog.show();
    }

    private void setupViews() {
        MaterialCardView blockManager = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout newLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        blockManager.addView(newLayout);
        makeup(newLayout, R.drawable.block_96_blue, getString(R.string.block_manager), getString(R.string.manage_your_own_blocks_to_use_in_logic_editor));
        base.addView(blockManager);
        newLayout.setOnClickListener(new ActivityLauncher(
                new Intent(getApplicationContext(), BlocksManager.class)));

        MaterialCardView blockSelectorManager = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout blockSelectorManagerLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        blockSelectorManager.addView(blockSelectorManagerLayout);
        makeup(blockSelectorManagerLayout, R.drawable.pull_down_48, getString(R.string.block_selector_menu_manager), getString(R.string.manage_your_own_block_selector_menus));
        base.addView(blockSelectorManager);
        blockSelectorManagerLayout.setOnClickListener(new ActivityLauncher(
                new Intent(getApplicationContext(), BlockSelectorActivity.class)));

        MaterialCardView componentManager = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout componentManagerLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        componentManager.addView(componentManagerLayout);
        makeup(componentManagerLayout, R.drawable.collage_48, getString(R.string.component_manager), getString(R.string.manage_your_own_components));
        base.addView(componentManager);
        componentManagerLayout.setOnClickListener(new ActivityLauncher(
                new Intent(getApplicationContext(), ManageCustomComponentActivity.class)));

        MaterialCardView eventManager = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout eventManagerLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        eventManager.addView(eventManagerLayout);
        makeup(eventManagerLayout, R.drawable.event_on_item_clicked_48dp, getString(R.string.event_manager), getString(R.string.manage_your_own_events));
        base.addView(eventManager);
        eventManagerLayout.setOnClickListener(new ActivityLauncher(
                new Intent(getApplicationContext(), EventsMaker.class)));

        MaterialCardView localLibraryManager = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout localLibraryManagerLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        localLibraryManager.addView(localLibraryManagerLayout);
        makeup(localLibraryManagerLayout, R.drawable.colored_box_96, getString(R.string.local_library_manager), getString(R.string.manage_and_download_local_libraries));
        base.addView(localLibraryManager);
        localLibraryManagerLayout.setOnClickListener(new ActivityLauncher(
                new Intent(getApplicationContext(), ManageLocalLibraryActivity.class),
                new Pair<>("sc_id", "system")));

        MaterialCardView modSettings = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout modSettingsLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        modSettings.addView(modSettingsLayout);
        makeup(modSettingsLayout, R.drawable.engineering_48, getString(R.string.mod_settings), getString(R.string.change_general_mod_settings));
        base.addView(modSettings);
        modSettingsLayout.setOnClickListener(new ActivityLauncher(
                new Intent(getApplicationContext(), ConfigActivity.class)));

        MaterialCardView openWorkingDirectory = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout openWorkingDirectoryLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        openWorkingDirectory.addView(openWorkingDirectoryLayout);
        makeup(openWorkingDirectoryLayout, R.mipmap.ic_type_folder, getString(R.string.open_working_directory), getString(R.string.open_directory));
        base.addView(openWorkingDirectory);
        openWorkingDirectoryLayout.setOnClickListener(v -> openWorkingDirectory());

        MaterialCardView signApkFile = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout signApkFileLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        signApkFile.addView(signApkFileLayout);
        makeup(signApkFileLayout, R.drawable.ic_apk_color_96dp, getString(R.string.sign_an_apk_file_with_testkey), getString(R.string.sign_an_already_existing_apk_file));
        base.addView(signApkFile);
        signApkFileLayout.setOnClickListener(v -> signApkFileDialog());

        MaterialCardView openLogcatReader = newCard(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.0f);
        LinearLayout logcatReaderLayout = newLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 0.0f);
        openLogcatReader.addView(logcatReaderLayout);
        makeup(logcatReaderLayout, R.drawable.icons8_app_components, getString(R.string.design_drawer_menu_title_logcat_reader), getString(R.string.design_drawer_menu_subtitle_logcat_reader));
        base.addView(openLogcatReader);
        logcatReaderLayout.setOnClickListener(new ActivityLauncher(
                new Intent(getApplicationContext(), LogReaderActivity.class)));
    }

    private void signApkFileDialog() {
        aB apkPathDialog = new aB(this);
        apkPathDialog.a(R.drawable.ic_apk_color_96dp);
        apkPathDialog.b(getString(R.string.input_apk));

        View testkey_root = getLayoutInflater().inflate(R.layout.sign_apk_file_dialog, null, false);
        TextInputEditText ed_input = testkey_root.findViewById(R.id.edit_input);
        MaterialButton select_file = testkey_root.findViewById(R.id.btn_select);

        select_file.setOnClickListener(view -> {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.extensions = new String[]{"apk"};
            FilePickerDialog dialog = new FilePickerDialog(this, properties);
            dialog.setDialogSelectionListener(files -> ed_input.setText(files[0]));
            dialog.setTitle(getString(R.string.select_the_apk_to_sign));
            dialog.show();
        });

        apkPathDialog.a(testkey_root);

        apkPathDialog.a(Helper.getResString(R.string.common_word_cancel),
                (dialogInterface, whichDialog) -> Helper.getDialogDismissListener(dialogInterface));
        apkPathDialog.b(getString(R.string.common_word_next), (dialogInterface, whichDialog) -> {
            dialogInterface.dismiss();

            String input_apk_path = ed_input.getText().toString();
            String output_apk_file_name = Uri.fromFile(new File(input_apk_path)).getLastPathSegment();
            String output_apk_path = new File(Environment.getExternalStorageDirectory(),
                    "sketchware/signed_apk/" + output_apk_file_name).getAbsolutePath();

            if (new File(output_apk_path).exists()) {
                aB confirmOverwrite = new aB(this);
                confirmOverwrite.a(R.drawable.color_save_as_new_96);
                confirmOverwrite.b("File exists");
                confirmOverwrite.a("An APK named " + output_apk_file_name + " already exists at " +
                        "/Internal storage/sketchware/signed_apk/. Overwrite it?");

                confirmOverwrite.a(Helper.getResString(R.string.common_word_cancel),
                        (d, which) -> Helper.getDialogDismissListener(d));
                confirmOverwrite.b("Overwrite", (d, which) -> {
                    d.dismiss();
                    signApkFileWithDialog(input_apk_path, output_apk_path, true,
                            null, null, null, null);
                });
                confirmOverwrite.show();
            } else {
                signApkFileWithDialog(input_apk_path, output_apk_path, true,
                        null, null, null, null);
            }
        });
        apkPathDialog.show();
    }

    private void signApkFileWithDialog(String inputApkPath, String outputApkPath, boolean useTestkey,
                                       String keyStorePath, String keyStorePassword, String keyStoreKeyAlias,
                                       String keyPassword) {
        View building_root = getLayoutInflater().inflate(R.layout.build_progress_msg_box, null, false);
        LinearLayout layout_quiz = building_root.findViewById(R.id.layout_quiz);
        TextView tv_progress = building_root.findViewById(R.id.tv_progress);

        ScrollView scroll_view = new ScrollView(this);
        TextView tv_log = new TextView(this);
        scroll_view.addView(tv_log);
        layout_quiz.addView(scroll_view);

        tv_progress.setText(R.string.signing_apk);

        AlertDialog building_dialog = new AlertDialog.Builder(this)
                .setView(building_root)
                .create();

        ApkSigner signer = new ApkSigner();
        new Thread() {
            @Override
            public void run() {
                super.run();

                ApkSigner.LogCallback callback = line -> runOnUiThread(() ->
                        tv_log.setText(tv_log.getText().toString() + line));

                if (useTestkey) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        signer.signWithTestKey(inputApkPath, outputApkPath, callback);
                    } else {
                        try {
                            ZipSigner zipSigner = new ZipSigner();
                            zipSigner.setKeymode(ZipSigner.KEY_TESTKEY);
                            zipSigner.signZip(inputApkPath, outputApkPath);
                        } catch (Exception e) {
                            tv_progress.setText("An error occurred. Check the log for more details.");
                            tv_log.setText("Failed to sign APK with zipsigner: " + e);
                        }
                    }
                } else {
                    signer.signWithKeyStore(inputApkPath, outputApkPath,
                            keyStorePath, keyStorePassword, keyStoreKeyAlias, keyPassword, callback);
                }

                runOnUiThread(() -> {
                    if (callback.errorCount.get() == 0) {
                        building_dialog.dismiss();
                        SketchwareUtil.toast("Successfully saved signed APK to: /Internal storage/sketchware/signed_apk/"
                                        + Uri.fromFile(new File(outputApkPath)).getLastPathSegment(),
                                Toast.LENGTH_LONG);
                    } else {
                        tv_progress.setText("An error occurred. Check the log for more details.");
                    }
                });
            }
        }.start();

        building_dialog.show();
    }

    private class ActivityLauncher implements View.OnClickListener {

        private final Intent launchIntent;
        private Pair<String, String> optionalExtra;

        ActivityLauncher(Intent launchIntent) {
            this.launchIntent = launchIntent;
        }

        ActivityLauncher(Intent launchIntent, Pair<String, String> optionalExtra) {
            this(launchIntent);
            this.optionalExtra = optionalExtra;
        }

        @Override
        public void onClick(View v) {
            if (optionalExtra != null) {
                launchIntent.putExtra(optionalExtra.first, optionalExtra.second);
            }
            startActivity(launchIntent);
        }
    }
}
