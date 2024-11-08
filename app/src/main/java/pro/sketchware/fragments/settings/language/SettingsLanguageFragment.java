package pro.sketchware.fragments.settings.language;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import a.a.a.qA;
import pro.sketchware.R;
import pro.sketchware.databinding.FragmentSettingsLanguageBinding;
import pro.sketchware.utility.language.LanguageManager;

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
        switch (LanguageManager.getCurrentLanguage(requireContext())) {
            case LanguageManager.LANGUAGE_CHINESE:
                binding.toggleLanguage.check(R.id.language_chinese);
                break;
            case LanguageManager.LANGUAGE_ENGLISH:
                binding.toggleLanguage.check(R.id.language_english);
                break;
            default:
                binding.toggleLanguage.check(R.id.language_system);
                break;
        }

        binding.toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.language_chinese) {
                    LanguageManager.applyLanguage(requireContext(), LanguageManager.LANGUAGE_CHINESE);
                } else if (checkedId == R.id.language_system) {
                    LanguageManager.applyLanguage(requireContext(), LanguageManager.LANGUAGE_SYSTEM);
                } else if (checkedId == R.id.language_english) {
                    LanguageManager.applyLanguage(requireContext(), LanguageManager.LANGUAGE_ENGLISH);
                } else {
                    LanguageManager.applyLanguage(requireContext(), LanguageManager.LANGUAGE_SYSTEM);
                }
            }

        });
    }
}
