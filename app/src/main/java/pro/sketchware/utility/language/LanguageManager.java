package pro.sketchware.utility.language;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.hjq.language.LocaleContract;
import com.hjq.language.MultiLanguages;

import pro.sketchware.SketchApplication;
import pro.sketchware.activities.main.activities.MainActivity;

public class LanguageManager {

    public static final String LANGUAGE_PREF = "languagedata";
    public static final String LANGUAGE_KEY = "idelabguage";

    public static final int LANGUAGE_SYSTEM = 0;
    public static final int LANGUAGE_CHINESE = 1;
    public static final int LANGUAGE_ENGLISH = 2;

    public static void applyLanguage(Context context, int type) {
        saveLanguage(context, type);
        boolean restart = switch (type) {
            case LANGUAGE_CHINESE ->
                    MultiLanguages.setAppLanguage(context, LocaleContract.getSimplifiedChineseLocale());
            case LANGUAGE_ENGLISH ->
                    MultiLanguages.setAppLanguage(context, LocaleContract.getEnglishLocale());
            case LANGUAGE_SYSTEM -> MultiLanguages.clearAppLanguage(context);
            default -> false;
        };
        if (restart) {
            Intent intent = new Intent(SketchApplication.getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            SketchApplication.getContext().startActivity(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public static int getCurrentLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE);
        return preferences.getInt(LANGUAGE_KEY, LANGUAGE_SYSTEM);
    }

    private static void saveLanguage(Context context, int theme) {
        SharedPreferences preferences = context.getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(LANGUAGE_KEY, theme);
        editor.apply();
    }
}