@file:Suppress("NOTHING_TO_INLINE")

package me.proxer.app.util.extension

import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.support.v4.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.TextView
import me.proxer.app.R
import me.proxer.app.util.ErrorUtils.ErrorAction
import me.proxer.app.util.ErrorUtils.ErrorAction.Companion.ACTION_MESSAGE_DEFAULT
import me.proxer.app.util.ErrorUtils.ErrorAction.Companion.ACTION_MESSAGE_HIDE
import org.jetbrains.anko.applyRecursively

@Suppress("unused")
inline fun Activity.snackbar(root: View, message: CharSequence, duration: Int = LENGTH_LONG,
                             actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                             actionCallback: View.OnClickListener? = null): Snackbar {
    return Snackbar.make(root, message, duration).apply {
        when (actionMessage) {
            ACTION_MESSAGE_DEFAULT -> setAction(R.string.error_action_retry, actionCallback)
            ACTION_MESSAGE_HIDE -> setAction(null, null)
            else -> setAction(actionMessage, actionCallback)
        }
    }.apply {
        show()
    }
}

inline fun Activity.snackbar(root: View, message: Int, duration: Int = LENGTH_LONG,
                             actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                             actionCallback: View.OnClickListener? = null): Snackbar {
    return snackbar(root, getString(message), duration, actionMessage, actionCallback)
}

inline fun Activity.snackbar(root: View, action: ErrorAction, duration: Int = LENGTH_LONG): Snackbar {
    return snackbar(root, action.message, duration, action.buttonMessage, action.buttonAction)
}

inline fun Activity.multilineSnackbar(root: View, message: CharSequence, duration: Int = LENGTH_LONG,
                                      actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                                      actionCallback: View.OnClickListener? = null,
                                      maxLines: Int = 5): Snackbar {
    return snackbar(root, message, duration, actionMessage, actionCallback).apply {
        view.applyRecursively {
            if (it is TextView && it !is Button) {
                it.maxLines = maxLines
            }
        }
    }
}

inline fun Activity.multilineSnackbar(root: View, message: Int, duration: Int = LENGTH_LONG,
                                      actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                                      actionCallback: View.OnClickListener? = null,
                                      maxLines: Int = 5): Snackbar {
    return multilineSnackbar(root, getString(message), duration, actionMessage, actionCallback, maxLines)
}

inline fun Fragment.snackbar(root: View, message: CharSequence, duration: Int = LENGTH_LONG,
                             actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                             actionCallback: View.OnClickListener? = null): Snackbar {
    return activity.snackbar(root, message, duration, actionMessage, actionCallback)
}

inline fun Fragment.snackbar(root: View, message: Int, duration: Int = LENGTH_LONG,
                             actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                             actionCallback: View.OnClickListener? = null): Snackbar {
    return activity.snackbar(root, message, duration, actionMessage, actionCallback)
}

inline fun Fragment.snackbar(root: View, action: ErrorAction, duration: Int = LENGTH_LONG): Snackbar {
    return snackbar(root, action.message, duration, action.buttonMessage, action.buttonAction)
}

inline fun Fragment.multilineSnackbar(root: View, message: CharSequence, duration: Int = LENGTH_LONG,
                                      actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                                      actionCallback: View.OnClickListener? = null,
                                      maxLines: Int = 5): Snackbar {
    return activity.multilineSnackbar(root, message, duration, actionMessage, actionCallback, maxLines)
}

inline fun Fragment.multilineSnackbar(root: View, message: Int, duration: Int = LENGTH_LONG,
                                      actionMessage: Int = ACTION_MESSAGE_DEFAULT,
                                      actionCallback: View.OnClickListener? = null,
                                      maxLines: Int = 5): Snackbar {
    return activity.multilineSnackbar(root, message, duration, actionMessage, actionCallback, maxLines)
}

