package mod.hilal.saif.activities.tools;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.AddCustomAttributeBinding;
import com.sketchware.remod.databinding.AddNewListenerBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mod.SketchwareUtil;
import mod.agus.jcoderz.lib.FileUtil;
import mod.hey.studios.util.Helper;
import mod.hilal.saif.events.EventsHandler;

public class EventsMaker extends AppCompatActivity {

    public static final File EVENT_EXPORT_LOCATION = new File(Environment.getExternalStorageDirectory(),
            ".sketchware/data/system/export/events/");
    public static final File EVENTS_FILE = new File(Environment.getExternalStorageDirectory(),
            ".sketchware/data/system/events.json");
    public static final File LISTENERS_FILE = new File(Environment.getExternalStorageDirectory(),
            ".sketchware/data/system/listeners.json");
    private ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();

    private AddCustomAttributeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = AddCustomAttributeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setToolbar();
        setupViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventsHandler.refreshCachedCustomEvents();
        EventsHandler.refreshCachedCustomListeners();
    }

    private void setupViews() {
        binding.eventSub.setText(getNumOfEvents(""));
        binding.activityEventCard.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), EventsMakerDetails.class);
            intent.putExtra("lis_name", "");
            startActivity(intent);
        });
        binding.addAttrFab.setOnClickListener(v -> showAddDial());
        refreshList();
    }

    private void showAddDial() {
        final AlertDialog create = new AlertDialog.Builder(this).create();
        AddNewListenerBinding listenerBinding = AddNewListenerBinding.inflate(getLayoutInflater());
        create.setView(listenerBinding.getRoot());
        create.setCanceledOnTouchOutside(true);
        create.requestWindowFeature(Window.FEATURE_NO_TITLE);
        create.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        create.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        listenerBinding.save.setOnClickListener(v -> {
            if (!listenerBinding.name.getText().toString().equals("")) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("name", listenerBinding.name.getText().toString());
                if (listenerBinding.separate.isChecked()) {
                    hashMap.put("code", "//" + listenerBinding.name.getText().toString() + "\n" + listenerBinding.code.getText().toString());
                    hashMap.put("s", "true");
                } else {
                    hashMap.put("code", listenerBinding.code.getText().toString());
                    hashMap.put("s", "false");
                }
                hashMap.put("imports", listenerBinding.customimport.getText().toString());
                listMap.add(hashMap);
                addItem();
                create.dismiss();
                return;
            }
            SketchwareUtil.toastError("Invalid name!");
        });
        listenerBinding.cancel.setOnClickListener(Helper.getDialogDismissListener(create));
        create.show();
    }

    private void overrideEvents(String before, String after) {
        ArrayList<HashMap<String, Object>> events = new ArrayList<>();
        if (FileUtil.isExistFile(EVENTS_FILE.getAbsolutePath())) {
            events = new Gson()
                    .fromJson(FileUtil.readFile(EVENTS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).get("listener").toString().equals(before)) {
                    events.get(i).put("listener", after);
                }
            }
        }
        FileUtil.writeFile(EVENTS_FILE.getAbsolutePath(), new Gson().toJson(events));
    }

    private void editItemDialog(final int position) {
        final AlertDialog create = new AlertDialog.Builder(this).create();
        AddNewListenerBinding listenerBinding = AddNewListenerBinding.inflate(getLayoutInflater());
        create.setView(listenerBinding.getRoot());
        create.setCanceledOnTouchOutside(true);
        create.requestWindowFeature(Window.FEATURE_NO_TITLE);
        create.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        create.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        listenerBinding.name.setText(listMap.get(position).get("name").toString());
        listenerBinding.code.setText(listMap.get(position).get("code").toString());
        listenerBinding.customimport.setText(listMap.get(position).get("imports").toString());
        if (listMap.get(position).containsKey("s") && listMap.get(position).get("s").toString().equals("true")) {
            listenerBinding.separate.setChecked(true);
            ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(listMap.get(position).get("code").toString().split("\n")));
            if (arrayList.get(0).contains("//" + listenerBinding.name.getText().toString())) {
                arrayList.remove(0);
            }
            String str = "";
            for (int i2 = 0; i2 < arrayList.size(); i2++) {
                if (str.equals("")) {
                    str = arrayList.get(i2);
                } else {
                    str = str.concat("\n").concat(arrayList.get(i2));
                }
            }
            listenerBinding.code.setText(str);
        }
        listenerBinding.save.setOnClickListener(v -> {
            if (!listenerBinding.name.getText().toString().equals("")) {
                HashMap<String, Object> hashMap = listMap.get(position);
                overrideEvents((String) hashMap.get("name"), listenerBinding.name.getText().toString());
                hashMap.put("name", listenerBinding.name.getText().toString());
                if (listenerBinding.separate.isChecked()) {
                    hashMap.put("code", "//" + listenerBinding.name.getText().toString() + "\n" + listenerBinding.code.getText().toString());
                    hashMap.put("s", "true");
                } else {
                    hashMap.put("code", listenerBinding.code.getText().toString());
                    hashMap.put("s", "false");
                }
                hashMap.put("imports", listenerBinding.customimport.getText().toString());
                FileUtil.writeFile(LISTENERS_FILE.getAbsolutePath(), new Gson().toJson(listMap));
                refreshList();
                create.dismiss();
                return;
            }
            SketchwareUtil.toastError("Invalid name!");
        });
        listenerBinding.cancel.setOnClickListener(Helper.getDialogDismissListener(create));
        create.show();
    }

    private void refreshList() {
        listMap.clear();
        if (FileUtil.isExistFile(LISTENERS_FILE.getAbsolutePath())) {
            listMap = new Gson().fromJson(FileUtil.readFile(LISTENERS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);
            binding.addAttrListview.setAdapter(new ListAdapter(listMap));
            ((BaseAdapter) binding.addAttrListview.getAdapter()).notifyDataSetChanged();
        }
    }

    private void deleteItem(int position) {
        listMap.remove(position);
        FileUtil.writeFile(LISTENERS_FILE.getAbsolutePath(), new Gson().toJson(listMap));
        refreshList();
    }

    private void deleteRelatedEvents(String name) {
        ArrayList<HashMap<String, Object>> events = new ArrayList<>();
        if (FileUtil.isExistFile(EVENTS_FILE.getAbsolutePath())) {
            events = new Gson()
                    .fromJson(FileUtil.readFile(EVENTS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);
            for (int i = events.size() - 1; i > -1; i--) {
                if (events.get(i).get("listener").toString().equals(name)) {
                    events.remove(i);
                }
            }
        }
        FileUtil.writeFile(EVENTS_FILE.getAbsolutePath(), new Gson().toJson(events));
    }

    private void addItem() {
        FileUtil.writeFile(LISTENERS_FILE.getAbsolutePath(), new Gson().toJson(listMap));
        refreshList();
    }

    private void openFileExplorerImport() {
        DialogProperties dialogProperties = new DialogProperties();
        dialogProperties.selection_mode = 0;
        dialogProperties.selection_type = 0;
        File file = new File(FileUtil.getExternalStorageDir());
        dialogProperties.root = file;
        dialogProperties.error_dir = file;
        dialogProperties.offset = file;
        dialogProperties.extensions = null;
        FilePickerDialog filePickerDialog = new FilePickerDialog(this, dialogProperties);
        filePickerDialog.setTitle(getString(R.string.select_a_txt_file));
        filePickerDialog.setDialogSelectionListener(selections -> {
            if (FileUtil.readFile(selections[0]).equals("")) {
                SketchwareUtil.toastError(getString(R.string.the_selected_file_is_empty));
            } else if (FileUtil.readFile(selections[0]).equals("[]")) {
                SketchwareUtil.toastError(getString(R.string.the_selected_file_is_empty));
            } else {
                try {
                    String[] split = FileUtil.readFile(selections[0]).split("\n");
                    importEvents(new Gson().fromJson(split[0], Helper.TYPE_MAP_LIST),
                            new Gson().fromJson(split[1], Helper.TYPE_MAP_LIST));
                } catch (Exception e) {
                    SketchwareUtil.toastError(getString(R.string.invalid_file));
                }
            }
        });
        filePickerDialog.show();
    }

    private void importEvents(ArrayList<HashMap<String, Object>> data, ArrayList<HashMap<String, Object>> data2) {
        ArrayList<HashMap<String, Object>> events = new ArrayList<>();
        if (FileUtil.isExistFile(EVENTS_FILE.getAbsolutePath())) {
            events = new Gson()
                    .fromJson(FileUtil.readFile(EVENTS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);
        }
        events.addAll(data2);
        FileUtil.writeFile(EVENTS_FILE.getAbsolutePath(), new Gson().toJson(events));
        listMap.addAll(data);
        FileUtil.writeFile(LISTENERS_FILE.getAbsolutePath(), new Gson().toJson(listMap));
        refreshList();
        SketchwareUtil.toast("Successfully imported events");
    }

    private void exportAll() {
        ArrayList<HashMap<String, Object>> events = new ArrayList<>();
        if (FileUtil.isExistFile(EVENTS_FILE.getAbsolutePath())) {
            events = new Gson().fromJson(FileUtil.readFile(EVENTS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);
        }
        FileUtil.writeFile(new File(EVENT_EXPORT_LOCATION, "All_Events.txt").getAbsolutePath(),
                new Gson().toJson(listMap) + "\n" + new Gson().toJson(events));
        SketchwareUtil.toast("Successfully exported events to:\n" +
                "/Internal storage/.sketchware/data/system/export/events", Toast.LENGTH_LONG);
    }

    private void export(int p) {
        String concat = FileUtil.getExternalStorageDir().concat("/.sketchware/data/system/export/events/");
        ArrayList<HashMap<String, Object>> ex = new ArrayList<>();
        ex.add(listMap.get(p));
        ArrayList<HashMap<String, Object>> ex2 = new ArrayList<>();
        if (FileUtil.isExistFile(EVENTS_FILE.getAbsolutePath())) {
            ArrayList<HashMap<String, Object>> events = new Gson()
                    .fromJson(FileUtil.readFile(EVENTS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).get("listener").toString().equals(listMap.get(p).get("name"))) {
                    ex2.add(events.get(i));
                }
            }
        }
        FileUtil.writeFile(concat + ex.get(0).get("name").toString() + ".txt", new Gson().toJson(ex) + "\n" + new Gson().toJson(ex2));
        SketchwareUtil.toast("Successfully exported event to:\n" +
                "/Internal storage/.sketchware/data/system/export/events", Toast.LENGTH_LONG);
    }

    private String getNumOfEvents(String str) {
        int eventAmount;
        if (FileUtil.isExistFile(EVENTS_FILE.getAbsolutePath())) {
            ArrayList<HashMap<String, Object>> events = new Gson()
                    .fromJson(FileUtil.readFile(EVENTS_FILE.getAbsolutePath()), Helper.TYPE_MAP_LIST);
            eventAmount = 0;
            for (int i = 0; i < events.size(); i++) {
                if (events.get(i).get("listener").toString().equals(str)) {
                    eventAmount++;
                }
            }
        } else {
            eventAmount = 0;
        }
        return getString(R.string.events) + eventAmount;
    }

    private void setToolbar() {
        Toolbar toolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar_improved, binding.background, false);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.event_manager);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());
        binding.background.addView(toolbar, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, R.string.import_events);
        menu.add(Menu.NONE, 2, Menu.NONE, R.string.export_events);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1 -> openFileExplorerImport();
            case 2 -> {
                exportAll();
                SketchwareUtil.toast("Successfully exported events to:\n" +
                        "/Internal storage/.sketchware/data/system/export/events", Toast.LENGTH_LONG);
            }
            default -> {
                return false;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class ListAdapter extends BaseAdapter {

        private final ArrayList<HashMap<String, Object>> _data;

        public ListAdapter(ArrayList<HashMap<String, Object>> arrayList) {
            _data = arrayList;
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
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.custom_view_pro, parent, false);
            }
            MaterialCardView materialCardView = convertView.findViewById(R.id.custom_view_pro_background);
            ImageView imageView = convertView.findViewById(R.id.custom_view_pro_img);
            TextView textView = convertView.findViewById(R.id.custom_view_pro_title);
            imageView.setImageResource(R.drawable.event_on_response_48dp);
            ((LinearLayout) imageView.getParent()).setGravity(Gravity.CENTER);
            textView.setText((String) _data.get(position).get("name"));
            ((TextView) convertView.findViewById(R.id.custom_view_pro_subtitle)).setText(getNumOfEvents(textView.getText().toString()));
            materialCardView.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), EventsMakerDetails.class);
                intent.putExtra("lis_name", (String) _data.get(position).get("name"));
                startActivity(intent);
            });
            materialCardView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(EventsMaker.this)
                        .setTitle(_data.get(position).get("name").toString())
                        .setItems(new String[]{getString(R.string.common_word_edit), getString(R.string.common_word_export), getString(R.string.common_word_delete)}, (dialog, which) -> {
                            switch (which) {
                                case 0:
                                    editItemDialog(position);
                                    break;

                                case 1:
                                    export(position);
                                    break;

                                case 2:
                                    deleteRelatedEvents(_data.get(position).get("name").toString());
                                    deleteItem(position);
                                    break;

                                default:
                            }
                        })
                        .show();

                return true;
            });

            return convertView;
        }
    }
}
