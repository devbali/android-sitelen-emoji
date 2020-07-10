package com.ourdhi.sitelenemoji

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.tokiponasitelenemojikeyboard.R

/**
 * A simple [Fragment] subclass.
 */

class SettingsFragment : PreferenceFragmentCompat () {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        val myPref =
            findPreference("enable") as Preference?
        myPref!!.setOnPreferenceClickListener(object : Preference.OnPreferenceClickListener {
            override fun onPreferenceClick(preference: Preference?): Boolean {
                startActivityForResult(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS),0)
                return true
            }
        })
    }
}
