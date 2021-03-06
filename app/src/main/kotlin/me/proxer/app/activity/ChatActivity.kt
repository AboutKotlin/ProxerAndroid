package me.proxer.app.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import me.proxer.app.R
import me.proxer.app.activity.base.MainActivity
import me.proxer.app.entity.chat.LocalConference
import me.proxer.app.fragment.chat.ChatFragment
import me.proxer.app.util.extension.bindView
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.startActivity

class ChatActivity : MainActivity() {

    companion object {
        private const val CONFERENCE_EXTRA = "conference"

        fun navigateTo(context: Activity, conference: LocalConference) {
            context.startActivity<ChatActivity>(CONFERENCE_EXTRA to conference)
        }

        fun getIntent(context: Context, conference: LocalConference): Intent {
            return context.intentFor<ChatActivity>(CONFERENCE_EXTRA to conference)
        }
    }

    var conference: LocalConference
        get() = intent.getParcelableExtra(CONFERENCE_EXTRA)
        set(value) {
            intent.putExtra(CONFERENCE_EXTRA, value)
        }

    private val toolbar: Toolbar by bindView(R.id.toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_default)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = conference.topic
        toolbar.setOnClickListener {
            if (conference.isGroup) {
                ConferenceInfoActivity.navigateTo(this, conference)
            } else {
                ProfileActivity.navigateTo(this, null, conference.topic, conference.image)
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ChatFragment.newInstance())
                    .commitNow()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
