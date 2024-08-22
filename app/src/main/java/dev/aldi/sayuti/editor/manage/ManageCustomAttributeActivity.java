package dev.aldi.sayuti.editor.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.sketchware.remod.R;
import com.sketchware.remod.databinding.ManageCustomAttributeBinding;
import com.sketchware.remod.databinding.ManageCustomComponentAddBinding;

import java.util.LinkedList;
import java.util.List;

import dev.aldi.sayuti.editor.injection.AddCustomAttributeActivity;
import dev.aldi.sayuti.editor.injection.AppCompatInjection;
import mod.remaker.view.CustomAttributeView;

public class ManageCustomAttributeActivity extends AppCompatActivity {

    private final List<String> customAttributeLocations = new LinkedList<>();
    private String sc_id = "";
    private String xmlFilename = "";
    private ManageCustomAttributeBinding binding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ManageCustomAttributeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.topAppBar.setNavigationOnClickListener(view -> onBackPressed());

        if (getIntent().hasExtra("sc_id") && getIntent().hasExtra("file_name")) {
            sc_id = getIntent().getStringExtra("sc_id");
            xmlFilename = getIntent().getStringExtra("file_name").replaceAll(".xml", "");
            addType("Toolbar");
            addType("AppBarLayout");
            addType("CoordinatorLayout");
            addType("FloatingActionButton");
            addType("DrawerLayout");
            addType("NavigationDrawer");
            binding.manageAttrListview.setAdapter(new CustomAdapter(customAttributeLocations));
        } else {
            finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppCompatInjection.refreshInjections();
    }

    private void addType(String type) {
        customAttributeLocations.add(type);
    }

    private class CustomAdapter extends BaseAdapter {

        private final List<String> _data;

        public CustomAdapter(List<String> arrayList) {
            _data = arrayList;
        }

        @Override
        public int getCount() {
            return _data.size();
        }

        @Override
        public String getItem(int position) {
            return _data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            CustomAttributeView attributeView = new CustomAttributeView(parent.getContext());

            attributeView.icon.setImageResource(R.drawable.ic_property_inject);
            attributeView.text.setText(getItem(position));
            attributeView.setOnClickListener(v -> {
                Intent i = new Intent();
                i.setClass(getApplicationContext(), AddCustomAttributeActivity.class);
                i.putExtra("sc_id", sc_id);
                i.putExtra("file_name", xmlFilename);
                i.putExtra("widget_type", getItem(position).toLowerCase());
                startActivity(i);
            });

            return attributeView;
        }
    }
}