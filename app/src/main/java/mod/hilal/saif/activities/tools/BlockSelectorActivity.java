package mod.hilal.saif.activities.tools;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.annotations.NonNull;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.MenuActivityBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import dev.aldi.sayuti.block.ExtraBlockClassInfo;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;

public class BlockSelectorActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final File BLOCK_SELECTOR_MENUS_FILE = new File(Environment.getExternalStorageDirectory(), ".sketchware/resources/block/My Block/menu.json");

    private final ArrayList<String> display = new ArrayList<>();
    private ArrayList<String> contents = new ArrayList<>();
    private int current_item;
    private ArrayList<HashMap<String, Object>> data = new ArrayList<>();
    private boolean isNewGroup;
    private HashMap<String, Object> map = new HashMap<>();

    private MenuActivityBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = MenuActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialize();
        initializeLogic();
    }

    private void initialize() {
        fixbug();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.block_selector_menu_manager);
        binding.toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        binding.addVal.setOnClickListener(this);
        binding.dele.setOnClickListener(this);
        binding.edi.setOnClickListener(this);
        binding.add.setOnClickListener(this);
        binding.canc.setOnClickListener(this);
        binding.save.setOnClickListener(this);
        binding.card.setOnClickListener(this);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                _showItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        binding.listv.setOnItemLongClickListener((parent, view, position, id) -> {
            if (current_item != 0) {
                new AlertDialog.Builder(this).setTitle(contents.get(position))
                        .setMessage(getString(R.string.delete_this_item))
                        .setPositiveButton(R.string.common_word_delete, (dialog, which) -> {
                            contents.remove(position);
                            map.put("data", contents);
                            _save_item();
                            _showItem(current_item);
                        })
                        .setNegativeButton(R.string.common_word_cancel, null)
                        .setNeutralButton(getString(R.string.copy_item), (dialog, which) -> {
                            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", contents.get(position)));
                            SketchwareUtil.toast(Helper.getResString(R.string.copied_to_clipboard));
                        })
                        .show();
            }
            return true;
        });
        applyRippleToView(binding.dele, binding.edi, binding.add, binding.canc, binding.save, binding.addVal);
    }

    private void save() {
        Menu menu = binding.toolbar.getMenu();
        if (binding.name.getText().toString().isEmpty()) {
            SketchwareUtil.toast(getString(R.string.enter_a_name));
        } else if (binding.title.getText().toString().isEmpty()) {
            SketchwareUtil.toast(getString(R.string.enter_a_title));
        } else {
            if (isNewGroup) {
                map = new HashMap<>();
                map.put("name", binding.name.getText().toString());
                map.put("title", binding.title.getText().toString());
                map.put("data", new ArrayList<>());
                data.add(map);
                _save_item();
                _refresh_display();
                _fabVisibility(true);
                binding.spinner.setSelection(data.size() - 1);
                AutoTransition autoTransition = new AutoTransition();
                autoTransition.setDuration(200L);
                TransitionManager.beginDelayedTransition(binding.back, autoTransition);
                binding.contai.setVisibility(View.GONE);
                Helper.setViewsVisibility(false, binding.add, binding.edi, binding.dele);
                menu.setGroupVisible(0, true);
                binding.spinner.setEnabled(true);
                binding.listv.setEnabled(true);
                isNewGroup = false;
            } else {
                map.put("name", binding.name.getText().toString());
                map.put("title", binding.title.getText().toString());
                _save_item();
                _refresh_display();
                _fabVisibility(true);
                binding.spinner.setSelection(current_item);
                AutoTransition autoTransition2 = new AutoTransition();
                autoTransition2.setDuration(200L);
                TransitionManager.beginDelayedTransition(binding.back, autoTransition2);
                binding.contai.setVisibility(View.GONE);
                Helper.setViewsVisibility(false, binding.add, binding.edi, binding.dele);
                menu.setGroupVisible(0, true);
                binding.spinner.setEnabled(true);
                binding.listv.setEnabled(true);
            }
            binding.label.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.import_block_selector_menus);
        menu.add(Menu.NONE, 2, Menu.NONE, R.string.export_current_block_selector_menu);
        menu.add(Menu.NONE, 3, Menu.NONE, R.string.export_all_block_selector_menus);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 2:
                ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
                arrayList.add(data.get(current_item));
                FileUtil.writeFile(FileUtil.getExternalStorageDir().concat("/.sketchware/resources/block/export/menu/") + data.get(current_item).get("name") + ".json", new Gson().toJson(arrayList));
                SketchwareUtil.toast("Successfully exported block menu to:\n/Internal storage/.sketchware/resources/block/export", Toast.LENGTH_LONG);
                break;

            case 1:
                openFileExplorerImport();
                break;

            case 3:
                FileUtil.writeFile(FileUtil.getExternalStorageDir().concat("/.sketchware/resources/block/export/menu/") + "All_Menus.json", new Gson().toJson(data));
                SketchwareUtil.toast("Successfully exported block menus to:\n/Internal storage/.sketchware/resources/block/export", Toast.LENGTH_LONG);
                break;

            default:
                return false;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        AutoTransition autoTransition = new AutoTransition();
        autoTransition.setDuration(200L);
        int id = v.getId();
        Menu menu = binding.toolbar.getMenu();
        if (id == R.id.add) {
            _fabVisibility(false);
            TransitionManager.beginDelayedTransition(binding.back, autoTransition);
            binding.name.setText("");
            binding.title.setText("");
            isNewGroup = true;
            Helper.setViewsVisibility(true, binding.add, binding.edi);
            Helper.setViewsVisibility(false, binding.label, binding.dele, binding.contai);
            menu.setGroupVisible(0, false);
            binding.spinner.setEnabled(false);
            binding.listv.setEnabled(false);
        } else if (id == R.id.add_val) {
            if (current_item != 0) {
                if (binding.val.getText().toString().isEmpty()) {
                    SketchwareUtil.toast(getString(R.string.enter_a_value));
                } else {
                    contents.add(binding.val.getText().toString());
                    map.put("data", contents);
                    _save_item();
                    _showItem(current_item);
                    binding.val.setText("");
                }
            } else {
                SketchwareUtil.toastError(getString(R.string.this_menu_can_t_be_modified));
            }
        } else if (id == R.id.dele) {
            if (current_item != 0) {
                new AlertDialog.Builder(this).setMessage(R.string.remove_this_menu_and_its_items)
                        .setPositiveButton(R.string.common_word_remove, (dialog, which) -> {
                            data.remove(binding.spinner.getSelectedItemPosition());
                            _save_item();
                            _refresh_display();
                            _fabVisibility(true);
                            isNewGroup = false;
                            binding.spinner.setSelection(0);
                        })
                        .setNegativeButton(R.string.common_word_cancel, null)
                        .create().show();
            } else {
                SketchwareUtil.toastError(getString(R.string.this_menu_can_t_be_deleted));
            }
        } else if (id == R.id.edi) {
            if (current_item != 0) {
                _fabVisibility(false);
                binding.name.setText(map.get("name").toString());
                binding.title.setText(map.get("title").toString());
                TransitionManager.beginDelayedTransition(binding.back, autoTransition);
                binding.contai.setVisibility(View.VISIBLE);
                Helper.setViewsVisibility(true, binding.add, binding.edi, binding.dele);
                menu.setGroupVisible(0, false);
                binding.spinner.setEnabled(false);
                binding.listv.setEnabled(false);
            } else {
                SketchwareUtil.toastError(getString(R.string.this_menu_can_t_be_modified));
            }
        } else if (id == R.id.save) {
            save();
        } else if (id == R.id.canc) {
            _fabVisibility(true);
            TransitionManager.beginDelayedTransition(binding.back, autoTransition);
            Helper.setViewsVisibility(false, binding.add, binding.edi, binding.dele);
            menu.setGroupVisible(0, true);
            Helper.setViewsVisibility(true, binding.contai, binding.label);
            binding.spinner.setEnabled(true);
            binding.listv.setEnabled(true);
            isNewGroup = false;
        }
    }

    private void applyRippleToView(View... views) {
        for (View view : views) {
            Helper.applyRippleToToolbarView(view);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        ExtraBlockClassInfo.loadEBCI();
    }

    private void initializeLogic() {
        Helper.setViewsVisibility(true, binding.contai, binding.label);
        _readFile();
        if (data.size() != 0) {
            _showItem(0);
        }
    }

    public void openFileExplorerImport() {
        DialogProperties dialogProperties = new DialogProperties();
        dialogProperties.selection_mode = DialogConfigs.SINGLE_MODE;
        dialogProperties.selection_type = DialogConfigs.FILE_SELECT;
        File file = Environment.getExternalStorageDirectory();
        dialogProperties.root = file;
        dialogProperties.error_dir = file;
        dialogProperties.offset = file;
        dialogProperties.extensions = new String[]{"json"};
        FilePickerDialog filePickerDialog = new FilePickerDialog(this, dialogProperties);
        filePickerDialog.setTitle(getString(R.string.select_a_json_file));
        filePickerDialog.setDialogSelectionListener(selections -> {
            String fileContent = FileUtil.readFile(selections[0]);
            if (fileContent.equals("")) {
                SketchwareUtil.toastError(getString(R.string.the_selected_file_is_empty));
            } else if (fileContent.equals("[]")) {
                SketchwareUtil.toastError(getString(R.string.the_selected_file_is_empty));
            } else {
                try {
                    _importMenu(new Gson().fromJson(fileContent, Helper.TYPE_MAP_LIST));
                } catch (Exception e) {
                    SketchwareUtil.toastError(getString(R.string.invalid_json_file));
                }
            }
        });
        filePickerDialog.show();
    }

    public void _importMenu(ArrayList<HashMap<String, Object>> menu) {
        data.addAll(menu);
        FileUtil.writeFile(BLOCK_SELECTOR_MENUS_FILE.getAbsolutePath(), new Gson().toJson(data));
        _readFile();
        if (data.size() != 0) {
            _showItem(0);
        }
        SketchwareUtil.toast(getString(R.string.successfully_imported_menu));
    }

    private void _readFile() {
        data.clear();
        parser:
        {
            if (BLOCK_SELECTOR_MENUS_FILE.exists()) {
                try {
                    data = new Gson().fromJson(FileUtil.readFile(BLOCK_SELECTOR_MENUS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);

                    if (data != null) {
                        break parser;
                    }
                    // fall-through to shared handler
                } catch (JsonParseException e) {
                    // fall-through to shared handler
                }

                SketchwareUtil.showFailedToParseJsonDialog(this, BLOCK_SELECTOR_MENUS_FILE,
                        "Block selector menus", v -> _readFile());
                data = new ArrayList<>();
            }
        }
        for (int i = 0; i < data.size(); i++) {
            if ("typeview".equals(data.get(i).get("name"))) {
                _refresh_display();
                return;
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "typeview");
        map.put("title", "select type :");
        map.put("data", new ArrayList<>(Helper.createStringList(
                "View", "ViewGroup",
                "LinearLayout", "RelativeLayout",
                "ScrollView", "HorizontalScrollView",
                "TextView", "EditText", "Button",
                "RadioButton", "CheckBox", "Switch",
                "ImageView", "SeekBar", "ListView",
                "Spinner", "WebView", "MapView",
                "ProgressBar"
        )));
        data.add(0, map);
        _refresh_display();
    }

    private void fixbug() {
        ViewGroup viewGroup = (ViewGroup) binding.name.getParent().getParent().getParent();
        viewGroup.removeView((ViewGroup) binding.name.getParent().getParent());
        viewGroup.removeView((ViewGroup) binding.title.getParent().getParent());
        ((ViewGroup) binding.name.getParent()).removeView(binding.name);
        ((ViewGroup) binding.title.getParent()).removeView(binding.title);
        viewGroup.addView(binding.title, 0);
        binding.title.setHint("");
        TextView textView = new TextView(this);
        textView.setTextColor(855638016);
        textView.setPadding((int) SketchwareUtil.getDip(8),
                (int) SketchwareUtil.getDip(4),
                (int) SketchwareUtil.getDip(8),
                (int) SketchwareUtil.getDip(0));
        textView.setText("Title");
        viewGroup.addView(textView, 0);
        viewGroup.addView(binding.name, 0);
        binding.name.setHint("");
        TextView textView2 = new TextView(this);
        textView2.setTextColor(855638016);
        textView2.setPadding((int) SketchwareUtil.getDip(8), (int) SketchwareUtil.getDip(4), (int) SketchwareUtil.getDip(8), (int) SketchwareUtil.getDip(0));
        textView2.setText("Name");
        viewGroup.addView(textView2, 0);
    }

    private void _showItem(int d) {
        current_item = d;
        map = data.get(d);
        binding.name.setText(map.get("name").toString());
        binding.title.setText(map.get("title").toString());
        contents = (ArrayList<String>) map.get("data");
        Parcelable onSaveInstanceState = binding.listv.onSaveInstanceState();
        binding.listv.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1, contents));
        ((BaseAdapter) binding.listv.getAdapter()).notifyDataSetChanged();
        binding.listv.onRestoreInstanceState(onSaveInstanceState);
    }

    private void _fabVisibility(boolean visible) {
        if (visible) {
            AutoTransition autoTransition = new AutoTransition();
            autoTransition.setDuration(200L);
            TransitionManager.beginDelayedTransition(binding.back, autoTransition);
            binding.bottom.setVisibility(View.VISIBLE);
        } else {
            AutoTransition autoTransition2 = new AutoTransition();
            autoTransition2.setDuration(200L);
            TransitionManager.beginDelayedTransition(binding.back, autoTransition2);
            binding.bottom.setVisibility(View.GONE);
        }
    }

    private void _save_item() {
        FileUtil.writeFile(BLOCK_SELECTOR_MENUS_FILE.getAbsolutePath(), new Gson().toJson(data));
    }

    private void _refresh_display() {
        display.clear();
        for (int i = 0; i < data.size(); i++) {
            display.add(data.get(i).get("name").toString());
        }
        binding.spinner.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_dropdown_item, display));
        ((BaseAdapter) binding.spinner.getAdapter()).notifyDataSetChanged();
    }
}
