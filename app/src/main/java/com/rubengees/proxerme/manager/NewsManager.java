package com.rubengees.proxerme.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.rubengees.proxerme.entity.News;
import com.rubengees.proxerme.receiver.BootReceiver;
import com.rubengees.proxerme.receiver.NewsReceiver;

import java.util.List;

/**
 * TODO: Describe Class
 *
 * @author Ruben Gees
 */
public class NewsManager {
    public static final int OFFSET_NOT_CALCULABLE = -2;
    public static final int OFFSET_TOO_LARGE = -1;
    public static final int NEWS_ON_PAGE = 15;
    public static final String PREFERENCE_NEW_NEWS = "news_new";
    private static final String PREFERENCE_NEWS_LAST_ID = "news_last_id";
    private static NewsManager INSTANCE;
    private int lastId;
    private int newNews = 0;
    private Context context;

    private NewsManager(@NonNull Context context) {
        this.context = context;

        loadId();
        loadNewNews();
    }

    public static NewsManager getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new NewsManager(context);
        }
        return INSTANCE;
    }

    public static int calculateOffsetFromStart(@NonNull List<News> list, @NonNull News last) {
        return calculateOffsetFromStart(list, last.getId());
    }

    public static int calculateOffsetFromEnd(@NonNull List<News> list, @NonNull News first) {
        return calculateOffsetFromEnd(list, first.getId());
    }

    public static int calculateOffsetFromStart(@NonNull List<News> list, int lastId) {
        if (list.isEmpty()) {
            return OFFSET_NOT_CALCULABLE;
        } else {
            for (int i = 0; i < list.size() || i < NEWS_ON_PAGE; i++) {
                if (lastId == list.get(i).getId()) {
                    return NEWS_ON_PAGE - i - 1;
                }
            }

            return OFFSET_TOO_LARGE;
        }
    }

    public static int calculateOffsetFromEnd(@NonNull List<News> list, int firstId) {
        if (list.isEmpty()) {
            return OFFSET_NOT_CALCULABLE;
        } else {
            int lastSearchableIndex = list.size() - NEWS_ON_PAGE;

            for (int i = list.size() - 1; i >= 0 && i >= lastSearchableIndex; i--) {
                if (firstId == list.get(i).getId()) {
                    return NEWS_ON_PAGE - i - 1;
                }
            }

            return OFFSET_TOO_LARGE;
        }
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int id) {
        lastId = id;

        saveId();
    }

    public int getNewNews() {
        return newNews;
    }

    public void setNewNews(int newNews) {
        this.newNews = newNews;

        saveNewNews();
    }

    public void retrieveNewsLater() {
        if (isNewsRetrievalEnabled()) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NewsReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, alarmIntent);

            ComponentName receiver = new ComponentName(context, BootReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    public void cancelNewsRetrieval() {
        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .cancel(PendingIntent.getBroadcast(context, 0,
                        new Intent(context, NewsReceiver.class), 0));
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public boolean isNewsRetrievalEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getBoolean("pref_notifications", false);
    }

    private void saveId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        preferences.edit().putInt(PREFERENCE_NEWS_LAST_ID, lastId).apply();
    }

    private void loadId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        lastId = preferences.getInt(PREFERENCE_NEWS_LAST_ID, -1);
    }

    private void saveNewNews() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        preferences.edit().putInt(PREFERENCE_NEW_NEWS, newNews).apply();
    }

    private void loadNewNews() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        newNews = preferences.getInt(PREFERENCE_NEW_NEWS, 0);
    }
}
