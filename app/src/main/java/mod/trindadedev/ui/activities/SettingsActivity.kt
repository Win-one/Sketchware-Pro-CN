package mod.trindadedev.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import com.besome.sketch.lib.base.BaseAppCompatActivity
import mod.trindadedev.ui.fragments.events.EventsManagerFragment
import mod.trindadedev.ui.fragments.settings.appearance.SettingsAppearanceFragment
import mod.trindadedev.ui.fragments.settings.language.SettingsLanguageFragment
import pro.sketchware.databinding.ActivitySettingsBinding

class SettingsActivity : BaseAppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    companion object {
        const val FRAGMENT_TAG_EXTRA = "fragment_tag"
        const val SETTINGS_APPEARANCE_FRAGMENT = "settings_appearance"
        const val EVENTS_MANAGER_FRAGMENT = "events_manager"
        const val SETTINGS_LANGUAGE_FRAGMENT = "settings_language"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragment = when (val fragmentTag = intent.getStringExtra(FRAGMENT_TAG_EXTRA)) {
            SETTINGS_APPEARANCE_FRAGMENT -> SettingsAppearanceFragment()
            EVENTS_MANAGER_FRAGMENT -> EventsManagerFragment()
            SETTINGS_LANGUAGE_FRAGMENT -> SettingsLanguageFragment()
            else -> throw IllegalArgumentException("Unknown fragment tag: $fragmentTag")
        }
        openFragment(fragment)
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.settingsFragmentContainer.id, fragment)
            .commit()
    }
}
