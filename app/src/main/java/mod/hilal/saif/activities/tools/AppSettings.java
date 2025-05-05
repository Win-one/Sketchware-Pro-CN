package mod.hilal.saif.activities.tools;

import static com.besome.sketch.editor.view.ViewEditor.shakeView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.besome.sketch.editor.manage.library.LibraryItemView;
import com.besome.sketch.help.SystemSettingActivity;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;

import dev.aldi.sayuti.editor.manage.ManageLocalLibraryActivity;
import mod.alucard.tn.apksigner.ApkSigner;
import mod.hey.studios.code.SrcCodeEditor;
import mod.hey.studios.util.Helper;
import mod.khaled.logcat.LogReaderActivity;
import pro.sketchware.R;
import pro.sketchware.activities.editor.component.ManageCustomComponentActivity;
import pro.sketchware.activities.settings.SettingsActivity;
import pro.sketchware.databinding.ActivityAppSettingsBinding;
import pro.sketchware.databinding.DialogSelectApkToSignBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;

public class AppSettings extends BaseAppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        var binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.contentScroll, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

        binding.topAppBar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));

        setupPreferences(binding.content);
    }

    private void setupPreferences(ViewGroup content) {
        var preferences = new ArrayList<LibraryItemView>();
        preferences.add(createPreference(R.drawable.ic_mtrl_block, getString(R.string.block_manager), getString(R.string.manage_your_own_blocks_to_use_in_logic_editor), new ActivityLauncher(new Intent(getApplicationContext(), BlocksManager.class))));
        preferences.add(createPreference(R.drawable.ic_mtrl_pull_down, getString(R.string.block_selector_menu_manager), getString(R.string.manage_your_own_block_selector_menus), openSettingsActivity(SettingsActivity.BLOCK_SELECTOR_MANAGER_FRAGMENT)));
        preferences.add(createPreference(R.drawable.ic_mtrl_component, getString(R.string.component_manager), getString(R.string.manage_your_own_components), new ActivityLauncher(new Intent(getApplicationContext(), ManageCustomComponentActivity.class))));
        preferences.add(createPreference(R.drawable.ic_mtrl_list, getString(R.string.event_manager), getString(R.string.manage_your_own_events), openSettingsActivity(SettingsActivity.EVENTS_MANAGER_FRAGMENT)));
        preferences.add(createPreference(R.drawable.ic_mtrl_box, getString(R.string.local_library_manager), getString(R.string.manage_and_download_local_libraries), new ActivityLauncher(new Intent(getApplicationContext(), ManageLocalLibraryActivity.class), new Pair<>("sc_id", "system"))));
        preferences.add(createPreference(R.drawable.ic_mtrl_settings_applications, getString(R.string.mod_settings), getString(R.string.change_general_mod_settings), new ActivityLauncher(new Intent(getApplicationContext(), ConfigActivity.class))));
        preferences.add(createPreference(R.drawable.ic_mtrl_folder, getString(R.string.open_working_directory), getString(R.string.open_sketchware_pro_s_directory_and_edit_files_in_it), v -> openWorkingDirectory()));
        preferences.add(createPreference(R.drawable.ic_mtrl_apk_document, getString(R.string.sign_an_apk_file_with_testkey), getString(R.string.sign_an_already_existing_apk_file), v -> signApkFileDialog()));
        preferences.add(createPreference(R.drawable.ic_mtrl_article, getString(R.string.design_drawer_menu_title_logcat_reader), getString(R.string.design_drawer_menu_subtitle_logcat_reader), new ActivityLauncher(new Intent(getApplicationContext(), LogReaderActivity.class))));
        preferences.forEach(content::addView);
    }

    private View.OnClickListener openSettingsActivity(String fragmentTag) {
        return v -> {
            Intent intent = new Intent(v.getContext(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.FRAGMENT_TAG_EXTRA, fragmentTag);
            v.getContext().startActivity(intent);
        };
    }

    private LibraryItemView createPreference(int icon, String title, String desc, View.OnClickListener listener) {
        LibraryItemView preference = new LibraryItemView(this);
        preference.enabled.setVisibility(View.GONE);
        preference.icon.setImageResource(icon);
        preference.title.setText(title);
        preference.description.setText(desc);
        preference.setOnClickListener(listener);
        return preference;
    }

    private void openWorkingDirectory() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT;
        properties.root = getFilesDir().getParentFile();
        properties.error_dir = getExternalCacheDir();
        properties.extensions = null;
        FilePickerDialog dialog = new FilePickerDialog(this, properties, R.style.RoundedCornersDialog);
        dialog.setTitle(R.string.select_an_entry_to_modify);
        dialog.setDialogSelectionListener(files -> {
            boolean isDirectory = new File(files[0]).isDirectory();
            if (files.length > 1 || isDirectory) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.select_an_action)
                        .setSingleChoiceItems(new String[]{getString(R.string.common_word_delete)}, -1, (actionDialog, which) -> {
                            new MaterialAlertDialogBuilder(this)
                                    .setTitle(getString(R.string.common_word_delete) + (isDirectory ? getString(R.string.common_word_folder) : getString(R.string.common_word_file)) + "?")
                                    .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this) + (isDirectory ? getString(R.string.common_word_folder) : getString(R.string.common_word_file)) + getString(R.string.permanently_this_cannot_be_undone))
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
                                    Intent intent = new Intent(getApplicationContext(), SrcCodeEditor.class);
                                    intent.putExtra("title", Uri.parse(files[0]).getLastPathSegment());
                                    intent.putExtra("content", files[0]);
                                    intent.putExtra("xml", "");
                                    startActivity(intent);
                                }
                                case 1 -> new MaterialAlertDialogBuilder(this)
                                        .setTitle(R.string.delete_file)
                                        .setMessage(R.string.are_you_sure_you_want_to_delete_this_file_permanently_this_cannot_be_undone)
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

    private void signApkFileDialog() {
        boolean[] isAPKSelected = {false};
        MaterialAlertDialogBuilder apkPathDialog = new MaterialAlertDialogBuilder(this);
        apkPathDialog.setTitle(getString(R.string.sign_apk_with_testkey));

        DialogSelectApkToSignBinding binding = DialogSelectApkToSignBinding.inflate(getLayoutInflater());
        View testkey_root = binding.getRoot();
        TextView apk_path_txt = binding.apkPathTxt;

        binding.selectFile.setOnClickListener(v -> {
            DialogProperties properties = new DialogProperties();
            properties.selection_mode = DialogConfigs.SINGLE_MODE;
            properties.selection_type = DialogConfigs.FILE_SELECT;
            properties.extensions = new String[]{"apk"};
            FilePickerDialog dialog = new FilePickerDialog(this, properties, R.style.RoundedCornersDialog);
            dialog.setDialogSelectionListener(files -> {
                isAPKSelected[0] = true;
                apk_path_txt.setText(files[0]);
            });
            dialog.show();
        });

        apkPathDialog.setPositiveButton(getString(R.string.common_word_continue), (v, which) -> {
            if (!isAPKSelected[0]) {
                SketchwareUtil.toast(getString(R.string.please_select_an_apk_file_to_sign), Toast.LENGTH_SHORT);
                shakeView(binding.selectFile);
                return;
            }
            String input_apk_path = Helper.getText(apk_path_txt);
            String output_apk_file_name = Uri.fromFile(new File(input_apk_path)).getLastPathSegment();
            String output_apk_path = new File(Environment.getExternalStorageDirectory(),
                    "sketchware/signed_apk/" + output_apk_file_name).getAbsolutePath();

            if (new File(output_apk_path).exists()) {
                MaterialAlertDialogBuilder confirmOverwrite = new MaterialAlertDialogBuilder(this);
                confirmOverwrite.setIcon(R.drawable.color_save_as_new_96);
                confirmOverwrite.setTitle(getString(R.string.file_exists));
                confirmOverwrite.setMessage(getString(R.string.an_apk_named) + output_apk_file_name + " already exists at /sketchware/signed_apk/.  Overwrite it?");

                confirmOverwrite.setNegativeButton(Helper.getResString(R.string.common_word_cancel), null);
                confirmOverwrite.setPositiveButton(Helper.getResString(R.string.common_word_overwrite), (view, which1) -> {
                    v.dismiss();
                    signApkFileWithDialog(input_apk_path, output_apk_path, true,
                            null, null, null, null);
                });
                confirmOverwrite.show();
            } else {
                signApkFileWithDialog(input_apk_path, output_apk_path, true,
                        null, null, null, null);
            }
        });

        apkPathDialog.setNegativeButton(Helper.getResString(R.string.common_word_cancel), null);

        apkPathDialog.setView(testkey_root);
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
                        tv_log.setText(Helper.getText(tv_log) + line));

                if (useTestkey) {
                    signer.signWithTestKey(inputApkPath, outputApkPath, callback);
                } else {
                    signer.signWithKeyStore(inputApkPath, outputApkPath,
                            keyStorePath, keyStorePassword, keyStoreKeyAlias, keyPassword, callback);
                }

                runOnUiThread(() -> {
                    if (ApkSigner.LogCallback.errorCount.get() == 0) {
                        building_dialog.dismiss();
                        SketchwareUtil.toast(Helper.getResString(R.string.successfully_saved_signed_apk)
                                        + Uri.fromFile(new File(outputApkPath)).getLastPathSegment(),
                                Toast.LENGTH_LONG);
                    } else {
                        tv_progress.setText(getString(R.string.an_error_occurred_check_the_log_for_more_details));
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
