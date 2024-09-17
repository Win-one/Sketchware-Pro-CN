package mod.hilal.saif.activities.tools;

import static com.besome.sketch.editor.view.ViewEditor.shakeView;
import static mod.SketchwareUtil.dpToPx;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;

import com.besome.sketch.editor.manage.library.LibraryItemView;
import com.besome.sketch.help.SystemSettingActivity;
import com.besome.sketch.language.LanguageSettingsActivity;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.DialogSelectApkToSignBinding;

import java.io.File;

import a.a.a.aB;
import dev.aldi.sayuti.editor.manage.ManageLocalLibraryActivity;
import kellinwood.security.zipsigner.ZipSigner;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.alucard.tn.apksigner.ApkSigner;
import mod.hey.studios.code.SrcCodeEditorLegacy;
import mod.hey.studios.util.Helper;
import mod.khaled.logcat.LogReaderActivity;
import mod.trindadedev.ui.activities.SettingsActivity;

public class AppSettings extends BaseAppCompatActivity {

    private LinearLayout content;
    private MaterialToolbar topAppBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prefences_content_appbar);

        content = findViewById(R.id.content);
        topAppBar = findViewById(R.id.topAppBar);

        topAppBar.setTitle(R.string.app_settings);
        topAppBar.setNavigationOnClickListener(view -> onBackPressed());
        setupViews();
    }

    private void openWorkingDirectory() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT;
        properties.root = getFilesDir().getParentFile();
        properties.error_dir = getExternalCacheDir();
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(this, properties);
        dialog.setTitle(R.string.select_an_entry_to_modify);
        dialog.setDialogSelectionListener(files -> {
            final boolean isDirectory = new File(files[0]).isDirectory();
            if (files.length > 1 || isDirectory) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.select_an_action)
                        .setSingleChoiceItems(new String[]{getString(R.string.common_word_delete)}, -1, (actionDialog, which) -> {
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle(R.string.common_word_delete + (isDirectory ? R.string.common_word_folder : R.string.common_word_file) + "?")
                                    .setMessage(R.string.are_you_sure_you_want_to_delete_this + (isDirectory ? R.string.common_word_folder : R.string.common_word_file) + getString(R.string.permanently_this_cannot_be_undone))
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
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.select_an_action)
                        .setSingleChoiceItems(new String[]{getString(R.string.common_word_edit), getString(R.string.common_word_delete)}, -1, (actionDialog, which) -> {
                            switch (which) {
                                case 0 -> {
                                    Intent intent = new Intent(getApplicationContext(), ConfigActivity.isLegacyCeEnabled() ?
                                            SrcCodeEditorLegacy.class
                                            : mod.hey.studios.code.SrcCodeEditor.class);
                                    intent.putExtra("title", Uri.parse(files[0]).getLastPathSegment());
                                    intent.putExtra("content", files[0]);
                                    intent.putExtra("xml", "");
                                    startActivity(intent);
                                }
                                case 1 -> new MaterialAlertDialogBuilder(this)
                                        .setTitle(R.string.delete_file)
                                        .setMessage(R.string.delete_file_permanently)
                                        .setPositiveButton(R.string.common_word_delete, (deleteDialog, pressedButton) ->
                                                FileUtil.deleteFile(files[0]))
                                        .setNegativeButton(R.string.common_word_cancel, null)
                                        .show();
                            }
                            actionDialog.dismiss();
                        })
                        .show();
            }
        });
        dialog.show();
    }

    private void setupViews() {
        createToolsView(R.drawable.block_96_blue, getString(R.string.block_manager), getString(R.string.manage_your_own_blocks_to_use_in_logic_editor), content, new ActivityLauncher(new Intent(getApplicationContext(), BlocksManager.class)), false);
        createToolsView(R.drawable.pull_down_48, getString(R.string.block_selector_menu_manager), getString(R.string.manage_your_own_block_selector_menus), content, new ActivityLauncher(new Intent(getApplicationContext(), BlockSelectorActivity.class)), false);
        createToolsView(R.drawable.collage_48, getString(R.string.component_manager), getString(R.string.manage_your_own_components), content, new ActivityLauncher(new Intent(getApplicationContext(), ManageCustomComponentActivity.class)), false);
        createToolsView(R.drawable.event_on_item_clicked_48dp, getString(R.string.event_manager), getString(R.string.manage_your_own_events), content, openSettingsActivity(SettingsActivity.EVENTS_MANAGER_FRAGMENT), false);
        createToolsView(R.drawable.colored_box_96, getString(R.string.local_library_manager), getString(R.string.manage_and_download_local_libraries), content, new ActivityLauncher(new Intent(getApplicationContext(), ManageLocalLibraryActivity.class), new Pair<>("sc_id", "system")), false);
        createToolsView(R.drawable.engineering_48, getString(R.string.mod_settings), getString(R.string.change_general_mod_settings), content, new ActivityLauncher(new Intent(getApplicationContext(), ConfigActivity.class)), false);
        createToolsView(R.drawable.ic_settings_24, getString(R.string.main_drawer_title_system_settings), getString(R.string.auto_save_and_vibrations), content, new ActivityLauncher(new Intent(getApplicationContext(), SystemSettingActivity.class)), false);
        createToolsView(R.drawable.icon_pallete, getString(R.string.settings_appearance), getString(R.string.settings_appearance_description), content, openSettingsActivity(SettingsActivity.SETTINGS_APPEARANCE_FRAGMENT), false);
        createToolsView(R.drawable.language_24, getString(R.string.language_settings), getString(R.string.change_the_sketchware_language), content, new ActivityLauncher(new Intent(getApplicationContext(), LanguageSettingsActivity.class)), false);
        createToolsView(R.drawable.ic_folder_48dp, getString(R.string.open_working_directory), getString(R.string.open_directory), content, v -> openWorkingDirectory(), false);
        createToolsView(R.drawable.ic_apk_color_96dp, getString(R.string.sign_an_apk_file_with_testkey), getString(R.string.sign_an_already_existing_apk_file), content, v -> signApkFileDialog(), false);
        createToolsView(R.drawable.icons8_app_components, getString(R.string.design_drawer_menu_title_logcat_reader), getString(R.string.design_drawer_menu_subtitle_logcat_reader), content, new ActivityLauncher(new Intent(getApplicationContext(), LogReaderActivity.class)), true);
    }

    private View.OnClickListener openSettingsActivity(String fragmentTag) {
        return v -> {
            Intent intent = new Intent(v.getContext(), SettingsActivity.class);
            intent.putExtra("fragment_tag", fragmentTag);
            v.getContext().startActivity(intent);
        };
    }

    private void createToolsView(int icon, String title, String desc, LinearLayout toView, View.OnClickListener listener, boolean lastItem) {
        LibraryItemView item = new LibraryItemView(this);
        item.enabled.setVisibility(View.GONE);
        item.icon.setImageResource(icon);
        item.title.setText(title);
        item.description.setText(desc);
        toView.addView(item);
        item.setOnClickListener(listener);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.0f
        );
        itemParams.bottomMargin = lastItem ? dpToPx(25) : dpToPx(0);
        item.setLayoutParams(itemParams);
    }

    private void signApkFileDialog() {
        final boolean[] isAPKSelected = {false};
        aB apkPathDialog = new aB(this);
        apkPathDialog.b(getString(R.string.sign_apk_with_testkey));

        DialogSelectApkToSignBinding binding = DialogSelectApkToSignBinding.inflate(getLayoutInflater());
        View testkey_root = binding.getRoot();
        TextView apk_path_txt = binding.apkPathTxt;

        binding.selectFile.setOnClickListener(v -> {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.extensions = new String[]{"apk"};
            FilePickerDialog dialog = new FilePickerDialog(this, properties);
            dialog.setDialogSelectionListener(files -> {
                isAPKSelected[0] = true;
                apk_path_txt.setText(files[0]);
            });
            dialog.show();
        });

        apkPathDialog.b(getString(R.string.common_word_continue), v -> {
            if (!isAPKSelected[0]) {
                SketchwareUtil.toast(getString(R.string.please_select_an_apk_file_to_sign), Toast.LENGTH_SHORT);
                shakeView(binding.selectFile);
                return;
            }
            String input_apk_path = apk_path_txt.getText().toString();
            String output_apk_file_name = Uri.fromFile(new File(input_apk_path)).getLastPathSegment();
            String output_apk_path = new File(Environment.getExternalStorageDirectory(),
                    "sketchware/signed_apk/" + output_apk_file_name).getAbsolutePath();

            if (new File(output_apk_path).exists()) {
                aB confirmOverwrite = new aB(this);
                confirmOverwrite.a(R.drawable.color_save_as_new_96);
                confirmOverwrite.b(getString(R.string.file_exists));
                confirmOverwrite.a("An APK named " + output_apk_file_name + " already exists at /sketchware/signed_apk/.  Overwrite it?");

                confirmOverwrite.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(confirmOverwrite));
                confirmOverwrite.b(getString(R.string.common_word_overwrite), view -> {
                    confirmOverwrite.dismiss();
                    signApkFileWithDialog(input_apk_path, output_apk_path, true,
                            null, null, null, null);
                });
                confirmOverwrite.show();
            } else {
                signApkFileWithDialog(input_apk_path, output_apk_path, true,
                        null, null, null, null);
            }
        });

        apkPathDialog.a(Helper.getResString(R.string.common_word_cancel), v -> apkPathDialog.dismiss());

        apkPathDialog.a(testkey_root);
        apkPathDialog.setCancelable(false);
        apkPathDialog.show();
    }

    private void signApkFileWithDialog(String inputApkPath, String outputApkPath, boolean useTestkey, String keyStorePath, String keyStorePassword, String keyStoreKeyAlias, String keyPassword) {
        View building_root = getLayoutInflater().inflate(R.layout.build_progress_msg_box, null, false);
        LinearLayout layout_quiz = building_root.findViewById(R.id.layout_quiz);
        TextView tv_progress = building_root.findViewById(R.id.tv_progress);

        ScrollView scroll_view = new ScrollView(this);
        TextView tv_log = new TextView(this);
        scroll_view.addView(tv_log);
        layout_quiz.addView(scroll_view);

        tv_progress.setText(R.string.signing_apk);

        AlertDialog building_dialog = new MaterialAlertDialogBuilder(this)
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
                        tv_progress.setText(R.string.an_error_occurred_check_the_log_for_more_details);
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
