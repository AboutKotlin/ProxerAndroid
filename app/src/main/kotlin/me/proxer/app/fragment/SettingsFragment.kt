package me.proxer.app.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.XpPreferenceFragment
import android.view.View
import me.proxer.app.R
import me.proxer.app.application.MainApplication
import me.proxer.app.dialog.AgeConfirmationDialog
import me.proxer.app.dialog.CleanMangaDialog
import me.proxer.app.helper.PreferenceHelper
import me.proxer.app.helper.PreferenceHelper.AGE_CONFIRMATION
import me.proxer.app.helper.PreferenceHelper.MANGA_CLEAN
import me.proxer.app.helper.PreferenceHelper.NOTIFICATIONS_ACCOUNT
import me.proxer.app.helper.PreferenceHelper.NOTIFICATIONS_CHAT
import me.proxer.app.helper.PreferenceHelper.NOTIFICATIONS_INTERVAL
import me.proxer.app.helper.PreferenceHelper.NOTIFICATIONS_NEWS
import me.proxer.app.helper.PreferenceHelper.THEME
import me.proxer.app.job.ChatJob
import me.proxer.app.job.NotificationsJob
import net.xpece.android.support.preference.TwoStatePreference
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.doAsync

/**
 * @author Ruben Gees
 */
class SettingsFragment : XpPreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment().apply {
                arguments = bundleOf()
            }
        }
    }

    override fun onCreatePreferences2(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)

        findPreference(AGE_CONFIRMATION).setOnPreferenceClickListener {
            it as TwoStatePreference

            if (it.isChecked) {
                it.isChecked = false

                AgeConfirmationDialog.show(activity as AppCompatActivity)
            }

            true
        }

        findPreference(MANGA_CLEAN).setOnPreferenceClickListener {
            CleanMangaDialog.show(activity as AppCompatActivity)

            true
        }

        updateIntervalNotification()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView.isFocusable = false
    }

    override fun onResume() {
        super.onResume()

        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        MainApplication.refWatcher.watch(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            AGE_CONFIRMATION -> {
                if (PreferenceHelper.isAgeRestrictedMediaAllowed(context)) {
                    (findPreference(AGE_CONFIRMATION) as TwoStatePreference).isChecked = true
                }
            }

            THEME -> {
                AppCompatDelegate.setDefaultNightMode(PreferenceHelper.getNightMode(context))

                activity.recreate()
            }

            NOTIFICATIONS_NEWS, NOTIFICATIONS_ACCOUNT -> {
                updateIntervalNotification()

                doAsync {
                    NotificationsJob.scheduleIfPossible(context)
                }
            }

            NOTIFICATIONS_CHAT -> {
                doAsync {
                    ChatJob.scheduleSynchronizationIfPossible(context)
                }
            }

            NOTIFICATIONS_INTERVAL -> {
                doAsync {
                    NotificationsJob.scheduleIfPossible(context)
                    ChatJob.scheduleSynchronizationIfPossible(context)
                }
            }
        }
    }

    private fun updateIntervalNotification() {
        findPreference(NOTIFICATIONS_INTERVAL).isEnabled = PreferenceHelper.areNewsNotificationsEnabled(context) ||
                PreferenceHelper.areAccountNotificationsEnabled(context)
    }
}
