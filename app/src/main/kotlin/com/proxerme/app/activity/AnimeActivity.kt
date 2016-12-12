package com.proxerme.app.activity

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.proxerme.app.R
import com.proxerme.app.fragment.anime.AnimeFragment
import com.proxerme.app.util.bindView
import org.jetbrains.anko.intentFor

class AnimeActivity : MainActivity() {

    companion object {
        private const val EXTRA_ID = "extra_id"
        private const val EXTRA_EPISODE = "extra_episode"
        private const val EXTRA_TOTAL_EPISODES = "extra_total_episodes"
        private const val EXTRA_LANGUAGE = "extra_language"

        fun navigateTo(context: Activity, id: String, episode: Int, totalEpisodes: Int,
                       language: String) {
            context.startActivity(context.intentFor<AnimeActivity>(
                    EXTRA_ID to id,
                    EXTRA_EPISODE to episode,
                    EXTRA_TOTAL_EPISODES to totalEpisodes,
                    EXTRA_LANGUAGE to language
            ))
        }
    }

    private val id: String
        get() = intent.getStringExtra(EXTRA_ID)

    private val episode: Int
        get() = intent.getIntExtra(EXTRA_EPISODE, 1)

    private val totalEpisodes: Int
        get() = intent.getIntExtra(EXTRA_TOTAL_EPISODES, 1)

    private val language: String
        get() = intent.getStringExtra(EXTRA_LANGUAGE)

    private val toolbar: Toolbar by bindView(R.id.toolbar)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_default)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.activity_anime_title, episode)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.container,
                    AnimeFragment.newInstance(id, episode, totalEpisodes, language)).commitNow()
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

    fun updateEpisode(episode: Int) {
        intent.putExtra(EXTRA_EPISODE, episode)

        title = getString(R.string.activity_anime_title, episode)
    }
}
