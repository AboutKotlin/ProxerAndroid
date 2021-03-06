package me.proxer.app.adapter.ucp

import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.iconics.IconicsDrawable
import me.proxer.app.R
import me.proxer.app.adapter.base.BaseGlideAdapter
import me.proxer.app.application.GlideRequest
import me.proxer.app.application.GlideRequests
import me.proxer.app.util.extension.*
import me.proxer.app.util.view.GlideGrayscaleTransformation
import me.proxer.library.entitiy.ucp.Bookmark
import me.proxer.library.util.ProxerUrls

/**
 * @author Ruben Gees
 */
class BookmarkAdapter(glide: GlideRequests) : BaseGlideAdapter<Bookmark>(glide) {

    companion object {
        private val grayscaleTransformationAdjustment = { it: GlideRequest<Drawable> ->
            it.transform(GlideGrayscaleTransformation())
        }
    }

    var callback: BookmarkAdapterCallback? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = internalList[position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<Bookmark> {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false))
    }

    override fun onViewRecycled(holder: BaseViewHolder<Bookmark>) {
        if (holder is ViewHolder) {
            clearImage(holder.image)
        }
    }

    override fun destroy() {
        super.destroy()

        callback = null
    }

    override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
        return oldItem.entryId == newItem.entryId
    }

    override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
        return oldItem.id == newItem.id
    }

    internal inner class ViewHolder(itemView: View) : BaseViewHolder<Bookmark>(itemView) {

        internal val title: TextView by bindView(R.id.title)
        internal val medium: TextView by bindView(R.id.medium)
        internal val image: ImageView by bindView(R.id.image)
        internal val episode: TextView by bindView(R.id.episode)
        internal val language: ImageView by bindView(R.id.language)
        internal val remove: ImageButton by bindView(R.id.remove)

        init {
            itemView.setOnClickListener {
                withSafeAdapterPosition {
                    callback?.onBookmarkClick(internalList[it])
                }
            }

            itemView.setOnLongClickListener { view ->
                withSafeAdapterPosition {
                    callback?.onBookmarkLongClick(view, internalList[it])
                }

                true
            }

            remove.setImageDrawable(IconicsDrawable(remove.context)
                    .icon(CommunityMaterial.Icon.cmd_bookmark_remove)
                    .colorRes(R.color.icon)
                    .sizeDp(48)
                    .paddingDp(12))

            remove.setOnClickListener {
                withSafeAdapterPosition {
                    callback?.onBookmarkRemoval(internalList[it])
                }
            }

            remove.setOnLongClickListener {
                it.toastBelow(R.string.fragment_bookmarks_delete_hint)

                true
            }
        }

        override fun bind(item: Bookmark) {
            ViewCompat.setTransitionName(image, "bookmark_${item.id}")

            val availabilityIndicator = AppCompatResources.getDrawable(episode.context, when (item.isAvailable) {
                true -> R.drawable.ic_circle_green
                false -> R.drawable.ic_circle_red
            })

            title.text = item.name
            medium.text = item.medium.toAppString(medium.context)
            episode.text = item.chapterName ?: item.category.toEpisodeAppString(episode.context, item.episode)

            episode.setCompoundDrawablesWithIntrinsicBounds(null, null, availabilityIndicator, null)
            language.setImageDrawable(item.language.toGeneralLanguage().toAppDrawable(language.context))

            loadImage(image, ProxerUrls.entryImage(item.entryId),
                    if (item.isAvailable) null else grayscaleTransformationAdjustment)
        }
    }

    interface BookmarkAdapterCallback {
        fun onBookmarkClick(item: Bookmark) {}
        fun onBookmarkLongClick(view: View, item: Bookmark) {}
        fun onBookmarkRemoval(item: Bookmark) {}
    }
}
