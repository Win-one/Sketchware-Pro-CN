package mod.hey.studios.project.custom_blocks;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.besome.sketch.beans.BlockBean;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import a.a.a.Rs;
import a.a.a.Zx;
import a.a.a.aB;
import mod.hey.studios.editor.manage.block.ExtraBlockInfo;
import mod.hey.studios.editor.manage.block.v2.BlockLoader;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.activities.tools.ConfigActivity;
import pro.sketchware.R;
import pro.sketchware.databinding.DialogPaletteBinding;
import pro.sketchware.databinding.ItemCustomBlockBinding;
import pro.sketchware.databinding.ViewUsedCustomBlocksBinding;
import pro.sketchware.utility.FileUtil;
import pro.sketchware.utility.SketchwareUtil;

public class CustomBlocksDialog {

    private final HashMap<Integer, Boolean> selectedBlocks = new HashMap<>();
    private String sc_id;

    public void show(Activity context, String sc_id) {
        this.sc_id = sc_id;
        ViewUsedCustomBlocksBinding dialogBinding = ViewUsedCustomBlocksBinding.inflate(context.getLayoutInflater());

        aB dialog = new aB(context);
        dialog.b(Helper.getResString(R.string.used_custom_blocks));

        String subtitle = Helper.getResString(R.string.you_haven_t_used_any_custom_blocks_in_this_project);

        CustomBlocksManager customBlocksManager = new CustomBlocksManager(sc_id);

        ArrayList<BlockBean> customBlocks = customBlocksManager.getUsedBlocks();

        if (!customBlocks.isEmpty()) {
            subtitle = Helper.getResString(R.string.you_have_used) + customBlocks.size() + Helper.getResString(R.string.custom_block_s_in_this_project);

            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));

            BlocksAdapter adapter = new BlocksAdapter(customBlocks, selectedBlocks::put);

