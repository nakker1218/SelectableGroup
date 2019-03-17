package me.nakker.selectable_view

import android.content.Context
import android.support.annotation.IdRes
import android.support.constraint.ConstraintHelper
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.widget.CompoundButton

/**
 * @author nakker
 */
class RadioGroup @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintHelper(context, attrs, defStyleAttr) {
    private val compoundButtonList: MutableList<CompoundButton> = mutableListOf()

    @IdRes
    private var selectedViewId: Int = -1
    private var onCheckedChangeListener: OnCheckedChangeListener? = null

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RadioGroup).apply {
            try {
                getResourceId(R.styleable.RadioGroup_initial_select_view_id, -1)
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
            val id = mIds[i]
            val view = container?.getViewById(id) ?: return
            val compoundButton = when (view) {
                is CompoundButton -> view
                is Selectable -> view.compoundButton
                else -> return
            }

            compoundButton.apply {
                compoundButtonList.add(this)
                isChecked = (id == selectedViewId)
                setOnClickListener { clickedButton ->
                    this@RadioGroup.selectedViewId = id
                    compoundButtonList.forEach {
                        it.isChecked = (it.id == selectedViewId)
                    }
                    onCheckedChangeListener?.onCheckedChanged(clickedButton.id)
                }
            }
        }
    }

    override fun updatePostLayout(container: ConstraintLayout?) {
        val params = this.layoutParams as ConstraintLayout.LayoutParams
        params.width = 0
        params.height = 0
    }

    fun setOnCheckedChangeListener(onCheckedChanged: (checkedId: Int) -> Unit) {
        setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(checkedId: Int) {
                onCheckedChanged.invoke(checkedId)
            }
        })
    }

    fun setOnCheckedChangeListener(onCheckedChangeListener: OnCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener
    }

    interface OnCheckedChangeListener {
        fun onCheckedChanged(@IdRes checkedId: Int)
    }

    interface Selectable {
        val compoundButton: CompoundButton
    }
}
