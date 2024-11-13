package pro.sketchware.fragments.settings.language;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;
import com.hjq.language.LocaleContract;
import com.hjq.language.MultiLanguages;

import java.util.Locale;

import a.a.a.qA;
import pro.sketchware.R;
import pro.sketchware.activities.main.activities.MainActivity;
import pro.sketchware.databinding.FragmentSettingsLanguageBinding;

public class SettingsLanguageFragment extends qA implements View.OnClickListener {
    private FragmentSettingsLanguageBinding binding;
    private MaterialCardView selectedLanguageCard;
    private boolean restart = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsLanguageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();
        initializeLanguageSettings();
        binding.switchSystem.setOnClickListener(this);
        binding.languageChinese.setOnClickListener(this);
        binding.languageEnglish.setOnClickListener(this);
        binding.switchSystem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setLanguageCardsEnabled(!isChecked);
            if (isChecked) {
                restart = MultiLanguages.clearAppLanguage(this.requireContext());
            }
            checkStart();
        });
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void initializeLanguageSettings() {
        boolean isSystemLanguage = MultiLanguages.isSystemLanguage(requireContext());
        binding.switchSystem.setChecked(isSystemLanguage);
        updateLanguageCardSelection();
        setLanguageCardsEnabled(!isSystemLanguage);
    }

    private void updateLanguageCardSelection() {
        unselectSelectedLanguageCard();
        MaterialCardView newSelection;
        Locale locale = MultiLanguages.getAppLanguage(this.requireContext());
        if (LocaleContract.getSimplifiedChineseLocale().equals(locale)) {
            newSelection = binding.languageChinese;
        } else if (LocaleContract.getEnglishLocale().equals(locale)) {
            newSelection = binding.languageEnglish;
        } else {
            newSelection = null;
        }
        if (newSelection != null && !binding.switchSystem.isChecked()) {
            newSelection.setChecked(true);
            selectedLanguageCard = newSelection;
        }
    }

    private void unselectSelectedLanguageCard() {
        if (selectedLanguageCard != null) {
            selectedLanguageCard.setChecked(false);
            selectedLanguageCard = null;
        }
    }

    private void setLanguageCardsEnabled(boolean enabled) {
        binding.languageChinese.setEnabled(enabled);
        binding.languageEnglish.setEnabled(enabled);

        float alpha = enabled ? 1.0f : 0.5f;
        binding.languageChinese.animate().alpha(alpha).start();
        binding.languageEnglish.animate().alpha(alpha).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.language_system) {
            binding.switchSystem.setChecked(!binding.switchSystem.isChecked());
        } else if (id == R.id.language_chinese) {
            if (!binding.switchSystem.isChecked()) {
                restart = MultiLanguages.setAppLanguage(this.requireContext(), LocaleContract.getSimplifiedChineseLocale());
                updateLanguageCardSelection();
            }
        } else if (id == R.id.language_english) {
            if (!binding.switchSystem.isChecked()) {
                restart = MultiLanguages.setAppLanguage(this.requireContext(), LocaleContract.getEnglishLocale());
                updateLanguageCardSelection();
            }
        }
        checkStart();
    }

    @SuppressLint("ResourceType")
    private void checkStart() {
        if (restart) {
            Intent intent = new Intent(this.requireContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            this.requireContext().startActivity(intent);
            this.requireActivity().finish();
        }
    }
}