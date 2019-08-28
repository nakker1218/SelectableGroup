package me.nakker.selectable_view

import android.content.Context
import android.support.annotation.IdRes
import android.support.constraint.ConstraintHelper
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton

/**
 * @author nakker
 */
class SelectableGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        ConstraintHelper(context, attrs, defStyleAttr) {
    private val selectableViewMap: MutableMap<CompoundButton, View> = mutableMapOf()
    private val maxSelections: Int
    private val selectionMode: SelectionMode

    @IdRes
    private var selectViewIds: MutableList<Int> = mutableListOf()
    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SelectableGroup).apply {
            try {
                maxSelections = getInteger(R.styleable.SelectableGroup_max_selections, 1)

                val rawSelectViewIds = getString(R.styleable.SelectableGroup_initial_select_view_ids)
                        ?.split(",")
                        ?.map { it.trim() }

                rawSelectViewIds?.map {
                    if (maxSelections < rawSelectViewIds.size) throw IllegalArgumentException("The initial select view count must not be over the Max Selection.")
                    return@map context.resources.getIdentifier(it, "id", context.packageName)
                }?.forEach { id -> selectViewIds.add(id) }

                val selectionModeValue = getInteger(R.styleable.SelectableGroup_selection_mode, 1)
                selectionMode = SelectionMode.from(selectionModeValue)
            } finally {
                recycle()
            }
        }
    }

    override fun init(attrs: AttributeSet?) {
        super.init(attrs)
        this.mUseViewMeasure = false
    }

    override fun updatePreLayout(container: ConstraintLayout?) {
        for (i in 0..mCount) {
            val rawView = container?.getViewById(mIds[i]) ?: return
            val child = rawView

            val selectableView = when (child) {
                is CompoundButton -> Pair(rawView, child)
                is Selectable -> Pair(rawView, child.compoundButton)
                else -> return
            }

            selectableView.apply {
                val (view, compoundButton) = this
                selectableViewMap[compoundButton] = view
                reselection()

                arrayOf(view, compoundButton).forEach {
                    it.setOnClickListener { clickedView ->
                        val clickedView = selectableViewMap[clickedView] ?: it

                        selectViewIds.forEach { id ->
                            if (clickedView.id == id) {
                                selectViewIds.minusAssign(id)
                                reselection()
                                onCheckedChangeListener?.onCheckedChanged(selectViewIds)
                                return@setOnClickListener
                            }
                        }

                        if (maxSelections <= selectViewIds.size) {
                            when (selectionMode) {
                                SelectionMode.RADIO -> {
                                    selectViewIds = selectViewIds.drop(1).toMutableList()
                                    reselection()
                                }
                                SelectionMode.SELECTABLE -> {
                                    reselection()
                                    return@setOnClickListener
                                }
                            }
                        }

                        selectViewIds.add(clickedView.id)
                        reselection()
                        onCheckedChangeListener?.onCheckedChanged(selectViewIds)
                    }
                }
            }
        }
    }

    override fun updatePostLayout(container: ConstraintLayout?) {
        val params = this.layoutParams as ConstraintLayout.LayoutParams
        params.width = 0
        params.height = 0
    }

    fun setOnCheckedChangeListener(onCheckedChanged: (checkedIds: List<Int>) -> Unit) {
        setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(checkedIds: List<Int>) {
                onCheckedChanged.invoke(checkedIds)
            }
        })
    }

    fun setOnCheckedChangeListener(onCheckedChangeListener: OnCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener
    }

    private fun reselection() {
        selectableViewMap.forEach compoundButton@{ (compoundButton, view) ->
            selectViewIds.take(maxSelections).forEach {
                if (view.id != it) return@forEach
                compoundButton.isChecked = true
                return@compoundButton
            }
            compoundButton.isChecked = false
        }
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(@IdRes checkedIds: List<Int>)
    }

    interface Selectable {
        val compoundButton: CompoundButton
    }

    private enum class SelectionMode(private val value: Int) {
        RADIO(0),
        SELECTABLE(1);

        companion object {
            fun from(value: Int): SelectionMode {
                return when (value) {
                    RADIO.value -> RADIO
                    SELECTABLE.value -> SELECTABLE
                    else -> throw IllegalArgumentException("Value must be success value.")
                }
            }
        }
    }
}
