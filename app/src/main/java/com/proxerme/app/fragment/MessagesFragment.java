package com.proxerme.app.fragment;

import android.os.Bundle;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.proxerme.app.R;
import com.proxerme.app.adapter.MessageAdapter;
import com.proxerme.app.manager.NotificationRetrievalManager;
import com.proxerme.app.manager.StorageManager;
import com.proxerme.app.manager.UserManager;
import com.proxerme.library.connection.ProxerConnection;
import com.proxerme.library.connection.ProxerTag;
import com.proxerme.library.entity.Message;
import com.proxerme.library.event.error.MessagesErrorEvent;
import com.proxerme.library.event.success.LoginEvent;
import com.proxerme.library.event.success.LogoutEvent;
import com.proxerme.library.event.success.MessagesEvent;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
public class MessagesFragment extends LoginPollingPagingFragment<Message, MessageAdapter,
        MessagesEvent, MessagesErrorEvent> {

    private static final String ARGUMENT_CONFERENCE_ID = "conference_id";

    @Bind(R.id.fragment_messages_input)
    TextInputEditText input;

    private String conferenceId;

    public static MessagesFragment newInstance(@NonNull String conferenceId) {
        MessagesFragment result = new MessagesFragment();
        Bundle arguments = new Bundle();

        arguments.putString(ARGUMENT_CONFERENCE_ID, conferenceId);
        result.setArguments(arguments);

        return result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conferenceId = getArguments().getString(ARGUMENT_CONFERENCE_ID);
    }

    @Override
    protected MessageAdapter createAdapter(Bundle savedInstanceState) {
        UserManager userManager = UserManager.getInstance();

        if (savedInstanceState == null) {
            return new MessageAdapter(userManager.isLoggedIn() ? userManager.getUser() : null);
        } else {
            return new MessageAdapter(savedInstanceState,
                    userManager.isLoggedIn() ? userManager.getUser() : null);
        }
    }

    @Override
    protected void load(@IntRange(from = 0) int page, boolean insert) {
        ProxerConnection.loadMessages(conferenceId, page).execute();
    }

    @Override
    public void onLoad(@NonNull MessagesEvent result) {
        super.onLoad(result);

        StorageManager.setNewMessages(0);
        StorageManager.resetMessagesInterval();

        if (getContext() != null) {
            NotificationRetrievalManager.retrieveMessagesLater(getContext());
        }
    }

    @Override
    public void onLoadError(@NonNull MessagesErrorEvent errorEvent) {
        super.onLoadError(errorEvent);

        //We need to override every EventBus handler which uses generics with concrete classes as
        //parameters.
    }

    @Override
    public void onLogin(LoginEvent event) {
        adapter.setUser(event.getItem());

        super.onLogin(event);
    }

    @Override
    public void onLogout(LogoutEvent event) {
        adapter.setUser(null);

        super.onLogout(event);
    }

    @Override
    protected void cancelRequest() {
        ProxerConnection.cancel(ProxerTag.MESSAGES);
    }

    @Override
    protected void configLayoutManager(@NonNull StaggeredGridLayoutManager layoutManager) {
        super.configLayoutManager(layoutManager);

        layoutManager.setReverseLayout(true);
        layoutManager.setSpanCount(1);
    }

    @Override
    protected int getFirstPage() {
        return 0;
    }

    @Override
    protected View inflateLayout(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @OnClick(R.id.fragment_messages_send)
    public void sendMessage() {
        //TODO
    }
}
