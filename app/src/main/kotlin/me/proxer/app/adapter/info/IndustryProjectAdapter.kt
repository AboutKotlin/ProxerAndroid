package me.proxer.app.adapter.info

import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import me.proxer.app.R
import me.proxer.app.adapter.base.BaseGlideAdapter
import me.proxer.app.application.GlideRequests
import me.proxer.app.util.extension.bindView
import me.proxer.app.util.extension.toAppString
import me.proxer.library.entitiy.list.IndustryProject
import me.proxer.library.util.ProxerUrls

/**
 * @author Ruben Gees
 */
class IndustryProjectAdapter(glide: GlideRequests) : BaseGlideAdapter<IndustryProject>(glide) {

    var callback: IndustryProjectAdapterCallback? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<IndustryProject> {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false))
    }

    override fun onViewRecycled(holder: BaseViewHolder<IndustryProject>) {
        if (holder is ViewHolder) {
            clearImage(holder.image)
        }
    }

    override fun destroy() {
        super.destroy()

        callback = null
    }

    internal inner class ViewHolder(itemView: View) : BaseViewHolder<IndustryProject>(itemView) {

        internal val title: TextView by bindView(R.id.title)
        internal val medium: TextView by bindView(R.id.medium)
        internal val image: ImageView by bindView(R.id.image)
        internal val ratingContainer: ViewGroup by bindView(R.id.ratingContainer)
        internal val rating: RatingBar by bindView(R.id.rating)
        internal val status: TextView by bindView(R.id.status)

        init {
            itemView.setOnClickListener { view ->
                withSafeAdapterPosition {
                    callback?.onProjectClick(view, internalList[it])
                }
            }
        }

        override fun bind(item: IndustryProject) {
            ViewCompat.setTransitionName(image, "industry_project_${item.id}")

            title.text = item.name
            medium.text = item.medium.toAppString(medium.context)
            status.text = item.state.toAppString(status.context)

            if (item.rating > 0) {
                ratingContainer.visibility = View.VISIBLE
                rating.rating = item.rating / 2.0f
            } else {
                ratingContainer.visibility = View.GONE
            }

            loadImage(image, ProxerUrls.entryImage(item.id))
        }
    }

    interface IndustryProjectAdapterCallback {
        fun onProjectClick(view: View, item: IndustryProject) {}
    }
}