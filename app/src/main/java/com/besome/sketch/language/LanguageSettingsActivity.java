package com.besome.sketch.language;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.besome.sketch.MainActivity;
import com.besome.sketch.SketchApplication;
import com.besome.sketch.language.util.MultiLanguageUtil;
import com.besome.sketch.language.util.SpUtil;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.sketchware.remod.R;

import java.util.Locale;
import java.util.Objects;

import mod.hey.studios.util.Helper;

public class LanguageSettingsActivity extends BaseAppCompatActivity {
    private RadioGroup radioGroup_language;
    private RadioButton radioButton_fs;
    private RadioButton radioButton_zh;
    private RadioButton radioButton_en;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        radioGroup_language = findViewById(R.id.rg_language);
        radioButton_zh = findViewById(R.id.rb_chinese);
        radioButton_en = findViewById(R.id.rb_english);
        radioButton_fs = findViewById(R.id.rb_follow_system);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.layout_main_logo).setVisibility(View.GONE);
        getSupportActionBar().setTitle(R.string.main_drawer_title_language_settings);
        toolbar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        getAppLanguage();
    }

    private void getAppLanguage() {
        radioButton_fs.setId(R.id.rb_follow_system);
        radioButton_zh.setId(R.id.rb_chinese);
        radioButton_en.setId(R.id.rb_english);
        int checkid = SpUtil.getInt("CheckID");
        if (checkid == R.id.rb_chinese) {
            radioButton_zh.setChecked(true);
        } else if (checkid == R.id.rb_english) {
            radioButton_en.setChecked(true);
        } else {
            radioButton_fs.setChecked(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int v = item.getItemId();
        if (v == R.id.menu_save) {
            if (radioGroup_language.getCheckedRadioButtonId() == R.id.rb_chinese) {
                changeLanguage("zh", "CN");
            } else if (radioGroup_language.getCheckedRadioButtonId() == R.id.rb_english) {
                changeLanguage("en", "US");
            } else {
                changeLanguage("", "");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @param language
     * @param area
     */
    //修改应用内语言设置
    private void changeLanguage(String language, String area) {
        SpUtil.saveInt("CheckID", radioGroup_language.getCheckedRadioButtonId());
        if (TextUtils.isEmpty(language) && TextUtils.isEmpty(area)) {
            //如果语言和地区都是空，那么跟随系统
            SpUtil.saveString(ConstantGlobal.LOCALE_LANGUAGE, "");
            SpUtil.saveString(ConstantGlobal.LOCALE_COUNTRY, "");
        } else {
            //不为空，那么修改app语言，并true是把语言信息保存到sp中，false是不保存到sp中
            Locale newLocale = new Locale(language, area);
            MultiLanguageUtil.changeAppLanguage(LanguageSettingsActivity.this, newLocale, true);
            MultiLanguageUtil.changeAppLanguage(SketchApplication.getContext(), newLocale, true);
        }
        //重启app,这一步一定要加上，如果不重启app，可能打开新的页面显示的语言会不正确
        Intent intent = new Intent(SketchApplication.getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        SketchApplication.getContext().startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
