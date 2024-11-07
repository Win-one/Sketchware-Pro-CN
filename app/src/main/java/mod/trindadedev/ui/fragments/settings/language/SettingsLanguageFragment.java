package mod.trindadedev.ui.fragments.settings.language;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.language.LocaleContract;
import com.hjq.language.MultiLanguages;

import java.util.Locale;

import mod.trindadedev.ui.fragments.BaseFragment;
import pro.sketchware.R;
import pro.sketchware.SketchApplication;
import pro.sketchware.activities.main.activities.MainActivity;
import pro.sketchware.databinding.FragmentSettingsLanguageBinding;

public class SettingsLanguageFragment extends BaseFragment {

    private FragmentSettingsLanguageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsLanguageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureToolbar(binding.topAppBar);
        configureLanguageController();
    }

    private void configureLanguageController() {
        if (MultiLanguages.isSystemLanguage(this.getContext())) {
            binding.toggleLanguage.check(R.id.language_system);
        } else {
            Locale locale = MultiLanguages.getAppLanguage(this.getContext());
            if (LocaleContract.getSimplifiedChineseLocale().equals(locale)) {
                binding.toggleLanguage.check(R.id.language_chinese);
            } else if (LocaleContract.getEnglishLocale().equals(locale)) {
                binding.toggleLanguage.check(R.id.language_english);
            }
        }

        binding.toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            boolean restart = false;
            if (isChecked) {
                if (checkedId == R.id.language_chinese) {
                    restart = MultiLanguages.setAppLanguage(this.getContext(), LocaleContract.getSimplifiedChineseLocale());
                } else if (checkedId == R.id.language_system) {
                    restart = MultiLanguages.clearAppLanguage(this.getContext());
                } else if (checkedId == R.id.language_english) {
                    restart = MultiLanguages.setAppLanguage(this.getContext(), LocaleContract.getEnglishLocale());
                }
            }
            if (restart) {
                Intent intent = new Intent(SketchApplication.getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                SketchApplication.getContext().startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }
}
