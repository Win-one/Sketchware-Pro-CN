package pro.sketchware.fragments.settings.language;

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

import a.a.a.qA;
import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;
import pro.sketchware.databinding.FragmentSettingsLanguageBinding;

public class SettingsLanguageFragment extends qA {

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
        Locale locale = MultiLanguages.getAppLanguage(this.requireContext());
        if (LocaleContract.getSimplifiedChineseLocale().equals(locale)) {
            binding.toggleLanguage.check(R.id.language_chinese);
        } else if (LocaleContract.getEnglishLocale().equals(locale)) {
            binding.toggleLanguage.check(R.id.language_english);
        } else {
            binding.toggleLanguage.check(R.id.language_system);
        }

        binding.toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                boolean restart = false;
                if (checkedId == R.id.language_system) {
                    restart = MultiLanguages.clearAppLanguage(this.requireContext());
                } else if (checkedId == R.id.language_chinese) {
                    restart = MultiLanguages.setAppLanguage(this.requireContext(), LocaleContract.getSimplifiedChineseLocale());
                } else if (checkedId == R.id.language_english) {
                    restart = MultiLanguages.setAppLanguage(this.requireContext(), LocaleContract.getEnglishLocale());
                }
                if (restart) {
                    startActivity(new Intent(this.requireContext(), MainActivity.class));
                    requireActivity().finish();
                }
            }

        });
    }
}
