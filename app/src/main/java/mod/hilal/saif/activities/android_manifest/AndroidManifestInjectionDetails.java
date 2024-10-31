package mod.hilal.saif.activities.android_manifest;

import static pro.sketchware.utility.SketchwareUtil.getDip;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.AddCustomAttributeBinding;
import com.sketchware.remod.databinding.CustomDialogAttributeBinding;

import java.util.ArrayList;
import java.util.HashMap;

import mod.hey.studios.util.Helper;
import mod.hilal.saif.android_manifest.ActComponentsDialog;
import mod.remaker.view.CustomAttributeView;
import pro.sketchware.utility.FileUtil;

public class AndroidManifestInjectionDetails extends BaseAppCompatActivity {

    private static String ATTRIBUTES_FILE_PATH;
    private final ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
    private ListView listView;
    private String src_id;
    private String activityName;
    private String type;
    private String constant;
    private AddCustomAttributeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddCustomAttributeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("sc_id") && getIntent().hasExtra("file_name") && getIntent().hasExtra("type")) {
            src_id = getIntent().getStringExtra("sc_id");
            activityName = getIntent().getStringExtra("file_name").replaceAll(".java", "");
            type = getIntent().getStringExtra("type");
        }
        ATTRIBUTES_FILE_PATH = FileUtil.getExternalStorageDir().concat("/.sketchware/data/").concat(src_id).concat("/Injection/androidmanifest/attributes.json");
        setupConst();
        setToolbar();
        setupViews();
    }

    private void setupConst() {
        switch (type) {
            case "all":
                constant = "_apply_for_all_activities";
                break;

            case "application":
                constant = "_application_attrs";
                break;

            case "permission":
                constant = "_application_permissions";
                break;

            default:
                constant = activityName;
                break;
        }
    }

    private void setupViews() {
        binding.activityEventCard.setVisibility(View.GONE);
        binding.listeners.setVisibility(View.GONE);
        FloatingActionButton fab = findViewById(R.id.add_attr_fab);
        fab.setOnClickListener(v -> showAddDial());
        listView = findViewById(R.id.add_attr_listview);
        refreshList();
    }

    private void refreshList() {
        listMap.clear();
        ArrayList<HashMap<String, Object>> data;
        if (FileUtil.isExistFile(ATTRIBUTES_FILE_PATH)) {
            data = new Gson().fromJson(FileUtil.readFile(ATTRIBUTES_FILE_PATH), Helper.TYPE_MAP_LIST);
            for (int i = 0; i < data.size(); i++) {
                String str = (String) data.get(i).get("name");
                if (str.equals(constant)) {
                    listMap.add(data.get(i));
                }
            }
            listView.setAdapter(new ListAdapter(listMap));
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        }
    }

    private void showDial(int pos) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(R.string.edit_value);
        CustomDialogAttributeBinding attributeBinding = CustomDialogAttributeBinding.inflate(getLayoutInflater());
        dialog.setView(attributeBinding.getRoot());

        attributeBinding.dialogInputRes.setVisibility(View.GONE);
        attributeBinding.dialogInputAttr.setVisibility(View.GONE);

        attributeBinding.dialogInputValue.setText((String) listMap.get(pos).get("value"));
        attributeBinding.dialogInputValue.setHint("android:attr=\"value\"");
        dialog.setPositiveButton(R.string.common_word_save, (dialog1, which) -> {
            listMap.get(pos).put("value", attributeBinding.dialogInputValue.getText().toString());
            applyChange();
        });

        dialog.show();
    }

    private void showAddDial() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(type.equals("permission") ? getString(R.string.add_new_permission) : getString(R.string.add_new_attribute));
        dialog.setIcon(R.drawable.ic_mtrl_tune);
        CustomDialogAttributeBinding attributeBinding = CustomDialogAttributeBinding.inflate(getLayoutInflater());
        dialog.setView(attributeBinding.getRoot());
        if (type.equals("permission")) {
            attributeBinding.dialogInputRes.setText("android");
            attributeBinding.dialogInputAttr.setText("name");
            attributeBinding.inputLayoutValue.setHint("permission");
        }
        dialog.setPositiveButton(R.string.common_word_save, (dialog1, which) -> {
            String fstr = attributeBinding.dialogInputRes.getText().toString().trim() + ":" + attributeBinding.dialogInputAttr.getText().toString().trim() + "=\"" + attributeBinding.dialogInputValue.getText().toString().trim() + "\"";
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", constant);
            map.put("value", fstr);
            listMap.add(map);
            applyChange();
            dialog1.dismiss();
        });

        dialog.setNegativeButton(R.string.common_word_cancel, (dialog1, which) -> dialog1.dismiss());
        dialog.show();
    }

    private void applyChange() {
        ArrayList<HashMap<String, Object>> data;
        if (FileUtil.isExistFile(ATTRIBUTES_FILE_PATH)) {
            data = new Gson().fromJson(FileUtil.readFile(ATTRIBUTES_FILE_PATH), Helper.TYPE_MAP_LIST);
            for (int i = data.size() - 1; i > -1; i--) {
                String str = (String) data.get(i).get("name");
                if (str.equals(constant)) {
                    data.remove(i);
                }
            }
            data.addAll(listMap);
        } else {
            data = new ArrayList<>(listMap);
        }
        FileUtil.writeFile(ATTRIBUTES_FILE_PATH, new Gson().toJson(data));
        refreshList();
    }

    private TextView newText(String str, float size, int color, int width, int height, float weight) {
        TextView temp_card = new TextView(this);
        temp_card.setLayoutParams(new LinearLayout.LayoutParams(width, height, weight));
        temp_card.setPadding((int) getDip(4), (int) getDip(4), (int) getDip(4), (int) getDip(4));
        temp_card.setTextColor(color);
        temp_card.setText(str);
        temp_card.setTextSize(size);
        return temp_card;
    }

    private void setToolbar() {
        String str = switch (type) {
            case "all" -> getString(R.string.attributes_for_all_activities);
            case "application" -> getString(R.string.application_attributes);
            case "permission" -> getString(R.string.application_permissions);
            default -> activityName;
        };
        Toolbar toolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar_improved, binding.background, false);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(str);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        binding.background.addView(toolbar, 0);
        if (!str.equals(getString(R.string.attributes_for_all_activities)) && !str.equals(getString(R.string.application_attributes)) && !str.equals(getString(R.string.application_permissions))) {
            // Feature description: allows to inject anything into the {@code activity} tag of the Activity
            // (yes, Command Blocks can do that too, but removing features is bad.)
            TextView actComponent = newText("Components ASD", 15, Color.parseColor("#ffffff"), -2, -2, 0);
            actComponent.setTypeface(Typeface.DEFAULT_BOLD);
            toolbar.addView(actComponent);
            actComponent.setOnClickListener(v -> {
                ActComponentsDialog acd = new ActComponentsDialog(this, src_id, activityName);
                acd.show();
            });
        }
    }

    private class ListAdapter extends BaseAdapter {

        private final ArrayList<HashMap<String, Object>> _data;

        public ListAdapter(ArrayList<HashMap<String, Object>> _arr) {
            _data = _arr;
        }

        @Override
        public int getCount() {
            return _data.size();
        }

        @Override
        public HashMap<String, Object> getItem(int position) {
            return _data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CustomAttributeView attributeView = new CustomAttributeView(parent.getContext());

            try {
                SpannableString spannableString = new SpannableString((String) _data.get(position).get("value"));
                spannableString.setSpan(new ForegroundColorSpan(0xff7a2e8c), 0, ((String) _data.get(position).get("value")).indexOf(":"), 33);
                spannableString.setSpan(new ForegroundColorSpan(0xff212121), ((String) _data.get(position).get("value")).indexOf(":"), ((String) _data.get(position).get("value")).indexOf("=") + 1, 33);
                spannableString.setSpan(new ForegroundColorSpan(0xff45a245), ((String) _data.get(position).get("value")).indexOf("\""), ((String) _data.get(position).get("value")).length(), 33);
                attributeView.getTextView().setText(spannableString);
            } catch (Exception e) {
                attributeView.getTextView().setText((String) _data.get(position).get("value"));
            }

            attributeView.getImageView().setVisibility(View.GONE);
            attributeView.setOnClickListener(v -> showDial(position));
            attributeView.setOnLongClickListener(v -> {
                new MaterialAlertDialogBuilder(AndroidManifestInjectionDetails.this)
                        .setTitle(R.string.delete_this_attribute)
                        .setMessage(R.string.this_action_cannot_be_undone)
                        .setPositiveButton(R.string.common_word_delete, (dialog, which) -> {
                            listMap.remove(position);
                            applyChange();
                        })
                        .setNegativeButton(R.string.common_word_cancel, null)
                        .show();

                return true;
            });

            return attributeView;
        }
    }
}
