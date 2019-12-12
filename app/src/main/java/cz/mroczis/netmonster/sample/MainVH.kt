package cz.mroczis.netmonster.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.mroczis.netmonster.core.model.cell.*
import kotlinx.android.synthetic.main.view_cell.view.*

class MainVH(view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        fun create(parent: ViewGroup): MainVH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.view_cell, parent, false)
            return MainVH(v)
        }
    }

    fun bind(cell: ICell) = itemView.root.bind(cell)

}