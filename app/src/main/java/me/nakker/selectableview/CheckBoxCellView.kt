package me.nakker.selectableview

import android.content.Context
import android.content.res.Resources
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.view_check_box_cell.view.*
import me.nakker.selectable_view.SelectableGroup


/**
 * @author nakker
 */
class CheckBoxCellView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), SelectableGroup.Selectable {

    override val compoundButton: CompoundButton

    init {
        LayoutInflater.from(context).inflate(R.layout.view_check_box_cell, this, true)
        compoundButton = checkbox

        isClickable = true
        isFocusable = true
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        foreground = ContextCompat.getDrawable(context, outValue.resourceId)
        minHeight = 72.dp

        context.obtainStyledAttributes(attrs, R.styleable.RadioCellView).apply {
            try {
                title.text = getString(R.styleable.RadioCellView_title)
                description.text = getString(R.styleable.RadioCellView_description)
            } finally {
                recycle()
            }
        }
    }

    private val Int.dp: Int
        get() = this * Resources.getSystem().displayMetrics.density.toInt()
}
