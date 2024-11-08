package pro.sketchware;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.besome.sketch.tools.CollectErrorActivity;
import com.hjq.language.MultiLanguages;

import pro.sketchware.utility.theme.ThemeManager;

public class SketchApplication extends Application {
    private static Context mApplicationContext;
    private static SketchApplication mInstance;
    public static Context getContext() {
        return mApplicationContext;
    }

    @Override
    public void onCreate() {
        MultiLanguages.init(this);
        mApplicationContext = getApplicationContext();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                Intent intent = new Intent(getApplicationContext(), CollectErrorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("error", Log.getStackTraceString(throwable));
                startActivity(intent);
                Process.killProcess(Process.myPid());
                System.exit(1);
            }
        });
        super.onCreate();
        ThemeManager.applyTheme(this, ThemeManager.getCurrentTheme(this));

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(MultiLanguages.attach(base));
    }

    public static Context getInstance() {
        if (mInstance == null) {
            mInstance = new SketchApplication();
        }
        return mInstance;
    }
}
