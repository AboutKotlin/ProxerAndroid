package me.proxer.app.fragment.ucp

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.rubengees.ktask.android.AndroidLifecycleTask
import com.rubengees.ktask.android.bindToLifecycle
import com.rubengees.ktask.util.TaskBuilder
import me.proxer.app.R
import me.proxer.app.activity.MediaActivity
import me.proxer.app.activity.base.MainActivity
import me.proxer.app.adapter.ucp.UcpTopTenAdapter
import me.proxer.app.application.GlideApp
import me.proxer.app.application.MainApplication.Companion.api
import me.proxer.app.fragment.base.LoadingFragment
import me.proxer.app.task.asyncProxerTask
import me.proxer.app.util.DeviceUtils
import me.proxer.app.util.ErrorUtils
import me.proxer.app.util.Validators
import me.proxer.app.util.extension.bindView
import me.proxer.app.util.extension.multilineSnackbar
import me.proxer.library.api.ProxerCall
import me.proxer.library.entitiy.ucp.UcpTopTenEntry
import me.proxer.library.enums.Category
import org.jetbrains.anko.bundleOf
import org.jetbrains.anko.find

/**
 * @author Ruben Gees
 */
class UcpTopTenFragment : LoadingFragment<ProxerCall<List<UcpTopTenEntry>>, List<UcpTopTenEntry>>() {

    companion object {
        fun newInstance(): UcpTopTenFragment {
            return UcpTopTenFragment().apply {
                arguments = bundleOf()
            }
        }
    }

    override val isLoginRequired = true

    private val animeAdapter by lazy { UcpTopTenAdapter(GlideApp.with(this)) }
    private val mangaAdapter by lazy { UcpTopTenAdapter(GlideApp.with(this)) }

    private lateinit var removalTask: AndroidLifecycleTask<ProxerCall<Void?>, Void?>
    private val removalQueue = LinkedHashSet<UcpTopTenEntry>()

    private val animeContainer: ViewGroup by bindView(R.id.animeContainer)
    private val mangaContainer: ViewGroup by bindView(R.id.mangaContainer)
    private val animeList: RecyclerView by bindView(R.id.animeList)
    private val mangaList: RecyclerView by bindView(R.id.mangaList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        removalTask = TaskBuilder.asyncProxerTask<Void?>()
                .validateBefore {
                    Validators.validateLogin()
                }
                .bindToLifecycle(this, "${javaClass}_removal_task")
                .onSuccess {
                    removalQueue.first().let {
                        when (it.category) {
                            Category.ANIME -> animeAdapter.remove(it)
                            Category.MANGA -> mangaAdapter.remove(it)
                        }

                        removalQueue.remove(it)
                    }

                    onSuccess(animeAdapter.list.plus(mangaAdapter.list))
                    removeEntriesFromQueue()
                }
                .onError {
                    removalQueue.clear()

                    ErrorUtils.handle(activity as MainActivity, it).let {
                        multilineSnackbar(root, getString(R.string.error_topten_entry_removal, getString(it.message)),
                                Snackbar.LENGTH_LONG, it.buttonMessage, it.buttonAction)
                    }
                }.build()
    }

    override fun onDestroy() {
        animeAdapter.destroy()
        mangaAdapter.destroy()

        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_top_ten, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spanCount = DeviceUtils.calculateSpanAmount(activity) + 1

        animeAdapter.callback = UcpTopTenCallback()
        mangaAdapter.callback = UcpTopTenCallback()

        animeList.isNestedScrollingEnabled = false
        animeList.layoutManager = GridLayoutManager(context, spanCount)
        animeList.adapter = animeAdapter

        mangaList.isNestedScrollingEnabled = false
        mangaList.layoutManager = GridLayoutManager(context, spanCount)
        mangaList.adapter = mangaAdapter
    }

    override fun onSuccess(result: List<UcpTopTenEntry>) {
        val animeList = result.filter { it.category == Category.ANIME }
        val mangaList = result.filter { it.category == Category.MANGA }

        animeAdapter.replace(animeList)
        mangaAdapter.replace(mangaList)

        when (animeAdapter.isEmpty()) {
            true -> animeContainer.visibility = View.GONE
            false -> animeContainer.visibility = View.VISIBLE
        }

        when (mangaAdapter.isEmpty()) {
            true -> mangaContainer.visibility = View.GONE
            false -> mangaContainer.visibility = View.VISIBLE
        }

        super.onSuccess(result)
    }

    override fun showContent() {
        super.showContent()

        if (animeAdapter.isEmpty() && mangaAdapter.isEmpty()) {
            showError(R.string.error_no_data_top_ten, ErrorUtils.ErrorAction.ACTION_MESSAGE_HIDE)
        }
    }

    override fun constructTask() = TaskBuilder.asyncProxerTask<List<UcpTopTenEntry>>().build()
    override fun constructInput() = api.ucp().topTen().build()

    private fun removeEntriesFromQueue() {
        if (removalQueue.isNotEmpty()) {
            removalTask.execute(api.ucp().deleteFavorite(removalQueue.first().id).build())
        }
    }

    private inner class UcpTopTenCallback : UcpTopTenAdapter.UcpToptenAdapterCallback {
        override fun onItemClick(view: View, item: UcpTopTenEntry) {
            val imageView = view.find<ImageView>(R.id.image)

            MediaActivity.navigateTo(activity, item.entryId, item.name, item.category,
                    if (imageView.drawable != null) imageView else null)
        }

        override fun onRemoveClick(item: UcpTopTenEntry) {
            removalQueue.add(item)

            removeEntriesFromQueue()
        }
    }
}
