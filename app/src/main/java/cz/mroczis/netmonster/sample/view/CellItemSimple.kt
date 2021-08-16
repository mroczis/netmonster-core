package cz.mroczis.netmonster.sample.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.updatePadding
import cz.mroczis.netmonster.sample.databinding.ViewCellItemSimpleBinding

class CellItemSimple @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewCellItemSimpleBinding.inflate(LayoutInflater.from(context), this)

    init {
        val dp2 = (context.resources.displayMetrics.density * 2).toInt()
        updatePadding(top = dp2, bottom = dp2)
    }

    fun bind(
        title: String,
        message: String
    ) = with(binding) {
        this.title.text = title
        this.message.text = message
    }



}