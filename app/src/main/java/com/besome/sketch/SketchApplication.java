package com.besome.sketch;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.besome.sketch.language.ConstantGlobal;
import com.besome.sketch.language.util.MultiLanguageUtil;
import com.besome.sketch.language.util.SpUtil;
import com.besome.sketch.tools.CollectErrorActivity;

import com.google.android.gms.analytics.Tracker;
import com.google.android.material.color.DynamicColors;

import java.util.Locale;

import mod.trindadedev.settings.appearance.theme.manage.ThemeManager;

public class SketchApplication extends Application {

    private static Context mApplicationContext;

    public static Context getContext() {
        return mApplicationContext;
    }

    public synchronized Tracker a() {
        return new Tracker();
    }

    @Override
    public void onCreate() {
        mApplicationContext = getApplicationContext();
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("SketchApplication", "Uncaught exception on thread " + thread.getName(), throwable);

            Intent intent = new Intent(getApplicationContext(), CollectErrorActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("error", Log.getStackTraceString(throwable));
            ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                    .set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                            1000,
                            PendingIntent.getActivity(getApplicationContext(), 11111, intent,
                                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE));
            Process.killProcess(Process.myPid());
            System.exit(1);
            if (uncaughtExceptionHandler != null) {
                uncaughtExceptionHandler.uncaughtException(thread, throwable);
            }
        });
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        ThemeManager.applyTheme(this, ThemeManager.getCurrentTheme(this));
        mApplicationContext = getApplicationContext();
        //初始化本地语言，这句话可以不要，因为在attachBaseContext中已经处理了
//        changeLanguage();
        //注册Activity生命周期监听回调，此部分一定加上，因为有些版本不加的话多语言切换不回来
        registerActivityLifecycleCallbacks(callbacks);
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    private void changeLanguage() {
        String spLanguage = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_LANGUAGE);
        String spCountry = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_COUNTRY);

        if (!TextUtils.isEmpty(spLanguage) && !TextUtils.isEmpty(spCountry)) {
            // 如果有一个不同
            if (!MultiLanguageUtil.isSameWithSetting(this)) {
                Locale locale = new Locale(spLanguage, spCountry);
                MultiLanguageUtil.changeAppLanguage(getApplicationContext(), locale, false);
            }
        }
    }

    ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            String language = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_LANGUAGE);
            String country = SpUtil.getString(getApplicationContext(), ConstantGlobal.LOCALE_COUNTRY);
            if (!TextUtils.isEmpty(language) && !TextUtils.isEmpty(country)) {
                //强制修改应用语言
                if (!MultiLanguageUtil.isSameWithSetting(activity)) {
                    Locale locale = new Locale(language, country);
                    MultiLanguageUtil.changeAppLanguage(activity, locale, false);
//                    activity.recreate();
                }
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
        //Activity 其它生命周期的回调
    };

    @Override
    protected void attachBaseContext(Context base) {
        //系统语言等设置发生改变时会调用此方法，需要要重置app语言
        super.attachBaseContext(MultiLanguageUtil.attachBaseContext(base));
    }
}
