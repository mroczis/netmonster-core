package cz.mroczis.netmonster.sample.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import cz.mroczis.netmonster.sample.R
import kotlinx.android.synthetic.main.view_cell_item_simple.view.*

class CellItemSimple @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_cell_item_simple, this)
    }

    fun bind(
        title: String,
        message: String
    ) {
        this.title.text = title
        this.message.text = message
    }



}