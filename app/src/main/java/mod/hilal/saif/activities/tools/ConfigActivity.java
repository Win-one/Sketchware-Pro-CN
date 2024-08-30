package mod.hilal.saif.activities.tools;

import static mod.SketchwareUtil.dpToPx;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.annotations.NonNull;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.PrefencesContentAppbarBinding;
import com.topjohnwu.superuser.Shell;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sketchware.remod.databinding.DialogCreateNewFileLayoutBinding;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;
import mod.jbk.util.LogUtil;

import dev.trindadedev.lib.ui.components.preference.*;

public class ConfigActivity extends AppCompatActivity {

    public static final File SETTINGS_FILE = new File(FileUtil.getExternalStorageDir(), ".sketchware/data/settings.json");
    public static final String SETTING_ALWAYS_SHOW_BLOCKS = "always-show-blocks";
    public static final String SETTING_BACKUP_DIRECTORY = "backup-dir";
    public static final String SETTING_ROOT_AUTO_INSTALL_PROJECTS = "root-auto-install-projects";
    public static final String SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING = "root-auto-open-after-installing";
    public static final String SETTING_BACKUP_FILENAME = "backup-filename";
    public static final String SETTING_LEGACY_CODE_EDITOR = "legacy-ce";
    public static final String SETTING_SHOW_BUILT_IN_BLOCKS = "built-in-blocks";
    public static final String SETTING_SHOW_EVERY_SINGLE_BLOCK = "show-every-single-block";
    public static final String SETTING_USE_NEW_VERSION_CONTROL = "use-new-version-control";
    public static final String SETTING_USE_ASD_HIGHLIGHTER = "use-asd-highlighter";
    public static final String SETTING_SKIP_MAJOR_CHANGES_REMINDER = "skip-major-changes-reminder";
    public static final String SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH = "palletteDir";
    public static final String SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH = "blockDir";
    private HashMap<String, Object> setting_map = new HashMap<>();
    private PrefencesContentAppbarBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = PrefencesContentAppbarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.topAppBar.setTitle(R.string.mod_settings);
        binding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());

        if (FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            setting_map = readSettings();
            if (!setting_map.containsKey(SETTING_SHOW_BUILT_IN_BLOCKS) || !setting_map.containsKey(SETTING_ALWAYS_SHOW_BLOCKS)) {
                restoreDefaultSettings();
            }
        } else {
            restoreDefaultSettings();
        }
        initialize();
    }

    public static String getBackupPath() {
        if (FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            HashMap<String, Object> settings = readSettings();
            if (settings.containsKey(SETTING_BACKUP_DIRECTORY)) {
                Object value = settings.get(SETTING_BACKUP_DIRECTORY);
                if (value instanceof String) {
                    return (String) value;
                } else {
                    SketchwareUtil.toastError("Detected invalid preference "
                                    + "for backup directory. Restoring defaults",
                            Toast.LENGTH_LONG);
                    settings.remove(SETTING_BACKUP_DIRECTORY);
                    FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
                }
            }
        }
        return "/.sketchware/backups/";
    }

    public static String getStringSettingValueOrSetAndGet(String settingKey, String toReturnAndSetIfNotFound) {
        HashMap<String, Object> settings = readSettings();

        Object value = settings.get(settingKey);
        if (value instanceof String) {
            return (String) value;
        } else {
            settings.put(settingKey, toReturnAndSetIfNotFound);
            FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));

            return toReturnAndSetIfNotFound;
        }
    }

    public static String getBackupFileName() {
        if (FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            HashMap<String, Object> settings = new Gson().fromJson(FileUtil.readFile(SETTINGS_FILE.getAbsolutePath()), Helper.TYPE_MAP);
            if (settings.containsKey(SETTING_BACKUP_FILENAME)) {
                Object value = settings.get(SETTING_BACKUP_FILENAME);
                if (value instanceof String) {
                    return (String) value;
                } else {
                    SketchwareUtil.toastError(Helper.getResString(R.string.detected_invalid_preference)
                                    + Helper.getResString(R.string.backup_filename_restoring_defaults),
                            Toast.LENGTH_LONG);
                    settings.remove(SETTING_BACKUP_FILENAME);
                    FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
                }
            }
        }
        return "$projectName v$versionName ($pkgName, $versionCode) $time(yyyy-MM-dd'T'HHmmss)";
    }

    public static boolean isLegacyCeEnabled() {
        /* The legacy Code Editor is specifically opt-in */
        if (!FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            return false;
        }

        HashMap<String, Object> settings = readSettings();
        if (settings.containsKey(SETTING_LEGACY_CODE_EDITOR)) {
            Object value = settings.get(SETTING_LEGACY_CODE_EDITOR);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else {
                SketchwareUtil.toastError(Helper.getResString(R.string.detected_invalid_preference_for_legacy)
                                + Helper.getResString(R.string.code_editor_restoring_defaults),
                        Toast.LENGTH_LONG);
                settings.remove(SETTING_LEGACY_CODE_EDITOR);
                FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
            }
        }
        return false;
    }

    public static boolean isSettingEnabled(String keyName) {
        if (!FileUtil.isExistFile(SETTINGS_FILE.getAbsolutePath())) {
            return false;
        }

        HashMap<String, Object> settings = readSettings();
        if (settings.containsKey(keyName)) {
            Object value = settings.get(keyName);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else {
                SketchwareUtil.toastError(Helper.getResString(R.string.detected_invalid_preference_restoring_defaults),
                        Toast.LENGTH_LONG);
                settings.remove(keyName);
                FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
            }
        }
        return false;
    }

    public static void setSetting(String key, Object value) {
        HashMap<String, Object> settings = readSettings();
        settings.put(key, value);
        FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
    }

    @NonNull
    private static HashMap<String, Object> readSettings() {
        HashMap<String, Object> settings;

        if (SETTINGS_FILE.exists()) {
            Exception toLog;

            try {
                settings = new Gson().fromJson(FileUtil.readFile(SETTINGS_FILE.getAbsolutePath()), Helper.TYPE_MAP);

                if (settings != null) {
                    return settings;
                }

                toLog = new NullPointerException("settings == null");
                // fall-through to shared error handler
            } catch (JsonParseException e) {
                toLog = e;
                // fall-through to shared error handler
            }

            SketchwareUtil.toastError(Helper.getResString(R.string.couldn_t_parse_mod_settings_restoring_defaults));
            LogUtil.e("ConfigActivity", "Failed to parse Mod Settings.", toLog);
        }
        settings = new HashMap<>();
        restoreDefaultSettings(settings);

        return settings;
    }

    private static void restoreDefaultSettings(HashMap<String, Object> settings) {
        settings.clear();

        List<String> keys = Arrays.asList(SETTING_ALWAYS_SHOW_BLOCKS,
                SETTING_BACKUP_DIRECTORY,
                SETTING_LEGACY_CODE_EDITOR,
                SETTING_ROOT_AUTO_INSTALL_PROJECTS,
                SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING,
                SETTING_SHOW_BUILT_IN_BLOCKS,
                SETTING_SHOW_EVERY_SINGLE_BLOCK,
                SETTING_USE_NEW_VERSION_CONTROL,
                SETTING_USE_ASD_HIGHLIGHTER,
                SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH,
                SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH);

        for (String key : keys) {
            settings.put(key, getDefaultValue(key));
        }
        FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(settings));
    }

    public static Object getDefaultValue(String key) {
        return switch (key) {
            case SETTING_ALWAYS_SHOW_BLOCKS, SETTING_LEGACY_CODE_EDITOR,
                 SETTING_ROOT_AUTO_INSTALL_PROJECTS, SETTING_SHOW_BUILT_IN_BLOCKS,
                 SETTING_SHOW_EVERY_SINGLE_BLOCK, SETTING_USE_NEW_VERSION_CONTROL,
                 SETTING_USE_ASD_HIGHLIGHTER -> false;
            case SETTING_BACKUP_DIRECTORY -> "/.sketchware/backups/";
            case SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING -> true;
            case SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH ->
                    "/.sketchware/resources/block/My Block/palette.json";
            case SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH ->
                    "/.sketchware/resources/block/My Block/block.json";
            default -> throw new IllegalArgumentException("Unknown key '" + key + "'!");
        };
    }

    @SuppressLint("SetTextI18n")
    private void initialize() {
        addSwitchPreference(getString(R.string.built_in_blocks),
                getString(R.string.may_slow_down_loading_blocks_in_logic_editor),
                SETTING_SHOW_BUILT_IN_BLOCKS,
                false);
        addSwitchPreference(getString(R.string.show_all_blocks_of_palettes),
                getString(R.string.all_variable_blocks_will_be_visible),
                SETTING_ALWAYS_SHOW_BLOCKS,
                false);
        addSwitchPreference(getString(R.string.show_all_blocks_of_palettes),
                getString(R.string.every_single_available_block_will_be_shown),
                SETTING_SHOW_EVERY_SINGLE_BLOCK,
                false);
        addTextInputPreference(getString(R.string.backup_directory),
                getString(R.string.the_default_directory), v -> {
                    DialogCreateNewFileLayoutBinding dialogBinding = DialogCreateNewFileLayoutBinding.inflate(getLayoutInflater());
                    EditText inputText = dialogBinding.inputText;
                    inputText.setText(getBackupPath());
                    AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                            .setView(dialogBinding.getRoot())
                            .setIcon(R.drawable.ic_folder_48dp)
                            .setTitle(R.string.backup_directory)
                            .setMessage(R.string.directory_inside_internal_storage)
                            .setNegativeButton(R.string.common_word_cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(R.string.common_word_save, null)
                            .create();

                    dialogBinding.chipGroupTypes.setVisibility(View.GONE);
                    dialog.setOnShowListener(dialogInterface -> {
                        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(view -> {
                            setSetting(SETTING_BACKUP_DIRECTORY, inputText.getText().toString());
                            SketchwareUtil.toast(getString(R.string.common_word_saved));
                            dialog.dismiss();
                        });

                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        inputText.requestFocus();
                    });
                    dialog.show();
                });
        addSwitchPreference(getString(R.string.use_legacy_code_editor),
                getString(R.string.enables_old_code_editor),
                SETTING_LEGACY_CODE_EDITOR,
                false);
        addSwitchPreference(getString(R.string.install_projects_with_root_access), getString(R.string.automatically_installs_project_apks),
                SETTING_ROOT_AUTO_INSTALL_PROJECTS, false, (buttonView, isChecked) -> {
                    if (isChecked) {
                        Shell.getShell(shell -> {
                            if (!shell.isRoot()) {
                                SketchwareUtil.toastError("Couldn't acquire root access");
                                buttonView.setChecked(false);
                            }
                        });
                    }
                });
        addSwitchPreference(getString(R.string.launch_projects_after_installing),
                getString(R.string.opens_projects_automatically),
                SETTING_ROOT_AUTO_OPEN_AFTER_INSTALLING,
                true);
        addSwitchPreference(getString(R.string.use_new_version_control),
                getString(R.string.enables_custom_version_code_and_name_for_projects),
                SETTING_USE_NEW_VERSION_CONTROL,
                false);
        addSwitchPreference(getString(R.string.enable_block_text_input_highlighting),
                getString(R.string.enables_syntax_highlighting_while_editing_blocks_text_parameters),
                SETTING_USE_ASD_HIGHLIGHTER,
                false);
        addTextInputPreference(getString(R.string.backup_filename_format),
                getString(R.string.default_is_projectname_v_versionname_pkgname_versioncode_time), v -> {
                    DialogCreateNewFileLayoutBinding dialogBinding = DialogCreateNewFileLayoutBinding.inflate(getLayoutInflater());
                    EditText inputText = dialogBinding.inputText;
                    inputText.setText(getBackupFileName());

                    AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                            .setView(dialogBinding.getRoot())
                            .setIcon(R.drawable.file_48_blue)
                            .setTitle(R.string.backup_filename_format)
                            .setMessage("This defines how SWB backup files get named.\n" +
                                    "Available variables:\n" +
                                    " - $projectName - Project name\n" +
                                    " - $versionCode - App version code\n" +
                                    " - $versionName - App version name\n" +
                                    " - $pkgName - App package name\n" +
                                    " - $timeInMs - Time during backup in milliseconds\n" +
                                    "\n" +
                                    "Additionally, you can format your own time like this using Java's date formatter syntax:\n" +
                                    "$time(yyyy-MM-dd'T'HHmmss)\n")
                            .setNegativeButton(R.string.common_word_cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(R.string.common_word_save, null)
                            .setNeutralButton(R.string.common_word_reset, (dialogInterface, which) -> {
                                setting_map.remove(SETTING_BACKUP_FILENAME);
                                FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
                                SketchwareUtil.toast(getString(R.string.reset_to_default_complete));
                            }).create();

                    dialogBinding.chipGroupTypes.setVisibility(View.GONE);
                    dialog.setOnShowListener(dialogInterface -> {
                        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                        positiveButton.setOnClickListener(view -> {
                            setting_map.put(SETTING_BACKUP_FILENAME, inputText.getText().toString());
                            FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
                            SketchwareUtil.toast(getString(R.string.common_word_saved));
                            dialog.dismiss();
                        });
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        inputText.requestFocus();
                    });
                    dialog.show();
                });
    }

    private void addSwitchPreference(String title, String description, String keyName, boolean defaultValue) {
        addSwitchPreference(title, description, keyName, defaultValue, null);
    }

    private void addSwitchPreference(String title, String description, String keyName, boolean defaultValue, CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        PreferenceSwitch preferenceSwitch = new PreferenceSwitch(this);
        preferenceSwitch.setTitle(title);
        preferenceSwitch.setDescription(description);
        preferenceSwitch.setValue(defaultValue);
        
        preferenceSwitch.setSwitchChangedListener((buttonView, isChecked) -> {
             ConfigActivity.setSetting(keyName, isChecked);
             if (onCheckedChangeListener != null) {
                 onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
             }
        });
        
        if (setting_map.containsKey(keyName)) {
            Object value = setting_map.get(keyName);
            if (value == null) {
                setting_map.remove(keyName);
            } else {
                if (value instanceof Boolean) {
                    preferenceSwitch.setValue((boolean) value);
                } else {
                    SketchwareUtil.toastError("Detected invalid value for preference \"" + title + "\". Restoring defaults");
                    setting_map.remove(keyName);
                    FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
                }
            }
        } else {
           setting_map.put(keyName, defaultValue);
           preferenceSwitch.setValue(defaultValue);
           FileUtil.writeFile(SETTINGS_FILE.getAbsolutePath(), new Gson().toJson(setting_map));
        }
        binding.content.addView(preferenceSwitch);
    }

    private void addTextInputPreference(String title, String description, View.OnClickListener listener) {
        Preference preference = new Preference(this);
        preference.setTitle(title);
        preference.setDescription(description);
        preference.setPreferenceClickListener(listener);
        binding.content.addView(preference);
    }

    private void restoreDefaultSettings() {
        restoreDefaultSettings(setting_map);
    }
}
