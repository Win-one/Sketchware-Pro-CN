package com.besome.sketch.language;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.besome.sketch.SketchApplication;
import com.besome.sketch.language.util.MultiLanguageUtil;
import com.besome.sketch.language.util.SpUtil;
import com.besome.sketch.lib.base.BaseAppCompatActivity;
import com.sketchware.remod.R;
import com.sketchware.remod.databinding.ActivityLanguageSettingsBinding;

import java.util.Locale;

import mod.hey.studios.util.Helper;
import pro.sketchware.activities.main.activities.MainActivity;

public class LanguageSettingsActivity extends BaseAppCompatActivity {
    private ActivityLanguageSettingsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLanguageSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.topAppBar.setNavigationOnClickListener(Helper.getBackPressedClickListener(this));
        getAppLanguage();

        binding.toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.rb_chinese) {
                    changeLanguage("zh", "CN");
                } else if (checkedId == R.id.rb_english) {
                    changeLanguage("en", "US");
                } else {
                    changeLanguage("", "");
                }
            }
        });
    }

    private void getAppLanguage() {
        int checkid = SpUtil.getInt("CheckID");
        if (checkid == R.id.rb_chinese) {
            binding.toggleLanguage.check(R.id.rb_chinese);
        } else if (checkid == R.id.rb_english) {
            binding.toggleLanguage.check(R.id.rb_english);
            ;
        } else {
            binding.toggleLanguage.check(R.id.rb_follow_system);
            ;
        }
    }

    //修改应用内语言设置
    private void changeLanguage(String language, String area) {
        SpUtil.saveInt("CheckID", binding.toggleLanguage.getCheckedButtonId());
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
