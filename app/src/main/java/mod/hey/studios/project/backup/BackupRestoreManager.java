package mod.hey.studios.project.backup;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

import com.besome.sketch.fragments.ProjectsFragment;

import com.oneskyer.library.model.DialogConfigs;
import com.oneskyer.library.model.DialogProperties;
import com.oneskyer.library.view.FilePickerDialog;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.ProgressMsgBoxBinding;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import a.a.a.aB;
import a.a.a.lC;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BackupRestoreManager {

    private final Activity act;

    // Needed to refresh the project list after restoring
    private ProjectsFragment projectsFragment;

    private HashMap<Integer, Boolean> backupDialogStates;

    public BackupRestoreManager(Activity act) {
        this.act = act;
    }

    public BackupRestoreManager(Activity act, ProjectsFragment projectsFragment) {
        this.act = act;
        this.projectsFragment = projectsFragment;
    }

    public static String getRestoreIntegratedLocalLibrariesMessage(boolean restoringMultipleBackups, int currentRestoringIndex, int totalAmountOfBackups, String filename) {
        if (!restoringMultipleBackups) {
            return "Looks like the backup file you selected contains some Local libraries. Do you want to copy them to your local_libs directory (if they do not already exist)?";
        } else {
            return "Looks like backup file " + filename + " (" + (currentRestoringIndex + 1) + " out of " + totalAmountOfBackups + ") contains some Local libraries. Do you want to copy them to your local_libs directory (if they do not already exist)?";
        }
    }

    public void backup(String sc_id, String project_name) {
        final String localLibrariesTag = "local libraries";
        final String customBlocksTag = "Custom Blocks";
        backupDialogStates = new HashMap<>();
        backupDialogStates.put(0, false);
        backupDialogStates.put(1, false);

        aB dialog = new aB(act);
        dialog.a(R.drawable.ic_backup);
        dialog.b(Helper.getResString(R.string.backup_options));

        LinearLayout checkboxContainer = new LinearLayout(act);
        checkboxContainer.setOrientation(LinearLayout.VERTICAL);
        checkboxContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        int dip = (int) SketchwareUtil.getDip(8);
        checkboxContainer.setPadding(dip, dip, dip, dip);

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            int index;
            Object tag = buttonView.getTag();
            if (tag instanceof String) {
                switch ((String) tag) {
                    case localLibrariesTag:
                        index = 0;
                        break;

                    case customBlocksTag:
                        index = 1;
                        break;

                    default:
                        return;
                }
                backupDialogStates.put(index, isChecked);
            }
        };

        CheckBox includeLocalLibraries = new CheckBox(act);
        includeLocalLibraries.setTag(localLibrariesTag);
        includeLocalLibraries.setText(R.string.include_used_local_libraries);
        includeLocalLibraries.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        includeLocalLibraries.setOnCheckedChangeListener(listener);
        checkboxContainer.addView(includeLocalLibraries);

        CheckBox includeUsedCustomBlocks = new CheckBox(act);
        includeUsedCustomBlocks.setTag(customBlocksTag);
        includeUsedCustomBlocks.setText(R.string.include_used_custom_blocks);
        includeUsedCustomBlocks.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        includeUsedCustomBlocks.setOnCheckedChangeListener(listener);
        checkboxContainer.addView(includeUsedCustomBlocks);

        dialog.a(checkboxContainer);
        dialog.b(Helper.getResString(R.string.back_up), v -> {
            dialog.dismiss();
            doBackup(sc_id, project_name);
        });
        dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
        dialog.show();
    }

    private void doBackup(String sc_id, String project_name) {
        new BackupAsyncTask(new WeakReference<>(act), sc_id, project_name, backupDialogStates)
                .execute("");
    }

    /*** Restore ***/

    public void restore() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.error_dir = Environment.getExternalStorageDirectory();
        properties.offset = new File(BackupFactory.getBackupDir());
        properties.extensions = new String[]{BackupFactory.EXTENSION};

        FilePickerDialog fpd = new FilePickerDialog(act, properties);
        fpd.setTitle(Helper.getResString(R.string.select_backups_to_restore) + BackupFactory.EXTENSION + ")");
        fpd.setDialogSelectionListener(files -> {
            for (int i = 0; i < files.length; i++) {
                String backupFilePath = files[i];

                if (BackupFactory.zipContainsFile(backupFilePath, "local_libs")) {
                    boolean restoringMultipleBackups = files.length > 1;

                    new MaterialAlertDialogBuilder(act)
                         .setTitle(R.string.common_word_warning)
                         .setMessage(getRestoreIntegratedLocalLibrariesMessage(restoringMultipleBackups, i, files.length,
                               FileUtil.getFileNameNoExtension(backupFilePath)))
                         .setPositiveButton(R.string.common_word_copy, (dialog, which) -> doRestore(backupFilePath, true))
                         .setNegativeButton(R.string.don_t_copy, (dialog, which) -> doRestore(backupFilePath, false))
                         .setNeutralButton(R.string.common_word_cancel, null)
                         .show();

                } else {
                    doRestore(backupFilePath, false);
                }
            }
        });

        fpd.show();
    }

    public void doRestore(String file, boolean restoreLocalLibs) {
        new RestoreAsyncTask(new WeakReference<>(act), file, restoreLocalLibs, projectsFragment).execute("");
    }

    private static class BackupAsyncTask extends AsyncTask<String, Integer, String> {

        private final String sc_id;
        private final String project_name;
        private final HashMap<Integer, Boolean> options;
        private final WeakReference<Activity> activityWeakReference;
        private BackupFactory bm;
        private AlertDialog dlg;

        BackupAsyncTask(WeakReference<Activity> activityWeakReference, String sc_id, String project_name, HashMap<Integer, Boolean> options) {
            this.activityWeakReference = activityWeakReference;
            this.sc_id = sc_id;
            this.project_name = project_name;
            this.options = options;
        }

        @Override
        protected void onPreExecute() {
            ProgressMsgBoxBinding loadingDialogBinding = ProgressMsgBoxBinding.inflate(LayoutInflater.from(activityWeakReference.get()));
            loadingDialogBinding.tvProgress.setText(Helper.getResString(R.string.creating_backup));
            dlg = new MaterialAlertDialogBuilder(activityWeakReference.get())
                  .setTitle(R.string.please_wait)
                  .setCancelable(false)
                  .setView(loadingDialogBinding.getRoot())
                  .create();
            dlg.show();
        }

        @Override
        protected String doInBackground(String... params) {
            bm = new BackupFactory(sc_id);
            bm.setBackupLocalLibs(options.get(0));
            bm.setBackupCustomBlocks(options.get(1));

            bm.backup(project_name);

            return "";
        }

        @Override
        protected void onPostExecute(String _result) {
            dlg.dismiss();

            if (bm.getOutFile() != null) {
                SketchwareUtil.toast(Helper.getResString(R.string.successfully_created_backup_to) + bm.getOutFile().getAbsolutePath());
            } else {
                SketchwareUtil.toastError("Error: " + bm.error, Toast.LENGTH_LONG);
            }
        }
    }

    private static class RestoreAsyncTask extends AsyncTask<String, Integer, String> {

        private final WeakReference<Activity> activityWeakReference;
        private final String file;
        private final ProjectsFragment projectsFragment;
        private final boolean restoreLocalLibs;
        private BackupFactory bm;
        private AlertDialog dlg;
        private boolean error = false;

        RestoreAsyncTask(WeakReference<Activity> activityWeakReference, String file, boolean restoreLocalLibraries, ProjectsFragment projectsFragment) {
            this.activityWeakReference = activityWeakReference;
            this.file = file;
            this.projectsFragment = projectsFragment;
            restoreLocalLibs = restoreLocalLibraries;
        }

        @Override
        protected void onPreExecute() {
            ProgressMsgBoxBinding loadingDialogBinding = ProgressMsgBoxBinding.inflate(LayoutInflater.from(activityWeakReference.get()));
            loadingDialogBinding.tvProgress.setText(Helper.getResString(R.string.restoring));
            dlg = new MaterialAlertDialogBuilder(activityWeakReference.get())
                  .setTitle(R.string.please_wait)
                  .setCancelable(false)
                  .setView(loadingDialogBinding.getRoot())
                  .create();
            dlg.show();
        }

        @Override
        protected String doInBackground(String... params) {
            bm = new BackupFactory(lC.b());
            bm.setBackupLocalLibs(restoreLocalLibs);

            try {
                bm.restore(new File(file));
            } catch (Exception e) {
                bm.error = e.getMessage();
                error = true;
            }

            return "";
        }

        @Override
        protected void onPostExecute(String _result) {
            dlg.dismiss();

            if (!bm.isRestoreSuccess() || error) {
                SketchwareUtil.toastError(Helper.getResString(R.string.couldn_t_restore) + bm.error, Toast.LENGTH_LONG);
            } else if (projectsFragment != null) {
                projectsFragment.refreshProjectsList();
                SketchwareUtil.toast(Helper.getResString(R.string.restored_successfully));
            } else {
                SketchwareUtil.toast(Helper.getResString(R.string.refresh_to_see_the_project), Toast.LENGTH_LONG);
            }
        }
    }
}
