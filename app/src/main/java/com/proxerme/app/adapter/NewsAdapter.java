package com.proxerme.app.adapter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.proxerme.app.R;
import com.proxerme.app.util.PagingHelper;
import com.proxerme.app.util.TimeUtils;
import com.proxerme.library.connection.UrlHolder;
import com.proxerme.library.entity.News;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.proxerme.app.manager.NewsManager.NEWS_ON_PAGE;

/**
 * An adapter for {@link News}, for usage in a {@link RecyclerView}.
 *
 * @author Ruben Gees
 */
public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private static final String STATE_NEWS_LIST = "news_list";
    private static final String STATE_NEWS_EXTENSION_IDS = "news_extension_ids";
    private static final int ICON_SIZE = 32;
    private static final int ICON_PADDING = 8;
    private static final float ROTATION_HALF = 180f;
    private static final int DESCRIPTION_MAX_LINES = 3;

    private ArrayList<News> list;
    private HashMap<String, Boolean> extensionMap;

    private OnNewsInteractionListener onNewsInteractionListener;

    public NewsAdapter() {
        this.list = new ArrayList<>(NEWS_ON_PAGE * 2);
        extensionMap = new HashMap<>(NEWS_ON_PAGE * 2);
    }

    public NewsAdapter(Collection<News> news) {
        this.list = new ArrayList<>(news.size() * 2);
        extensionMap = new HashMap<>(news.size() * 2);

        this.list.addAll(news);
        notifyItemRangeInserted(0, news.size());
    }

    public NewsAdapter(@NonNull Bundle savedInstanceState) {
        this.list = savedInstanceState.getParcelableArrayList(STATE_NEWS_LIST);
        List<String> ids = savedInstanceState.getStringArrayList(STATE_NEWS_EXTENSION_IDS);
        extensionMap = new HashMap<>(this.list.size() * 2);

        if (ids != null) {
            for (String id : ids) {
                extensionMap.put(id, true);
            }
        }
    }

    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false));
    }

    @Override
    public void onBindViewHolder(NewsAdapter.ViewHolder holder, int position) {
        News item = list.get(position);

        holder.title.setText(item.getSubject());
        holder.description.setText(item.getDescription());
        holder.category.setText(item.getCategoryTitle());
        holder.time.setText(TimeUtils.convertToRelativeReadableTime(holder.time.getContext(),
                item.getTime()));

        holder.expand.setImageDrawable(new IconicsDrawable(holder.expand.getContext())
                .colorRes(R.color.icons_grey).sizeDp(ICON_SIZE).paddingDp(ICON_PADDING)
                .icon(GoogleMaterial.Icon.gmd_keyboard_arrow_down));

        if (extensionMap.containsKey(item.getId())) {
            holder.description.setMaxLines(Integer.MAX_VALUE);
            ViewCompat.setRotationX(holder.expand, ROTATION_HALF);
        } else {
            holder.description.setMaxLines(DESCRIPTION_MAX_LINES);
            ViewCompat.setRotationX(holder.expand, 0f);
        }

        Glide.with(holder.image.getContext()).load(UrlHolder.getNewsImageUrl(item.getId(),
                item.getImageId())).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    /**
     * Inserts the given List of news into this Adapter. A offset to the existing data is calculated,
     * to determine if more data needs to be loaded.
     *
     * @param news The List of News to insert.
     * @return The offset to the existing data. -1 is returned, when the offset is to large and
     * new data needs to be loaded. -2 is returned if the offset could not ne calculated (The
     * internal List or the passed List is empty)
     */
    public int insertAtStart(@NonNull List<News> news) {
        if (!news.isEmpty()) {
            int offset = PagingHelper.calculateOffsetFromStart(news, this.list.get(0),
                    NEWS_ON_PAGE);

            if (offset >= 0) {
                news = news.subList(0, offset);
            }

            this.list.addAll(0, news);
            notifyItemRangeInserted(0, news.size());

            return offset;
        }

        return PagingHelper.OFFSET_NOT_CALCULABLE;
    }

    public int append(@NonNull List<News> news) {
        if (!news.isEmpty()) {
            int offset = PagingHelper.calculateOffsetFromEnd(this.list, news.get(0), NEWS_ON_PAGE);

            if (offset > 0) {
                news = news.subList(offset, news.size());
            }

            this.list.addAll(news);
            notifyItemRangeInserted(this.list.size() - news.size(), news.size());

            return offset;
        }

        return PagingHelper.OFFSET_NOT_CALCULABLE;
    }

    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(STATE_NEWS_LIST, list);
        outState.putStringArrayList(STATE_NEWS_EXTENSION_IDS, new ArrayList<>(extensionMap.keySet()));
    }

    public void setOnNewsInteractionListener(OnNewsInteractionListener onNewsInteractionListener) {
        this.onNewsInteractionListener = onNewsInteractionListener;
    }

    public static abstract class OnNewsInteractionListener {
        public void onNewsClick(@NonNull View v, @NonNull News news) {

        }

        public void onNewsImageClick(@NonNull View v, @NonNull News news) {

        }

        public void onNewsExpanded(@NonNull View v, @NonNull News news) {

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_news_image)
        ImageView image;
        @Bind(R.id.item_news_title)
        TextView title;
        @Bind(R.id.item_news_description)
        TextView description;
        @Bind(R.id.item_news_category)
        TextView category;
        @Bind(R.id.item_news_time)
        TextView time;
        @Bind(R.id.item_news_expand_description)
        ImageButton expand;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_news_content_container)
        void onContentClick(View view) {
            if (onNewsInteractionListener != null) {
                onNewsInteractionListener.onNewsClick(view, list.get(getLayoutPosition()));
            }
        }

        @OnClick(R.id.item_news_image)
        void onImageClick(View view) {
            if (onNewsInteractionListener != null) {
                onNewsInteractionListener.onNewsImageClick(view, list.get(getLayoutPosition()));
            }
        }

        @OnClick(R.id.item_news_expand_description)
        void onExpandClick(View view) {
            News news = list.get(getLayoutPosition());
            String id = news.getId();
            boolean isExpanded = extensionMap.containsKey(id);

            if (isExpanded) {
                extensionMap.remove(id);

                description.setMaxLines(DESCRIPTION_MAX_LINES);
                ViewCompat.animate(view).rotationX(0f);
            } else {
                extensionMap.put(id, true);

                description.setMaxLines(Integer.MAX_VALUE);
                ViewCompat.animate(view).rotationX(ROTATION_HALF);

                if (onNewsInteractionListener != null) {
                    onNewsInteractionListener.onNewsExpanded(view, news);
                }
            }
        }
    }
}
