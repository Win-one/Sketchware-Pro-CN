package mod.hey.studios.activity.managers.java;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mod.hey.studios.code.SrcCodeEditor;
import mod.hey.studios.util.Helper;
import pro.sketchware.R;
import pro.sketchware.databinding.DialogCreateNewFileLayoutBinding;
import pro.sketchware.databinding.DialogInputLayoutBinding;
import pro.sketchware.databinding.ManageFileBinding;
import pro.sketchware.databinding.ManageJavaItemHsBinding;
import pro.sketchware.utility.FilePathUtil;
import pro.sketchware.utility.FileResConfig;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;

public class ManageJavaActivity extends BaseAppCompatActivity {

    // works for both Java & Kotlin files
    private static final String PACKAGE_DECL_REGEX = "package (.*?);?\\n";

    private static final String ACTIVITY_TEMPLATE =
            "package %s;\n" +
                    "\n" +
                    "import android.app.Activity;\n" +
                    "import android.os.Bundle;\n" +
                    "\n" +
                    "public class %s extends Activity {\n" +
                    "\n" +
                    "    @Override\n" +
                    "    protected void onCreate(Bundle savedInstanceState) {\n" +
                    "        super.onCreate(savedInstanceState);\n" +
                    "    }" +
                    "\n" +
                    "}\n";

    private static final String CLASS_TEMPLATE =
            "package %s;\n" +
                    "\n" +
                    "public class %s {\n" +
                    "    \n" +
                    "}\n";

    private static final String KT_ACTIVITY_TEMPLATE =
            "package %s\n" +
                    "\n" +
                    "import android.app.Activity\n" +
                    "import android.os.Bundle\n" +
                    "\n" +
                    "class %s : Activity() {\n" +
                    "\n" +
                    "    override fun onCreate(savedInstanceState: Bundle?) {\n" +
                    "        super.onCreate(savedInstanceState)\n" +
                    "    }" +
                    "\n" +
                    "}\n";

    private static final String KT_CLASS_TEMPLATE =
            "package %s\n" +
                    "\n" +
                    "class %s {\n" +
                    "    \n" +
                    "}\n";

    private final ArrayList<String> currentTree = new ArrayList<>();
    private String current_path;
    private FilePathUtil fpu;
    private FileResConfig frc;
    private String sc_id;
    private FilesAdapter filesAdapter;

    ManageFileBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ManageFileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sc_id = getIntent().getStringExtra("sc_id");
        Helper.fixFileprovider();
        setupUI();
        frc = new FileResConfig(sc_id);
        fpu = new FilePathUtil();
        current_path = Uri.parse(fpu.getPathJava(sc_id)).getPath();
        refresh();
    }

    @Override
    public void onBackPressed() {
        if (Objects.equals(
                Uri.parse(current_path).getPath(),
                Uri.parse(fpu.getPathJava(sc_id)).getPath())) {
            super.onBackPressed();
        } else {
            current_path = current_path.substring(0, current_path.lastIndexOf("/"));
            refresh();
        }
    }

    private void setupUI() {
        binding.topAppBar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        binding.topAppBar.setTitle(R.string.java_kotlin_manager);
        binding.showOptionsButton.setOnClickListener(view -> hideShowOptionsButton(false));
        binding.closeButton.setOnClickListener(view -> hideShowOptionsButton(true));
        binding.createNewButton.setOnClickListener(v -> {
            showCreateDialog();
            hideShowOptionsButton(true);
        });
        binding.importNewButton.setOnClickListener(v -> {
            showImportDialog();
            hideShowOptionsButton(true);
        });

    }

    private void hideShowOptionsButton(boolean isHide) {
        binding.optionsLayout.animate()
                .translationY(isHide ? 300 : 0)
                .alpha(isHide ? 0 : 1)
                .setInterpolator(new OvershootInterpolator());

        binding.showOptionsButton.animate()
                .translationY(isHide ? 0 : 300)
                .alpha(isHide ? 1 : 0)
                .setInterpolator(new OvershootInterpolator());
    }

    private String getCurrentPkgName() {
        String pkgName = getIntent().getStringExtra("pkgName");

        try {
            String trimmedPath = Helper.trimPath(fpu.getPathJava(sc_id));
            String substring = current_path.substring(current_path.indexOf(trimmedPath) + trimmedPath.length());

            if (substring.endsWith("/")) {
                substring = substring.substring(0, substring.length() - 1);
            }

            if (substring.startsWith("/")) {
                substring = substring.substring(1);
            }

            String replace = substring.replace("/", ".");
            return replace.isEmpty() ? pkgName : pkgName + "." + replace;
        } catch (Exception e) {
            return pkgName;
        }
    }

    private void showCreateDialog() {
        DialogCreateNewFileLayoutBinding dialogBinding = DialogCreateNewFileLayoutBinding.inflate(getLayoutInflater());
        var inputText = dialogBinding.inputText;

        var dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setTitle(R.string.create_new)
                .setMessage(R.string.file_extension_will_be_added_automatically)
                .setNegativeButton(R.string.common_word_cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(R.string.common_word_create, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            inputText.requestFocus();

            Button positiveButton = ((androidx.appcompat.app.AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> {
                if (Helper.getText(inputText).isEmpty()) {
                    SketchwareUtil.toastError(getString(R.string.invalid_file_name));
                    return;
                }

                String name = Helper.getText(inputText);
                String packageName = getCurrentPkgName();

                String extension;
                String newFileContent;
                int checkedChipId = dialogBinding.chipGroupTypes.getCheckedChipId();
                if (checkedChipId == R.id.chip_java_class) {
                    newFileContent = String.format(CLASS_TEMPLATE, packageName, name);
                    extension = ".java";
                } else if (checkedChipId == R.id.chip_java_activity) {
                    newFileContent = String.format(ACTIVITY_TEMPLATE, packageName, name);
                    extension = ".java";
                } else if (checkedChipId == R.id.chip_kotlin_class) {
                    newFileContent = String.format(KT_CLASS_TEMPLATE, packageName, name);
                    extension = ".kt";
                } else if (checkedChipId == R.id.chip_kotlin_activity) {
                    newFileContent = String.format(KT_ACTIVITY_TEMPLATE, packageName, name);
                    extension = ".kt";
                } else if (checkedChipId == R.id.chip_folder) {
                    FileUtil.makeDir(new File(current_path, name).getAbsolutePath());
                    refresh();
                    SketchwareUtil.toast(getString(R.string.folder_was_created_successfully));
                    dialog.dismiss();
                    return;
                } else {
                    SketchwareUtil.toast(getString(R.string.select_a_file_type));
                    return;
                }

                if (Build.VERSION.SDK_INT < 26 && ".kt".equals(extension)) {
                    SketchwareUtil.toast("Unfortunately you cannot use Kotlin " +
                            "since kotlinc only works on devices with Android 8 and higher, " +
                            "sorry for the inconvenience!", Toast.LENGTH_LONG);
                    return;
                }

                FileUtil.writeFile(new File(current_path, name + extension).getAbsolutePath(), newFileContent);
                refresh();
                SketchwareUtil.toast(getString(R.string.file_was_created_successfully));
                dialog.dismiss();
            });

            dialog.show();

            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            inputText.requestFocus();

            dialogBinding.chipFolder.setVisibility(View.VISIBLE);
            dialogBinding.chipJavaClass.setVisibility(View.VISIBLE);
            dialogBinding.chipJavaActivity.setVisibility(View.VISIBLE);
            dialogBinding.chipKotlinClass.setVisibility(View.VISIBLE);
            dialogBinding.chipKotlinActivity.setVisibility(View.VISIBLE);
        });

        dialog.show();
    }

    private void showImportDialog() {
        DialogProperties properties = new DialogProperties();

        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory();
        properties.error_dir = Environment.getExternalStorageDirectory();
        properties.offset = Environment.getExternalStorageDirectory();
        properties.extensions = new String[]{"java", "kt"};

        FilePickerDialog pickerDialog = new FilePickerDialog(this, properties, R.style.RoundedCornersDialog);

        pickerDialog.setTitle(getString(R.string.select_java_kotlin_file_s));
        pickerDialog.setDialogSelectionListener(selections -> {

            for (String path : selections) {
                String filename = Uri.parse(path).getLastPathSegment();
                String fileContent = FileUtil.readFile(path);

                if (Build.VERSION.SDK_INT < 26 && filename.endsWith(".kt")) {
                    SketchwareUtil.toast("Unfortunately you cannot use Kotlin " +
                            "since kotlinc only works on devices with Android 8 " +
                            "and higher, skipping " + filename + "!");
                    continue;
                }

                if (fileContent.contains("package ")) {
                    fileContent = fileContent.replaceFirst(PACKAGE_DECL_REGEX,
                            "package " + getCurrentPkgName()
                                    + (filename.endsWith(".java") ? ";" : "")
                                    + "\n");
                }

                FileUtil.writeFile(new File(current_path, filename).getAbsolutePath(),
                        fileContent);
                refresh();
            }
        });

        pickerDialog.show();
    }

    private void showRenameDialog(int position) {
        DialogInputLayoutBinding dialogBinding = DialogInputLayoutBinding.inflate(getLayoutInflater());

        var inputText = dialogBinding.inputText;
        var renameOccurrencesCheckBox = dialogBinding.renameOccurrencesCheckBox;

        var dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.common_word_rename + filesAdapter.getFileName(position))
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.common_word_cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton(R.string.common_word_rename, (dialogInterface, i) -> {
                    if (!Helper.getText(inputText).isEmpty()) {
                        if (!filesAdapter.isFolder(position)) {
                            if (frc.getJavaManifestList().contains(filesAdapter.getFullName(position))) {
                                frc.getJavaManifestList().remove(filesAdapter.getFullName(position));
                                FileUtil.writeFile(fpu.getManifestJava(sc_id), new Gson().toJson(frc.listJavaManifest));
                                SketchwareUtil.toast(getString(R.string.note_removed_activity_from_manifest));
                            }

                            if (renameOccurrencesCheckBox.isChecked()) {
                                String fileContent = FileUtil.readFile(filesAdapter.getItem(position));
                                FileUtil.writeFile(filesAdapter.getItem(position),
                                        fileContent.replaceAll(filesAdapter.getFileNameWoExt(position),
                                                FileUtil.getFileNameNoExtension(Helper.getText(inputText))));
                            }
                        }

                        FileUtil.renameFile(filesAdapter.getItem(position), new File(current_path, Helper.getText(inputText)).getAbsolutePath());
                        refresh();
                        SketchwareUtil.toast(getString(R.string.renamed_successfully));
                    }
                    dialogInterface.dismiss();
                })
                .create();

        inputText.setText(filesAdapter.getFileName(position));
        boolean isFolder = filesAdapter.isFolder(position);

        inputText.setText(filesAdapter.getFileName(position));

        if (!isFolder) {
            renameOccurrencesCheckBox.setVisibility(View.VISIBLE);
            renameOccurrencesCheckBox.setText("Rename occurrences of \"" + filesAdapter.getFileNameWoExt(position) + "\" in file");
        }
        dialog.show();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        inputText.requestFocus();
    }

    private void showDeleteDialog(final int position) {
        boolean isInManifest = frc.getJavaManifestList().contains(filesAdapter.getFullName(position));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.common_word_delete + filesAdapter.getFileName(position) + "?")
                .setMessage(R.string.are_you_sure_you_want_to_delete_this + (filesAdapter.isFolder(position) ? "folder" : "file") + "? "
                        + (isInManifest ? getString(R.string.this_will_also_remove_it_from_androidmanifest) : "")
                        + getString(R.string.this_action_cannot_be_undone))
                .setPositiveButton(R.string.common_word_delete, (dialog, which) -> {
                    if (!filesAdapter.isFolder(position) && isInManifest) {
                        frc.getJavaManifestList().remove(filesAdapter.getFullName(position));
                        FileUtil.writeFile(fpu.getManifestJava(sc_id), new Gson().toJson(frc.listJavaManifest));
                    }

                    FileUtil.deleteFile(filesAdapter.getItem(position));
                    refresh();
                    SketchwareUtil.toast(getString(R.string.deleted_successfully));
                })
                .setNegativeButton(R.string.common_word_cancel, null)
                .create()
                .show();
    }

    private void refresh() {
        if (!FileUtil.isExistFile(fpu.getPathJava(sc_id))) {
            FileUtil.makeDir(fpu.getPathJava(sc_id));
            refresh();
        }

        if (!FileUtil.isExistFile(fpu.getManifestJava(sc_id))) {
            FileUtil.writeFile(fpu.getManifestJava(sc_id), "");
            refresh();
        }

        currentTree.clear();
        FileUtil.listDir(current_path, currentTree);
        Helper.sortPaths(currentTree);

        filesAdapter = new FilesAdapter(currentTree);

        binding.filesListRecyclerView.setAdapter(filesAdapter);

        binding.noContentLayout.setVisibility(currentTree.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
        private final List<String> currentTree;

        public FilesAdapter(List<String> currentTree) {
            this.currentTree = currentTree;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ManageJavaItemHsBinding binding = ManageJavaItemHsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            var binding = holder.binding;
            String fileName = getFileName(position);

            holder.binding.title.setText(fileName);

            binding.getRoot().setOnClickListener(view -> {
                if (isFolder(position)) {
                    current_path = filesAdapter.getItem(position);
                    refresh();
                    return;
                }
                goEditFile(position);
            });
            binding.getRoot().setOnLongClickListener(view -> {
                itemContextMenu(view, position, Gravity.CENTER);
                return true;
            });

            if (isFolder(position)) {
                holder.binding.icon.setImageResource(R.drawable.ic_mtrl_folder);
            } else if (fileName.endsWith(".java")) {
                holder.binding.icon.setImageResource(R.drawable.ic_mtrl_java);
            } else if (fileName.endsWith(".kt")) {
                holder.binding.icon.setImageResource(R.drawable.ic_mtrl_kotlin);
            }

            Helper.applyRipple(ManageJavaActivity.this, holder.binding.more);

            holder.binding.more.setOnClickListener(v -> itemContextMenu(v, position, Gravity.RIGHT));
        }

        @Override
        public int getItemCount() {
            return currentTree.size();
        }

        public String getItem(int position) {
            return currentTree.get(position);
        }

        /**
         * Gets the full package name of the Java/Kotlin file.
         *
         * @param position The Java/Kotlin file's position in this adapter's {@code ArrayList}
         * @return The full package name of the Java/Kotlin file
         */
        public String getFullName(int position) {
            String readFile = FileUtil.readFile(getItem(position));

            if (!readFile.contains("package ")) {
                return getFileNameWoExt(position);
            }

            Matcher m = Pattern.compile(PACKAGE_DECL_REGEX).matcher(readFile);
            if (m.find()) {
                return m.group(1) + "." + getFileNameWoExt(position);
            }

            return getFileNameWoExt(position);
        }

        /**
         * @param position The Java file's position in this adapter's {@code ArrayList}
         * @return The file's filename with extension
         * @see ManageJavaActivity.FilesAdapter#getFileNameWoExt(int)
         */
        public String getFileName(int position) {
            String item = getItem(position);
            return item.substring(item.lastIndexOf("/") + 1);
        }

        /**
         * @param position The Java file's position in this adapter's {@code ArrayList}
         * @return The file's filename without extension
         * @see ManageJavaActivity.FilesAdapter#getFileName(int)
         */
        public String getFileNameWoExt(int position) {
            return FileUtil.getFileNameNoExtension(getItem(position));
        }

        public boolean isFolder(int position) {
            return FileUtil.isDirectory(getItem(position));
        }

        public void goEditFile(int position) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), SrcCodeEditor.class);
            intent.putExtra("java", "");
            intent.putExtra("title", getFileName(position));
            intent.putExtra("content", getItem(position));

            startActivity(intent);
        }

        private void itemContextMenu(View v, int position, int gravity) {
            PopupMenu popupMenu = new PopupMenu(ManageJavaActivity.this, v, gravity);
            popupMenu.inflate(R.menu.popup_menu_double);

            Menu popupMenuMenu = popupMenu.getMenu();
            popupMenuMenu.clear();

            boolean isActivityInManifest = frc.getJavaManifestList().contains(getFullName(position));
            boolean isServiceInManifest = frc.getServiceManifestList().contains(getFullName(position));

            if (!isFolder(position)) {
                if (isActivityInManifest) {
                    popupMenuMenu.add(Menu.NONE, 0, Menu.NONE, R.string.remove_activity_from_manifest);
                } else if (!isServiceInManifest) {
                    popupMenuMenu.add(Menu.NONE, 1, Menu.NONE, R.string.add_as_activity_to_manifest);
                }

                if (isServiceInManifest) {
                    popupMenuMenu.add(Menu.NONE, 2, Menu.NONE, R.string.remove_service_from_manifest);
                } else if (!isActivityInManifest) {
                    popupMenuMenu.add(Menu.NONE, 3, Menu.NONE, R.string.add_as_service_to_manifest);
                }

                popupMenuMenu.add(Menu.NONE, 4, Menu.NONE, R.string.common_word_edit);
                popupMenuMenu.add(Menu.NONE, 5, Menu.NONE, R.string.edit_with);
            }

            popupMenuMenu.add(Menu.NONE, 6, Menu.NONE, R.string.common_word_rename);
            popupMenuMenu.add(Menu.NONE, 7, Menu.NONE, R.string.common_word_delete);

            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1 -> {
                        frc.getJavaManifestList().add(getFullName(position));
                        FileUtil.writeFile(fpu.getManifestJava(sc_id), new Gson().toJson(frc.listJavaManifest));
                        SketchwareUtil.toast(getString(R.string.successfully_added) + getFileNameWoExt(position) + getString(R.string.as_activity_to_androidmanifest));
                    }
                    case 0 -> {
                        if (frc.getJavaManifestList().remove(getFullName(position))) {
                            FileUtil.writeFile(fpu.getManifestJava(sc_id), new Gson().toJson(frc.listJavaManifest));
                            SketchwareUtil.toast(getString(R.string.successfully_removed_activity) + getFileNameWoExt(position) + getString(R.string.from_androidmanifest));
                        } else {
                            SketchwareUtil.toast(getString(R.string.activity_was_not_defined_in_androidmanifest));
                        }
                    }
                    case 3 -> {
                        frc.getServiceManifestList().add(getFullName(position));
                        FileUtil.writeFile(fpu.getManifestService(sc_id), new Gson().toJson(frc.listServiceManifest));
                        SketchwareUtil.toast(getString(R.string.successfully_added) + getFileNameWoExt(position) + getString(R.string.as_service_to_androidmanifest));
                    }
                    case 2 -> {
                        if (frc.getServiceManifestList().remove(getFullName(position))) {
                            FileUtil.writeFile(fpu.getManifestService(sc_id), new Gson().toJson(frc.listServiceManifest));
                            SketchwareUtil.toast(getString(R.string.successfully_removed_service) + getFileNameWoExt(position) + getString(R.string.from_androidmanifest));
                        } else {
                            SketchwareUtil.toast(getString(R.string.service_was_not_defined_in_androidmanifest));
                        }
                    }
                    case 4 -> goEditFile(position);
                    case 5 -> {
                        Intent launchIntent = new Intent(Intent.ACTION_VIEW);
                        launchIntent.setDataAndType(Uri.fromFile(new File(getItem(position))), "text/plain");
                        startActivity(launchIntent);
                    }
                    case 6 -> showRenameDialog(position);
                    case 7 -> showDeleteDialog(position);
                    default -> {
                        return false;
                    }
                }

                return true;
            });

            popupMenu.show();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ManageJavaItemHsBinding binding;

            public ViewHolder(ManageJavaItemHsBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
