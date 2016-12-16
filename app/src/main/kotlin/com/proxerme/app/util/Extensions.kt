package com.proxerme.app.util

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.proxerme.app.R
import com.proxerme.app.activity.WebViewActivity
import com.proxerme.app.adapter.framework.PagingAdapter
import me.zhanghai.android.customtabshelper.CustomTabsHelperFragment
import okhttp3.HttpUrl

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */

val Context.inputMethodManager: InputMethodManager
    get() = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.alarmManager: AlarmManager
    get() = getSystemService(Context.ALARM_SERVICE) as AlarmManager

val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

fun CustomTabsHelperFragment.openHttpPage(activity: Activity, url: HttpUrl) {
    val customTabsIntent = CustomTabsIntent.Builder(session)
            .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
            .setSecondaryToolbarColor(ContextCompat.getColor(context,
                    R.color.colorPrimaryDark))
            .addDefaultShareMenuItem()
            .enableUrlBarHiding()
            .setShowTitle(true)
            .build()

    CustomTabsHelperFragment.open(activity, customTabsIntent, Uri.parse(url.toString()),
            { activity, uri ->
                WebViewActivity.navigateTo(activity, uri.toString())
            })
}

fun <T> PagingAdapter<T>.insertAndScrollUpIfNecessary(layoutManager: RecyclerView.LayoutManager,
                                                      recyclerView: RecyclerView,
                                                      items: Array<T>) {
    val isFirstDifferent = this.items.firstOrNull() != items.firstOrNull()
    val wasAtTop = when (layoutManager) {
        is LinearLayoutManager -> layoutManager.findFirstVisibleItemPosition() == 0
        is StaggeredGridLayoutManager -> {
            layoutManager.findFirstVisibleItemPositions(null).contains(0)
        }
        else -> throw IllegalArgumentException("Unknown LayoutManager: $layoutManager")
    }

    insert(items)

    if (wasAtTop && isFirstDifferent) {
        recyclerView.post {
            recyclerView.smoothScrollToPosition(0)
        }
    }
}