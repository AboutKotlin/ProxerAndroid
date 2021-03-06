package me.proxer.app.view.bbcode

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.util.SparseBooleanArray
import android.view.View
import android.view.View.MeasureSpec.*
import android.widget.LinearLayout
import me.proxer.app.view.bbcode.BBProcessor.BBElement
import org.jetbrains.anko.collections.forEachWithIndex
import java.util.*
import kotlin.properties.Delegates


/**
 * @author Ruben Gees
 */
class BBCodeView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var maxHeight = Int.MAX_VALUE
    var text by Delegates.observable("", { _, _, _ ->
        refreshViews()
    })

    var spoilerStates: SparseBooleanArray
        get() = SparseBooleanArray().apply { spoilers.forEachWithIndex { index, it -> put(index, it.expanded) } }
        set(value) = spoilers.forEachWithIndex { index, it -> it.expanded = value.get(index, false) }

    var spoilerStateListener: ((SparseBooleanArray, hasBeenExpanded: Boolean) -> Unit)? = null

    private val spoilers = ArrayList<BBSpoilerView>()

    init {
        orientation = VERTICAL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val hSize = getSize(heightMeasureSpec)
        val hMode = getMode(heightMeasureSpec)

        super.onMeasure(widthMeasureSpec, when (hMode) {
            AT_MOST -> makeMeasureSpec(Math.min(hSize, maxHeight), AT_MOST)
            EXACTLY -> makeMeasureSpec(Math.min(hSize, maxHeight), EXACTLY)
            UNSPECIFIED -> makeMeasureSpec(maxHeight, AT_MOST)
            else -> throw IllegalArgumentException("Illegal measurement mode: $hMode")
        })
    }

    private fun refreshViews() {
        removeAllViews()
        spoilers.clear()

        if (text.isNotBlank()) {
            buildViews(BBProcessor.process(BBTokenizer.tokenize(text))).forEach {
                addView(it)
            }
        }
    }

    private fun buildViews(elements: List<BBElement>): List<View> {
        val result = LinkedList<View>()

        for (element in elements) {
            when (element) {
                is BBElement.BBTextElement -> {
                    val textView = AppCompatTextView(context)

                    textView.text = element.text
                    textView.gravity = element.gravity

                    result.add(textView)
                }
                is BBElement.BBSpoilerElement -> {
                    val spoiler = BBSpoilerView(context).apply {
                        addViews(buildViews(element.children))
                    }

                    spoiler.expansionListener = {
                        spoilerStateListener?.invoke(spoilerStates, it)
                    }

                    spoilers.add(spoiler)
                    result.add(spoiler)
                }
            }
        }

        return result
    }
}