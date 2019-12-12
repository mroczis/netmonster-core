package cz.mroczis.netmonster.sample

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cz.mroczis.netmonster.core.model.cell.ICell

class MainAdapter : RecyclerView.Adapter<MainVH>() {

    var data: List<ICell> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MainVH.create(parent)
    override fun onBindViewHolder(holder: MainVH, position: Int) = holder.bind(data[position])
    override fun getItemCount() = data.size

}