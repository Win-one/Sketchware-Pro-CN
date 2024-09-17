package mod.hilal.saif.activities.tools;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.developer.filepicker.model.DialogConfigs;
import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.BlocksManagersDetailsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;

public class BlocksManagerDetailsActivity extends BaseAppCompatActivity {

    private static final String BLOCK_EXPORT_PATH = new File(FileUtil.getExternalStorageDir(), ".sketchware/resources/block/export/").getAbsolutePath();

    private final ArrayList<HashMap<String, Object>> filtered_list = new ArrayList<>();
    private final ArrayList<Integer> reference_list = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> all_blocks_list = new ArrayList<>();
    private String blocks_path = "";
    private String mode = "normal";
    private ArrayList<HashMap<String, Object>> pallet_list = new ArrayList<>();
    private String pallet_path = "";
    private int palette = 0;
    private Parcelable listViewSavedState;

    private Toolbar toolbar;
    private BlocksManagersDetailsBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BlocksManagersDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initialize();
        _receive_intents();
    }

    private void initialize() {

        toolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar_improved, binding.background, false);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        binding.background.addView(toolbar, 0);

        binding.fabButton.setOnClickListener(v -> {
            Object paletteColor = pallet_list.get(palette - 9).get("color");
            if (paletteColor instanceof String) {
                Intent intent = new Intent(getApplicationContext(), BlocksManagerCreatorActivity.class);
                intent.putExtra("mode", "add");
                intent.putExtra("color", (String) paletteColor);
                intent.putExtra("path", blocks_path);
                intent.putExtra("pallet", String.valueOf(palette));
                startActivity(intent);
            } else {
                SketchwareUtil.toastError("Invalid color of palette #" + (palette - 9));
            }
        });
    }

    public void openFileExplorerImport() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        File externalStorageDir = Environment.getExternalStorageDirectory();
        properties.root = externalStorageDir;
        properties.error_dir = externalStorageDir;
        properties.offset = externalStorageDir;
        properties.extensions = new String[]{"json"};
        FilePickerDialog filePickerDialog = new FilePickerDialog(this, properties);
        filePickerDialog.setTitle(R.string.select_a_json_file);
        filePickerDialog.setDialogSelectionListener(selections -> {
            if (FileUtil.readFile(selections[0]).equals("")) {
                SketchwareUtil.toastError(getString(R.string.the_selected_file_is_empty));
            } else if (FileUtil.readFile(selections[0]).equals("[]")) {
                SketchwareUtil.toastError(getString(R.string.the_selected_file_is_empty));
            } else {
                try {
                    ArrayList<HashMap<String, Object>> readMap = new Gson().fromJson(FileUtil.readFile(selections[0]), Helper.TYPE_MAP_LIST);
                    _importBlocks(readMap);
                } catch (JsonParseException e) {
                    SketchwareUtil.toastError(getString(R.string.invalid_json_file));
                }
            }
        });
        filePickerDialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        listViewSavedState = binding.blockList.onSaveInstanceState();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (listViewSavedState != null) {
            binding.blockList.onRestoreInstanceState(listViewSavedState);
            _refreshLists();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mode.equals("editor")) {
            mode = "normal";
            Parcelable savedState = binding.blockList.onSaveInstanceState();
            binding.blockList.setAdapter(new Adapter(filtered_list));
            ((BaseAdapter) binding.blockList.getAdapter()).notifyDataSetChanged();
            binding.blockList.onRestoreInstanceState(savedState);
            fabButtonVisibility(true);
            onCreateOptionsMenu(toolbar.getMenu());
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if (Integer.parseInt(getIntent().getStringExtra("position")) != -1) {
            if (mode.equals("normal")) {
                menu.add(Menu.NONE, 1, Menu.NONE, R.string.common_word_swap).setIcon(getDrawable(R.drawable.swap_vert_24px)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.add(Menu.NONE, 2, Menu.NONE, R.string.common_word_import);
                menu.add(Menu.NONE, 3, Menu.NONE, R.string.common_word_export);
            } else {
                menu.add(Menu.NONE, 1, Menu.NONE, "Swap").setIcon(getDrawable(R.drawable.save_icon_24px)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 1:
                if (mode.equals("normal")) {
                    mode = "editor";
                    fabButtonVisibility(false);
                } else {
                    mode = "normal";
                    fabButtonVisibility(true);
                }
                Parcelable savedInstanceState = binding.blockList.onSaveInstanceState();
                binding.blockList.setAdapter(new Adapter(filtered_list));
                ((BaseAdapter) binding.blockList.getAdapter()).notifyDataSetChanged();
                binding.blockList.onRestoreInstanceState(savedInstanceState);
                onCreateOptionsMenu(toolbar.getMenu());
                break;

            case 2:
                openFileExplorerImport();
                break;

            case 3:
                Object paletteName = pallet_list.get(palette - 9).get("name");
                if (paletteName instanceof String) {
                    String exportTo = new File(BLOCK_EXPORT_PATH, paletteName + ".json").getAbsolutePath();
                    FileUtil.writeFile(exportTo, new Gson().toJson(filtered_list));
                    SketchwareUtil.toast("Successfully exported blocks to:\n" + exportTo, Toast.LENGTH_LONG);
                } else {
                    SketchwareUtil.toastError("Invalid name of palette #" + (palette - 9));
                }
                break;

            default:
                return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void _receive_intents() {
        palette = Integer.parseInt(getIntent().getStringExtra("position"));
        pallet_path = getIntent().getStringExtra("dirP");
        blocks_path = getIntent().getStringExtra("dirB");
        _refreshLists();
        if (palette == -1) {
            getSupportActionBar().setTitle(R.string.common_word_recycle_bin);
            binding.fabButton.setVisibility(View.GONE);
        } else {
            Object paletteName = pallet_list.get(palette - 9).get("name");

            if (paletteName instanceof String) {
                getSupportActionBar().setTitle(R.string.manage_block);
                getSupportActionBar().setSubtitle((String) paletteName);
            }
        }
    }

    private void _refreshLists() {
        filtered_list.clear();
        reference_list.clear();
        String paletteFileContent = FileUtil.readFile(pallet_path);
        String blocksFileContent = FileUtil.readFile(blocks_path);
        if (paletteFileContent.equals("")) {
            FileUtil.writeFile(pallet_path, "[]");
            paletteFileContent = "[]";
        }
        if (blocksFileContent.equals("")) {
            FileUtil.writeFile(blocks_path, "[]");
            blocksFileContent = "[]";
        }

        parseLists:
        {
            try {
                pallet_list = new Gson().fromJson(paletteFileContent, Helper.TYPE_MAP_LIST);

                if (pallet_list != null) {
                    break parseLists;
                }
                // fall-through to shared error handling
            } catch (JsonParseException e) {
                // fall-through to shared error handling
            }

            SketchwareUtil.showFailedToParseJsonDialog(this, new File(pallet_path), "Custom Block Palettes", v -> _refreshLists());
            pallet_list = new ArrayList<>();
        }

        parseBlocks:
        {
            try {
                all_blocks_list = new Gson().fromJson(blocksFileContent, Helper.TYPE_MAP_LIST);

                if (all_blocks_list != null) {
                    break parseBlocks;
                }
                // fall-through to shared error handling
            } catch (JsonParseException e) {
                // fall-through to shared error handling
            }

            SketchwareUtil.showFailedToParseJsonDialog(this, new File(blocks_path), "Custom Blocks", v -> _refreshLists());
            all_blocks_list = new ArrayList<>();
        }

        for (int i = 0; i < all_blocks_list.size(); i++) {
            HashMap<String, Object> block = all_blocks_list.get(i);

            Object blockPalette = block.get("palette");
            if (blockPalette instanceof String) {
                try {
                    if (Integer.parseInt((String) blockPalette) == palette) {
                        reference_list.add(i);
                        filtered_list.add(block);
                    }
                } catch (NumberFormatException e) {
                    SketchwareUtil.toastError("Invalid palette entry in block #" + (i + 1));
                }
            }
        }
        Parcelable onSaveInstanceState = binding.blockList.onSaveInstanceState();
        binding.blockList.setAdapter(new Adapter(filtered_list));
        ((BaseAdapter) binding.blockList.getAdapter()).notifyDataSetChanged();
        binding.blockList.onRestoreInstanceState(onSaveInstanceState);
    }

    private void _a(View view) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setColor(Color.parseColor("#ffffff"));
        RippleDrawable rippleDrawable = new RippleDrawable(new ColorStateList(new int[][]{new int[0]}, new int[]{Color.parseColor("#20008DCD")}), gradientDrawable, null);
        view.setBackground(rippleDrawable);
        view.setClickable(true);
        view.setFocusable(true);
    }

    private void _swapitems(int sourcePosition, int targetPosition) {
        Collections.swap(all_blocks_list, sourcePosition, targetPosition);
        FileUtil.writeFile(blocks_path, new Gson().toJson(all_blocks_list));
        _refreshLists();
    }

    private void _showItemPopup(View view, final int position) {
        if (palette == -1) {
            PopupMenu popupMenu = new PopupMenu(this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, 1, Menu.NONE, R.string.delete_permanently);
            menu.add(Menu.NONE, 2, Menu.NONE, R.string.common_word_restore);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 2:
                        _deleteBlock(position);
                        break;

                    case 1:
                        _changePallette(position);
                        break;

                    default:
                        return false;
                }
                return true;
            });
            popupMenu.show();
            return;
        }
        PopupMenu popupMenu = new PopupMenu(this, view);
        Menu menu = popupMenu.getMenu();
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.insert_above);
        menu.add(Menu.NONE, 2, Menu.NONE, R.string.common_word_delete);
        menu.add(Menu.NONE, 3, Menu.NONE, R.string.duplicate);
        menu.add(Menu.NONE, 4, Menu.NONE, R.string.move_to_palette);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 3:
                    _duplicateBlock(position);
                    break;

                case 1:
                    Object paletteColor = pallet_list.get(palette - 9).get("color");
                    if (paletteColor instanceof String) {
                        Intent intent = new Intent(getApplicationContext(), BlocksManagerCreatorActivity.class);
                        intent.putExtra("mode", "insert");
                        intent.putExtra("path", blocks_path);
                        intent.putExtra("color", (String) paletteColor);
                        intent.putExtra("pos", String.valueOf(position));
                        startActivity(intent);
                    } else {
                        SketchwareUtil.toastError("Invalid color of palette #" + (palette - 9));
                    }
                    break;

                case 4:
                    _changePallette(position);
                    break;

                case 2:
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.delete_block)
                            .setMessage(R.string.are_you_sure_you_want_to_delete_this_block)
                            .setPositiveButton(R.string.common_word_recycle_bin, (dialog, which) -> _moveToRecycleBin(position))
                            .setNegativeButton(R.string.common_word_cancel, null)
                            .setNeutralButton(R.string.delete_permanently, (dialog, which) -> _deleteBlock(position))
                            .show();
                    break;

                default:
                    return false;
            }
            return true;
        });
        popupMenu.show();
    }

    private void _duplicateBlock(int position) {
        HashMap<String, Object> block = new HashMap<>(all_blocks_list.get(position));
        Object blockName = block.get("name");

        if (blockName instanceof String) {
            if (((String) blockName).matches("(?s).*_copy[0-9][0-9]")) {
                block.put("name", ((String) blockName).replaceAll("_copy[0-9][0-9]", "_copy" + SketchwareUtil.getRandom(11, 99)));
            } else {
                block.put("name", blockName + "_copy" + SketchwareUtil.getRandom(11, 99));
            }
        }
        all_blocks_list.add(position + 1, block);
        FileUtil.writeFile(blocks_path, new Gson().toJson(all_blocks_list));
        _refreshLists();
    }

    private void _deleteBlock(int position) {
        all_blocks_list.remove(position);
        FileUtil.writeFile(blocks_path, new Gson().toJson(all_blocks_list));
        _refreshLists();
    }

    private void _moveToRecycleBin(int position) {
        all_blocks_list.get(position).put("palette", "-1");
        FileUtil.writeFile(blocks_path, new Gson().toJson(all_blocks_list));
        _refreshLists();
    }

    private void _changePallette(final int position) {
        ArrayList<String> paletteNames = new ArrayList<>();
        for (int j = 0, pallet_listSize = pallet_list.size(); j < pallet_listSize; j++) {
            HashMap<String, Object> palette = pallet_list.get(j);
            Object name = palette.get("name");

            if (name instanceof String) {
                paletteNames.add((String) name);
            } else {
                SketchwareUtil.toastError("Invalid name of Custom Block palette #" + (j + 1));
            }
        }

        Gson gson = new Gson();
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setNegativeButton(R.string.common_word_cancel, null);
        if (palette == -1) {
            AtomicInteger restoreToChoice = new AtomicInteger(-1);
            builder.setTitle(R.string.restore_to)
                    .setSingleChoiceItems(paletteNames.toArray(new String[0]), -1, (dialog, which) -> restoreToChoice.set(which))
                    .setPositiveButton(R.string.common_word_restore, (dialog, which) -> {
                        if (restoreToChoice.get() != -1) {
                            all_blocks_list.get(position).put("palette", String.valueOf(restoreToChoice.get() + 9));
                            Collections.swap(all_blocks_list, position, all_blocks_list.size() - 1);
                            FileUtil.writeFile(blocks_path, gson.toJson(all_blocks_list));
                            _refreshLists();
                        }
                    });
        } else {
            AtomicInteger moveToChoice = new AtomicInteger(palette - 9);
            builder.setTitle(R.string.move_to)
                    .setSingleChoiceItems(paletteNames.toArray(new String[0]), palette - 9, (dialog, which) -> moveToChoice.set(which))
                    .setPositiveButton(R.string.common_word_move, (dialog, which) -> {
                        all_blocks_list.get(position).put("palette", String.valueOf(moveToChoice.get() + 9));
                        Collections.swap(all_blocks_list, position, all_blocks_list.size() - 1);
                        FileUtil.writeFile(blocks_path, gson.toJson(all_blocks_list));
                        _refreshLists();
                    });
        }
        builder.show();
    }

    private void _importBlocks(final ArrayList<HashMap<String, Object>> blocks) {
        try {
            ArrayList<String> names = new ArrayList<>();
            final ArrayList<Integer> toAdd = new ArrayList<>();
            for (int i = 0; i < blocks.size(); i++) {
                Object blockName = blocks.get(i).get("name");

                if (blockName instanceof String) {
                    names.add((String) blockName);
                } else {
                    SketchwareUtil.toastError("Invalid name entry of Custom Block #" + (i + 1) + " in Blocks to import");
                }
            }
            AlertDialog.Builder import_dialog = new AlertDialog.Builder(this);
            import_dialog.setTitle(R.string.import_blocks)
                    .setMultiChoiceItems(names.toArray(new CharSequence[0]), null, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            toAdd.add(which);
                        } else {
                            toAdd.remove((Integer) which);
                        }
                    })
                    .setPositiveButton(R.string.common_word_import, (dialog, which) -> {
                        for (int i = 0; i < blocks.size(); i++) {
                            if (toAdd.contains(i)) {
                                HashMap<String, Object> map = blocks.get(i);
                                map.put("palette", String.valueOf(palette));
                                all_blocks_list.add(map);
                            }
                        }
                        FileUtil.writeFile(blocks_path, new Gson().toJson(all_blocks_list));
                        _refreshLists();
                        SketchwareUtil.toast("Imported successfully");
                    })
                    .setNegativeButton(R.string.common_word_reverse, (dialog, which) -> {
                        for (int i = 0; i < blocks.size(); i++) {
                            if (!toAdd.contains(i)) {
                                HashMap<String, Object> map = blocks.get(i);
                                map.put("palette", String.valueOf(palette));
                                all_blocks_list.add(map);
                            }
                        }
                        FileUtil.writeFile(blocks_path, new Gson().toJson(all_blocks_list));
                        _refreshLists();
                        SketchwareUtil.toast("Imported successfully");
                    })
                    .setNeutralButton(R.string.common_word_all, (dialog, which) -> {
                        for (int i = 0; i < blocks.size(); i++) {
                            HashMap<String, Object> map = blocks.get(i);
                            map.put("palette", String.valueOf(palette));
                            all_blocks_list.add(map);
                        }
                        FileUtil.writeFile(blocks_path, new Gson().toJson(all_blocks_list));
                        _refreshLists();
                        SketchwareUtil.toast("Imported successfully");
                    })
                    .show();
        } catch (Exception e) {
            SketchwareUtil.toastError("An error occurred! [" + e.getMessage() + "]");
        }
    }

    private void fabButtonVisibility(boolean visible) {
        if (visible) {
            ObjectAnimator.ofFloat(binding.fabButton, "translationX", binding.fabButton.getTranslationX(), -50.0f, 0.0f).setDuration(400L).start();
        } else {
            ObjectAnimator.ofFloat(binding.fabButton, "translationX", binding.fabButton.getTranslationX(), -50.0f, 250.0f).setDuration(400L).start();
        }
    }

    private class Adapter extends BaseAdapter {

        private final ArrayList<HashMap<String, Object>> blocks;

        public Adapter(ArrayList<HashMap<String, Object>> data) {
            blocks = data;
        }

        @Override
        public int getCount() {
            return blocks.size();
        }

        @Override
        public HashMap<String, Object> getItem(int position) {
            return blocks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.block_customview, parent, false);
            }

            final HashMap<String, Object> block = blocks.get(position);

            final LinearLayout background = convertView.findViewById(R.id.background);
            final TextView name = convertView.findViewById(R.id.name);
            final TextView spec = convertView.findViewById(R.id.spec);
            final MaterialCardView upLayout = convertView.findViewById(R.id.up_layout);
            final MaterialCardView downLayout = convertView.findViewById(R.id.down_layout);
            final LinearLayout down = convertView.findViewById(R.id.down);
            final LinearLayout up = convertView.findViewById(R.id.up);

            if (mode.equals("normal")) {
                downLayout.setVisibility(View.GONE);
                upLayout.setVisibility(View.GONE);
            } else {
                downLayout.setVisibility(position != (blocks.size() - 1) ? View.VISIBLE : View.GONE);
                upLayout.setVisibility(position != 0 ? View.VISIBLE : View.GONE);
            }
            _a(up);
            _a(down);
            _a(background);

            Object blockName = block.get("name");
            if (blockName instanceof String) {
                name.setText((String) blockName);
                spec.setHint("");
            } else {
                name.setText("");
                name.setHint(R.string.invalid_block_name_entry);
            }

            Object blockSpec = block.get("spec");
            if (blockSpec instanceof String) {
                spec.setText((String) blockSpec);
                spec.setHint("");
            } else {
                spec.setText("");
                spec.setHint(R.string.invalid_block_spec_entry);
            }

            Object blockType = block.get("type");
            if (blockType instanceof String) {
                switch ((String) blockType) {
                    case " ":
                    case "regular":
                        spec.setBackgroundResource(R.drawable.block_ori);
                        break;

                    case "b":
                        spec.setBackgroundResource(R.drawable.block_boolean);
                        break;

                    case "c":
                    case "e":
                        spec.setBackgroundResource(R.drawable.if_else);
                        break;

                    case "d":
                        spec.setBackgroundResource(R.drawable.block_num);
                        break;

                    case "f":
                        spec.setBackgroundResource(R.drawable.block_stop);
                        break;

                    default:
                        spec.setBackgroundResource(R.drawable.block_string);
                        break;
                }
            } else {
                spec.setBackgroundResource(R.drawable.block_string);
            }

            if (palette == -1) {
                spec.getBackground().setColorFilter(new PorterDuffColorFilter(0xff9e9e9e, PorterDuff.Mode.MULTIPLY));
            } else {
                if (block.containsKey("color")) {
                    Object blockColor = block.get("color");

                    if (blockColor instanceof String) {
                        int color = -1;
                        try {
                            color = Color.parseColor((String) blockColor);
                        } catch (IllegalArgumentException e) {
                            SketchwareUtil.toastError("Invalid color entry in block #" + (position + 1));
                        }

                        if (color != -1) {
                            spec.getBackground().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
                        }
                    } else {
                        SketchwareUtil.toastError("Invalid color entry in block #" + (position + 1));
                    }
                } else {
                    HashMap<String, Object> paletteObject = pallet_list.get(palette - 9);
                    Object paletteColor = paletteObject.get("color");

                    if (paletteColor instanceof String) {
                        try {
                            spec.getBackground().setColorFilter(new PorterDuffColorFilter(
                                    Color.parseColor((String) paletteColor),
                                    PorterDuff.Mode.MULTIPLY
                            ));
                        } catch (IllegalArgumentException e) {
                            SketchwareUtil.toastError("Invalid color in Custom Block palette #" + (palette - 8));
                        }
                    }
                }
            }
            up.setOnClickListener(v -> {
                if (position > 0) {
                    _swapitems(reference_list.get(position), reference_list.get(position - 1));
                }
            });
            down.setOnClickListener(v -> {
                if (position < filtered_list.size() - 1) {
                    _swapitems(reference_list.get(position), reference_list.get(position + 1));
                }
            });
            if (mode.equals("normal")) {
                background.setOnClickListener(v -> {
                    if (palette == -1) {
                        _showItemPopup(background, reference_list.get(position));
                    } else {
                        Object paletteColor = pallet_list.get(palette - 9).get("color");

                        if (paletteColor instanceof String) {
                            Intent intent = new Intent(getApplicationContext(), BlocksManagerCreatorActivity.class);
                            intent.putExtra("mode", "edit");
                            intent.putExtra("color", (String) paletteColor);
                            intent.putExtra("path", blocks_path);
                            intent.putExtra("pos", String.valueOf(reference_list.get(position)));
                            startActivity(intent);
                        }
                    }
                });
                background.setOnLongClickListener(v -> {
                    _showItemPopup(background, reference_list.get(position));
                    return true;
                });
            }
            return convertView;
        }
    }
}
