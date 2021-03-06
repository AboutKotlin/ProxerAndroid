package me.proxer.app.adapter.ucp

import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
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
import me.proxer.app.application.GlideRequests
import me.proxer.app.util.extension.bindView
import me.proxer.app.util.extension.toastBelow
import me.proxer.library.entitiy.ucp.UcpTopTenEntry
import me.proxer.library.util.ProxerUrls

/**
 * @author Ruben Gees
 */
class UcpTopTenAdapter(glide: GlideRequests) : BaseGlideAdapter<UcpTopTenEntry>(glide) {

    var callback: UcpToptenAdapterCallback? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = internalList[position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<UcpTopTenEntry> {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_ucp_top_ten_entry, parent, false))
    }

    override fun onViewRecycled(holder: BaseViewHolder<UcpTopTenEntry>) {
        if (holder is ViewHolder) {
            clearImage(holder.image)
        }
    }

    override fun destroy() {
        super.destroy()

        callback = null
    }

    internal inner class ViewHolder(itemView: View) : BaseViewHolder<UcpTopTenEntry>(itemView) {

        internal val title: TextView by bindView(R.id.title)
        internal val image: ImageView by bindView(R.id.image)
        internal val removeButton: ImageButton by bindView(R.id.removeButton)

        init {
            itemView.setOnClickListener { view ->
                withSafeAdapterPosition {
                    callback?.onItemClick(view, internalList[it])
                }
            }

            removeButton.setImageDrawable(IconicsDrawable(removeButton.context)
                    .icon(CommunityMaterial.Icon.cmd_star_off)
                    .colorRes(R.color.icon)
                    .sizeDp(48)
                    .paddingDp(12))

            removeButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    callback?.onRemoveClick(internalList[adapterPosition])
                }
            }

            removeButton.setOnLongClickListener {
                it.toastBelow(R.string.fragment_ucp_topten_delete_hint)

                true
            }
        }

        override fun bind(item: UcpTopTenEntry) {
            ViewCompat.setTransitionName(image, "ucp_top_ten_${item.id}")

            title.text = item.name

            loadImage(image, ProxerUrls.entryImage(item.entryId))
        }
    }

    interface UcpToptenAdapterCallback {
        fun onItemClick(view: View, item: UcpTopTenEntry) {}
        fun onRemoveClick(item: UcpTopTenEntry) {}
    }
}