            dialogBinding.recyclerView.setAdapter(adapter);
        }

        if (customBlocks.isEmpty()) {
            dialog.a(subtitle);
        } else {
            dialog.b(Helper.getResString(R.string.common_word_import), v -> {
                ArrayList<BlockBean> selectedBeans = new ArrayList<>();
                for (int i = 0; i < customBlocks.size(); i++) {
                    if (Boolean.TRUE.equals(selectedBlocks.getOrDefault(i, false))) {
                        selectedBeans.add(customBlocks.get(i));
                    }
                }

                if (selectedBeans.isEmpty()) {
                    SketchwareUtil.toastError(Helper.getResString(R.string.please_select_at_least_one_block_to_import));
                    return;
                }

                importAll(context, customBlocksManager, selectedBeans);
                dialog.dismiss();
            });
            dialog.a(dialogBinding.getRoot());
        }

        dialog.a(Helper.getResString(R.string.common_word_dismiss), Helper.getDialogDismissListener(dialog));
        dialog.show();
    }

    private void importAll(Context context, CustomBlocksManager customBlocksManager, ArrayList<BlockBean> list) {
        ArrayList<HashMap<String, Object>> blocksList = new ArrayList<>();
        String paletteDir = getConfigPath(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_PALETTE_FILE_PATH);
        String blocksDir = getConfigPath(ConfigActivity.SETTING_BLOCKMANAGER_DIRECTORY_BLOCK_FILE_PATH);

        ArrayList<HashMap<String, Object>> allBlocksList = loadJsonList(blocksDir);
        ArrayList<HashMap<String, Object>> paletteList = loadJsonList(paletteDir);

        if (paletteList.isEmpty()) {
            showCreatePaletteDialog(context, paletteList, paletteDir, customBlocksManager, list, blocksList, allBlocksList, blocksDir);
            return;
        }

        ArrayList<String> paletteNames = new ArrayList<>();
        for (HashMap<String, Object> map : paletteList) {
            if (map.get("name") instanceof String) {
                paletteNames.add((String) map.get("name"));
            }
        }

        AtomicInteger selectedPalette = new AtomicInteger(paletteList.size() - 1);

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.import_custom_blocks_to)
                .setSingleChoiceItems(paletteNames.toArray(new String[0]), selectedPalette.get(), (dialog, which) -> selectedPalette.set(which))
                .setNegativeButton(R.string.create_new_palette, (dialog, which) -> {
                    showCreatePaletteDialog(context, paletteList, paletteDir, customBlocksManager, list, blocksList, allBlocksList, blocksDir);
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.common_word_import, (dialog, which) -> {
                    addBlocksToList(customBlocksManager, list, blocksList, selectedPalette.get() + 9);
                    allBlocksList.addAll(blocksList);
                    FileUtil.writeFile(blocksDir, new Gson().toJson(allBlocksList));
                    BlockLoader.refresh();
                    SketchwareUtil.toast(Helper.getResString(R.string.blocks_imported));
                })
                .show();
    }

    private String getConfigPath(String settingKey) {
        return FileUtil.getExternalStorageDir() + ConfigActivity.getStringSettingValueOrSetAndGet(
                settingKey, (String) ConfigActivity.getDefaultValue(settingKey));
    }

    private ArrayList<HashMap<String, Object>> loadJsonList(String path) {
        ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        if (FileUtil.isExistFile(path)) {
            try {
                String content = FileUtil.readFile(path);
                if (!content.isEmpty()) {
                    result = new Gson().fromJson(content, Helper.TYPE_MAP_LIST);
                }
            } catch (JsonParseException | NullPointerException ignored) {}
        }
        return result;
    }

    private void showCreatePaletteDialog(Context context, ArrayList<HashMap<String, Object>> paletteList, String paletteDir,
                                                CustomBlocksManager customBlocksManager, ArrayList<BlockBean> list, ArrayList<HashMap<String, Object>> blocksList,
                                                ArrayList<HashMap<String, Object>> allBlocksList, String blocksDir) {

        aB dialog = new aB((Activity) context);
        dialog.a(R.drawable.icon_style_white_96);
        dialog.b(Helper.getResString(R.string.create_a_new_palette));

        DialogPaletteBinding binding = DialogPaletteBinding.inflate(((Activity) context).getLayoutInflater());

        binding.openColorPalette.setOnClickListener(v -> {
            Zx colorPicker = new Zx((Activity) context, 0, true, false);
            colorPicker.a(new Zx.b() {
                @Override
                public void a(int colorInt) {
                    binding.colorEditText.setText(getHexColor(colorInt));
                }

                @Override
                public void a(String var1, int var2) {
                }
            });
            colorPicker.showAtLocation(binding.openColorPalette, Gravity.CENTER, 0, 0);
        });

        dialog.a(binding.getRoot());
        dialog.b(Helper.getResString(R.string.common_word_save), v -> {
            String name = Helper.getText(binding.nameEditText);
            String color = Helper.getText(binding.colorEditText);

            if (!validateInput(binding, name, color)) return;

            HashMap<String, Object> newPalette = new HashMap<>();
            newPalette.put("name", name);
            newPalette.put("color", color);
            paletteList.add(newPalette);
            FileUtil.writeFile(paletteDir, new Gson().toJson(paletteList));

            addBlocksToList(customBlocksManager, list, blocksList, paletteList.size() + 8);
            allBlocksList.addAll(blocksList);
            FileUtil.writeFile(blocksDir, new Gson().toJson(allBlocksList));
            BlockLoader.refresh();
            SketchwareUtil.toast(Helper.getResString(R.string.blocks_imported));
            dialog.dismiss();
        });
        dialog.a(Helper.getResString(R.string.common_word_cancel), Helper.getDialogDismissListener(dialog));
        dialog.show();
    }

    private boolean validateInput(DialogPaletteBinding binding, String name, String color) {
        if (name.isEmpty()) {
            binding.name.setError(Helper.getResString(R.string.name_can_t_be_empty));
            binding.name.requestFocus();
            return false;
        }
        if (color.isEmpty()) {
            binding.color.setError(Helper.getResString(R.string.color_cannot_be_empty));
            binding.color.requestFocus();
            return false;
        }
        try {
            Color.parseColor(color);
        } catch (IllegalArgumentException e) {
            binding.color.setError(Helper.getResString(R.string.invalid_hexadecimal_color));
            binding.color.requestFocus();
            return false;
        }
        return true;
    }

    private void addBlocksToList(CustomBlocksManager customBlocksManager, ArrayList<BlockBean> list,
                                        ArrayList<HashMap<String, Object>> blocksList, int paletteIndex) {

        for (BlockBean block : list) {
            try {
                HashMap<String, Object> blockData = new HashMap<>();
                blockData.put("name", block.opCode);
                blockData.put("type", block.type);
                blockData.put("typeName", block.typeName);
                blockData.put("spec", block.spec);
                blockData.put("color", getHexColor(block.color));
                blockData.put("spec2", customBlocksManager.getCustomBlockSpec2(block.opCode));
                blockData.put("code", customBlocksManager.getCustomBlockCode(block.opCode));
                blockData.put("palette", String.valueOf(paletteIndex));
                blocksList.add(blockData);
            } catch (Exception ignored) {}
        }
    }

    private String getHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public class BlocksAdapter extends RecyclerView.Adapter<BlocksAdapter.ViewHolder> {

        private final ArrayList<BlockBean> blockBeans;
        private final BiConsumer<Integer, Boolean> onCheckedChangeListener;

        public BlocksAdapter(ArrayList<BlockBean> blockBeans, BiConsumer<Integer, Boolean> onCheckedChangeListener) {
            this.blockBeans = blockBeans;
            this.onCheckedChangeListener = onCheckedChangeListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemCustomBlockBinding binding = ItemCustomBlockBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false
            );
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(blockBeans.get(position), position);
        }

        @Override
        public int getItemCount() {
            return blockBeans.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final ItemCustomBlockBinding binding;

            public ViewHolder(@NonNull ItemCustomBlockBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void bind(@NonNull BlockBean block, int position) {
                String blockInfo = getBlockInfo(block);
                binding.tvBlockId.setText(blockInfo);
                addCustomBlockView(binding.customBlocksContainer, itemView.getContext(), block);

                setupCheckBox(block, position);
                setupClickListener(block, blockInfo);
            }

            private void addCustomBlockView(ViewGroup container, Context context, BlockBean block) {
                container.removeAllViews();
                container.addView(createBlock(context, block));
            }

            private void setupCheckBox(@NonNull BlockBean block, int position) {
                boolean canImport = isBlockImportable(block);
                binding.checkBox.setEnabled(canImport);

                if (canImport) {
                    binding.checkBox.setChecked(Boolean.TRUE.equals(selectedBlocks.getOrDefault(position, false)));
                    binding.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        onCheckedChangeListener.accept(position, isChecked);
                    });
                } else {
                    binding.checkBox.setOnCheckedChangeListener(null);
                    binding.checkBox.setChecked(false);
                }
            }

            private void setupClickListener(@NonNull BlockBean block, String blockInfo) {
                boolean canImport = isBlockImportable(block);

                binding.transparentOverlay.setOnClickListener(view -> {
                    if (canImport) {
                        binding.checkBox.setChecked(!binding.checkBox.isChecked());
                    } else if (blockInfo.equals("Missing")) {
                        SketchwareUtil.toastError(Helper.getResString(R.string.this_block_is_missing));
                    } else {
                        SketchwareUtil.toastError(Helper.getResString(R.string.this_block_already_exists_in_your_collection));
                    }
                });
            }

            private boolean isBlockImportable(@NonNull BlockBean block) {
                ExtraBlockInfo blockInfo = BlockLoader.getBlockInfo(block.opCode);
                return blockInfo.isMissing && !BlockLoader.getBlockFromProject(sc_id, block.opCode).isMissing;
            }

            private String getBlockInfo(@NonNull BlockBean block) {
                ExtraBlockInfo blockInfo = BlockLoader.getBlockInfo(block.opCode);
                if (BlockLoader.getBlockFromProject(sc_id, block.opCode).isMissing && blockInfo.isMissing) {
                    return "Missing";
                }
                return block.opCode;
            }
        }

        private Rs createBlock(Context context, BlockBean blockBean) {
            Rs block = new Rs(
                    context,
                    Integer.parseInt(blockBean.id),
                    blockBean.spec,
                    blockBean.type,
                    blockBean.typeName,
                    blockBean.opCode
            );
            block.e = blockBean.color;
            return block;
        }
    }

}