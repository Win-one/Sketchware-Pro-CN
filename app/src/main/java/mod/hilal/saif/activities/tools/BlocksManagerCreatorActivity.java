package mod.hilal.saif.activities.tools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.BlocksManagerBinding;
import com.sketchware.remod.databinding.BlocksManagerCreatorBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import a.a.a.Zx;
import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hasrat.highlighter.SimpleHighlighter;
import mod.hasrat.lib.BaseTextWatcher;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.lib.PCP;

public class BlocksManagerCreatorActivity extends AppCompatActivity {

    private final ArrayList<String> id_detector = new ArrayList<>();
    private ArrayList<HashMap<String, Object>> blocksList = new ArrayList<>();
    /**
     * Current mode of this activity, "edit" if editing a block, "add" if creating a new block and "insert" if inserting a block above another
     */
    private String mode = "";
    /**
     * Position of current editing/adding/inserting block in palette
     */
    private int blockPosition = 0;
    private String palletColour = "";
    private String path = "";
    private BlocksManagerCreatorBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = BlocksManagerCreatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initialize();
        initializeLogic();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialize() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        binding.toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));

        binding.name.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String string = s.toString();
                if (!id_detector.contains(string)) {
                    binding.nameLay.setErrorEnabled(false);
                    binding.save.setEnabled(true);
                } else if (!mode.equals("edit")) {
                    binding.nameLay.setErrorEnabled(true);
                    binding.nameLay.setError("Block name already in use");
                    binding.save.setEnabled(false);
                } else {
                    HashMap<String, Object> savedBlocksListBlock = blocksList.get(blockPosition);
                    Object blockNameObject = savedBlocksListBlock.get("name");

                    if (!string.equals(blockNameObject)) {
                        binding.nameLay.setErrorEnabled(true);
                        binding.nameLay.setError("Block name already in use");
                        binding.save.setEnabled(false);
                    }
                }
            }
        });

        binding.selectType.setOnClickListener(v -> {
            List<String> types = Arrays.asList(
                    "regular",
                    "c",
                    "e",
                    "s",
                    "b",
                    "d",
                    "v",
                    "a",
                    "f",
                    "l",
                    "p",
                    "h"
            );
            List<String> choices = Arrays.asList(
                    "Regular block (regular)",
                    "if block (c)",
                    "if-else block (e)",
                    "String (s)",
                    "Boolean (b)",
                    "Number (d)",
                    "Variable (v)",
                    "Map (a)",
                    "stop block (f)",
                    "List (l)",
                    "Component (p)",
                    "Header (h)"
            );
            AtomicInteger choice = new AtomicInteger();
            new AlertDialog.Builder(this).setTitle(R.string.block_type)
                    .setSingleChoiceItems(choices.toArray(new String[0]),
                            types.indexOf(binding.type.getText().toString()), (dialog, which) -> choice.set(which))
                    .setPositiveButton(R.string.common_word_save, (dialog, which) -> binding.type.setText(types.get(choice.get())))
                    .setNegativeButton(R.string.common_word_cancel, null)
                    .create().show();
        });

        binding.type.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("e")) {
                    AutoTransition transition = new AutoTransition();
                    transition.setDuration(300L);
                    TransitionManager.beginDelayedTransition(binding.scrollView, transition);
                    binding.spec2lay.setVisibility(View.VISIBLE);
                } else {
                    AutoTransition transition = new AutoTransition();
                    transition.setDuration(300L);
                    TransitionManager.beginDelayedTransition(binding.scrollView, transition);
                    binding.spec2lay.setVisibility(View.GONE);
                }
                updateBlockSpec(s.toString(), binding.color.getText().toString());
            }
        });

        binding.spec.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Matcher matcher = Pattern.compile("%[smdb]\\.?[a-zA-Z]*").matcher(s.toString());
                while (matcher.find()) {
                    try {
                        binding.spec.getEditableText().setSpan(new ForegroundColorSpan(Color.WHITE),
                                matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception ignored) {
                    }
                    try {
                        binding.spec.getEditableText().setSpan(new BackgroundColorSpan(0x18000000),
                                matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception ignored) {
                    }
                    try {
                        binding.spec.getEditableText().setSpan(new RelativeSizeSpan(-5),
                                matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception ignored) {
                    }
                    try {
                        binding.spec.getEditableText().setSpan(new StyleSpan(1), matcher.start(),
                                matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception ignored) {
                    }
                }
            }
        });

        binding.colourSelector.setOnClickListener(v -> {
            View inflate = getLayoutInflater().inflate(R.layout.color_picker, null);
            Zx zx = new Zx(inflate, this, 0, true, false);
            zx.a(new PCP(binding.color));
            zx.setAnimationStyle(R.anim.abc_fade_in);
            zx.showAtLocation(inflate, Gravity.CENTER, 0, 0);
        });

        binding.color.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateBlockSpec(binding.type.getText().toString(), s.toString());
            }
        });

        binding.cancel.setOnClickListener(Helper.getBackPressedClickListener(this));
        binding.save.setOnClickListener(v -> {
            if (binding.type.getText().toString().equals("")) {
                binding.type.setText(" ");
            }
            if (mode.equals("add")) {
                addBlock();
            }
            if (mode.equals("insert")) {
                insertBlockAt(blockPosition);
            }
            if (mode.equals("edit")) {
                editBlock(blockPosition);
            }
        });

        binding.reset.setOnClickListener(v -> binding.color.setText(palletColour));
        binding.spec.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;

                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            v.onTouchEvent(event);
            return true;
        });
    }

    private void initializeLogic() {
        inputProperties();
        addParameters();
        receiveIntents();
        new SimpleHighlighter(binding.code);
    }

    private View addBlockMenu(final String menu, String name) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        textView.setPadding(
                (int) SketchwareUtil.getDip(8),
                0,
                (int) SketchwareUtil.getDip(8),
                0
        );
        textView.setTextColor(0xff006064);
        textView.setText(name);
        textView.setTextSize(14.0f);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder(binding.spec.getText().toString());
            int selectionStart = binding.spec.getSelectionStart();
            sb.insert(selectionStart, menu);
            binding.spec.setText(sb);
            binding.spec.setSelection(selectionStart + menu.length());
        });
        return textView;
    }

    private void inputProperties() {
        binding.name.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        binding.name.setSingleLine(true);
        binding.type.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        binding.type.setSingleLine(true);
        binding.typeName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        binding.typeName.setSingleLine(true);
        binding.spec.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        binding.spec.setSingleLine(true);
        binding.color.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        binding.color.setSingleLine(true);
        binding.spec2.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        binding.spec2.setSingleLine(true);
        binding.code.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        binding.code.setSingleLine(false);
        binding.scrollParameters.setVerticalScrollBarEnabled(false);
        binding.scrollParameters.setHorizontalScrollBarEnabled(false);
        binding.spec2lay.setVisibility(View.GONE);
    }

    private void addParameters() {
        binding.parameterHolder.addView(addBlockMenu("%s.inputOnly ", "inputOnly"));
        binding.parameterHolder.addView(addBlockMenu("%s ", "string"));
        binding.parameterHolder.addView(addBlockMenu("%b ", "boolean"));
        binding.parameterHolder.addView(addBlockMenu("%d ", "number"));
        binding.parameterHolder.addView(addBlockMenu("%m.varMap ", "map"));
        binding.parameterHolder.addView(addBlockMenu("%m.view ", "view"));
        binding.parameterHolder.addView(addBlockMenu("%m.textview ", "textView"));
        binding.parameterHolder.addView(addBlockMenu("%m.edittext ", "editText"));
        binding.parameterHolder.addView(addBlockMenu("%m.imageview ", "ImageView"));
        binding.parameterHolder.addView(addBlockMenu("%m.listview ", "listView"));
        binding.parameterHolder.addView(addBlockMenu("%m.list ", "list"));
        binding.parameterHolder.addView(addBlockMenu("%m.listMap ", "listMap"));
        binding.parameterHolder.addView(addBlockMenu("%m.listStr ", "listString"));
        binding.parameterHolder.addView(addBlockMenu("%m.listInt ", "listNumber"));
        binding.parameterHolder.addView(addBlockMenu("%m.intent ", "intent"));
        binding.parameterHolder.addView(addBlockMenu("%m.color ", "color"));
        binding.parameterHolder.addView(addBlockMenu("%m.activity ", "activity"));
        binding.parameterHolder.addView(addBlockMenu("%m.resource ", "resource"));
        binding.parameterHolder.addView(addBlockMenu("%m.customViews ", "custom views"));
        binding.parameterHolder.addView(addBlockMenu("%m.layout ", "layout"));
        binding.parameterHolder.addView(addBlockMenu("%m.anim ", "anim"));
        binding.parameterHolder.addView(addBlockMenu("%m.drawable ", "drawable"));
    }

    private void receiveIntents() {
        mode = getIntent().getStringExtra("mode");
        path = getIntent().getStringExtra("path");
        palletColour = getIntent().getStringExtra("color");
        getBlockList();
        if (mode.equals("add")) {
            blockPosition = Integer.parseInt(getIntent().getStringExtra("pallet"));
            binding.color.setText(palletColour);
            binding.txToolbarTitle.setText(R.string.add_a_new_block);
            return;
        }
        blockPosition = Integer.parseInt(getIntent().getStringExtra("pos"));
        binding.color.setText(palletColour);
        binding.txToolbarTitle.setText(R.string.insert_block);
        if (mode.equals("edit")) {
           binding.txToolbarTitle.setText(R.string.edit_block);
            fillUpInputs(blockPosition);
        }
    }

    private void fillUpInputs(int position) {
        HashMap<String, Object> block = blocksList.get(position);

        Object nameObject = block.get("name");
        if (nameObject instanceof String) {
            binding.name.setText((String) nameObject);
        } else {
            binding.name.setError("Invalid name block data");
        }

        Object typeObject = block.get("type");
        if (typeObject instanceof String) {
            String typeString = (String) typeObject;

            if (typeString.equals(" ")) {
                binding.type.setText("regular");
            } else {
                binding.type.setText(typeString);
            }
        } else {
            binding.type.setError("Invalid type block data");
        }

        Object typeName = block.get("typeName");
        if (typeName != null) {
            if (typeName instanceof String) {
                binding.typeName.setText((String) typeName);
            } else {
                binding.typeName.setError("Invalid typeName block data");
            }
        }

        Object specObject = block.get("spec");
        if (specObject instanceof String) {
            binding.spec.setText((String) specObject);
        } else {
            binding.spec.setError("Invalid spec block data");
        }

        Object spec2Object = block.get("spec2");
        if (spec2Object != null) {
            if (spec2Object instanceof String) {
                binding.spec2.setText((String) spec2Object);
            } else {
                binding.spec2.setError("Invalid spec2 block data");
            }
        }

        Object colorObject = block.get("color");
        if (colorObject != null) {
            if (colorObject instanceof String) {
                binding.color.setText((String) colorObject);
            } else {
                binding.color.setError("Invalid color block data");
            }
        } else {
            binding.color.setText(palletColour);
        }

        Object codeObject = block.get("code");
        if (codeObject instanceof String) {
            binding.code.setText((String) codeObject);
        } else {
            binding.code.setHint("(Invalid code block data)");
        }
    }

    private void getBlockList() {
        try {
            blocksList = new Gson().fromJson(FileUtil.readFile(path), Helper.TYPE_MAP_LIST);

            if (blocksList != null) {
                for (int i = 0, blocksListSize = blocksList.size(); i < blocksListSize; i++) {
                    HashMap<String, Object> block = blocksList.get(i);
                    Object name = block.get("name");

                    if (name instanceof String) {
                        id_detector.add((String) name);
                    } else {
                        SketchwareUtil.toastError("Custom Block #" + i + " in current palette has an invalid name");
                    }
                }
                return;
            }
            // fall-through to shared handler
        } catch (JsonParseException e) {
            // fall-through to shared handler
        }
        SketchwareUtil.showFailedToParseJsonDialog(this, new File(path), "Custom Blocks", v -> getBlockList());
        blocksList = new ArrayList<>();
    }

    private void updateBlockSpec(String specId, String color) {
        switch (specId) {
            case " ":
            case "regular":
                binding.spec.setBackgroundResource(R.drawable.block_ori);
                break;

            case "b":
                binding.spec.setBackgroundResource(R.drawable.block_boolean);
                break;

            case "c":
            case "e":
                binding.spec.setBackgroundResource(R.drawable.if_else);
                break;

            case "d":
                binding.spec.setBackgroundResource(R.drawable.block_num);
                break;

            case "f":
                binding.spec.setBackgroundResource(R.drawable.block_stop);
                break;

            default:
                binding.spec.setBackgroundResource(R.drawable.block_string);
                break;
        }
        try {
            binding.spec.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.MULTIPLY);
            binding.spec.setTag(color);
        } catch (Exception e) {
            try {
                binding.spec.getBackground().setColorFilter(Color.parseColor(palletColour), PorterDuff.Mode.MULTIPLY);
                binding.spec.setTag(palletColour);
            } catch (Exception e2) {
                binding.spec.getBackground().setColorFilter(Color.parseColor("#8c8c8c"), PorterDuff.Mode.MULTIPLY);
                binding.spec.setTag("#8c8c8c");
            }
        }
    }

    private void addBlock() {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put("name", binding.name.getText().toString());
        if (binding.type.getText().toString().equals("regular")) {
            tempMap.put("type", " ");
        } else if (binding.type.getText().toString().equals("")) {
            tempMap.put("type", " ");
        } else {
            tempMap.put("type", binding.type.getText().toString());
        }
        tempMap.put("typeName", binding.typeName.getText().toString());
        tempMap.put("spec", binding.spec.getText().toString());
        tempMap.put("color", binding.spec.getTag());
        if (binding.type.getText().toString().equals("e")) {
            tempMap.put("spec2", binding.spec2.getText().toString());
        }
        tempMap.put("code", binding.code.getText().toString());
        tempMap.put("palette", String.valueOf(blockPosition));
        blocksList.add(tempMap);
        FileUtil.writeFile(path, new Gson().toJson(blocksList));
        SketchwareUtil.toast("Saved");
        finish();
    }

    private void insertBlockAt(int position) {
        HashMap<String, Object> tempMap = new HashMap<>();
        tempMap.put("name", binding.name.getText().toString());
        if (binding.type.getText().toString().equals("regular") || binding.type.getText().toString().equals("")) {
            tempMap.put("type", " ");
        } else {
            tempMap.put("type", binding.type.getText().toString());
        }
        tempMap.put("typeName", binding.typeName.getText().toString());
        tempMap.put("spec", binding.spec.getText().toString());
        tempMap.put("color", binding.spec.getTag());
        if (binding.type.getText().toString().equals("e")) {
            tempMap.put("spec2", binding.spec2.getText().toString());
        }
        tempMap.put("code", binding.code.getText().toString());
        tempMap.put("palette", blocksList.get(position).get("palette"));
        blocksList.add(position, tempMap);
        FileUtil.writeFile(path, new Gson().toJson(blocksList));
        SketchwareUtil.toast("Saved");
        finish();
    }

    private void editBlock(int position) {
        HashMap<String, Object> tempMap = blocksList.get(position);
        tempMap.put("name", binding.name.getText().toString());
        if (binding.type.getText().toString().equals("regular") || binding.type.getText().toString().equals("")) {
            tempMap.put("type", " ");
        } else {
            tempMap.put("type", binding.type.getText().toString());
        }
        tempMap.put("typeName", binding.typeName.getText().toString());
        tempMap.put("spec", binding.spec.getText().toString());
        tempMap.put("color", binding.spec.getTag());
        if (binding.type.getText().toString().equals("e")) {
            tempMap.put("spec2", binding.spec2.getText().toString());
        }
        tempMap.put("code", binding.code.getText().toString());
        FileUtil.writeFile(path, new Gson().toJson(blocksList));
        SketchwareUtil.toast("Saved");
        finish();
    }
}
