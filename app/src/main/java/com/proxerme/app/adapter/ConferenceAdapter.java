package com.proxerme.app.adapter;

import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.proxerme.app.R;
import com.proxerme.app.util.TimeUtils;
import com.proxerme.library.connection.UrlHolder;
import com.proxerme.library.entity.Conference;
import com.proxerme.library.util.ProxerInfo;

import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * An Adapter for {@link Conference}s, used in a RecyclerView.
 *
 * @author Ruben Gees
 */
public class ConferenceAdapter extends PagingAdapter<Conference, ConferenceAdapter.ViewHolder> {

    private OnConferenceInteractionListener onConferenceInteractionListener;

    public ConferenceAdapter() {
    }

    public ConferenceAdapter(@NonNull Collection<Conference> list) {
        super(list);
    }

    public ConferenceAdapter(@NonNull Bundle savedInstanceState) {
        super(savedInstanceState);
    }

    @Override
    @IntRange(from = 1)
    protected int getItemsOnPage() {
        return ProxerInfo.CONFERENCES_ON_PAGE;
    }

    @Override
    public ConferenceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conference, parent, false));
    }

    @Override
    public int insertAtStart(@NonNull List<Conference> list) {
        int result = super.insertAtStart(list);
        int currentPos = 0;

        while (currentPos < list.size() && !list.get(currentPos).equals(getItemAt(currentPos))) {
            setItemAt(currentPos, list.get(currentPos));

            currentPos++;
        }

        if (currentPos > 0) {
            notifyItemRangeChanged(0, currentPos);
        }

        return result;
    }

    @Override
    public void onBindViewHolder(ConferenceAdapter.ViewHolder holder, int position) {
        Conference item = getItemAt(position);
        int participantAmount = item.getParticipantAmount();
        String participantText = participantAmount + " " +
                (participantAmount == 1 ?
                        holder.participants.getContext().getString(R.string.participant_single) :
                        holder.participants.getContext().getString(R.string.participant_multiple));

        holder.topic.setText(item.getTopic());
        holder.time.setText(TimeUtils.convertToRelativeReadableTime(holder.time.getContext(),
                item.getTime()));
        holder.participants.setText(participantText);

        if (item.isRead()) {
            holder.topic.setCompoundDrawables(null, null, null, null);
        } else {
            holder.topic.setCompoundDrawables(null, null,
                    new IconicsDrawable(holder.image.getContext())
                            .icon(CommunityMaterial.Icon.cmd_message_alert).sizeDp(32).paddingDp(8)
                            .colorRes(R.color.primary), null);
        }

        if (TextUtils.isEmpty(item.getImageId())) {
            IconicsDrawable icon = new IconicsDrawable(holder.image.getContext())
                    .sizeDp(96).paddingDp(16).colorRes(R.color.primary);

            if (item.isConference()) {
                icon.icon(CommunityMaterial.Icon.cmd_account_multiple);
            } else {
                icon.icon(CommunityMaterial.Icon.cmd_account);
            }

            holder.image.setImageDrawable(icon);
        } else {
            Glide.with(holder.image.getContext()).load(UrlHolder.getUserImage(item.getImageId()))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(holder.image);
        }
    }

    public void setOnConferenceInteractionListener(@Nullable OnConferenceInteractionListener
                                                           onConferenceInteractionListener) {
        this.onConferenceInteractionListener = onConferenceInteractionListener;
    }

    public static abstract class OnConferenceInteractionListener {
        public void onConferenceClick(@NonNull View v, @NonNull Conference conference) {

        }

        public void onConferenceImageClick(@NonNull View v, @NonNull Conference conference) {

        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.item_conference_image)
        ImageView image;
        @Bind(R.id.item_conference_title)
        TextView topic;
        @Bind(R.id.item_conference_time)
        TextView time;
        @Bind(R.id.item_conference_participants)
        TextView participants;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.item_conference_image)
        void onImageClick(View v) {
            if (onConferenceInteractionListener != null) {
                onConferenceInteractionListener.onConferenceImageClick(v,
                        getItemAt(getLayoutPosition()));
            }
        }

        @OnClick(R.id.item_conference_content_container)
        void onContentClick(View v) {
            if (onConferenceInteractionListener != null) {
                onConferenceInteractionListener.onConferenceClick(v, getItemAt(getLayoutPosition()));
            }
        }
    }
}
