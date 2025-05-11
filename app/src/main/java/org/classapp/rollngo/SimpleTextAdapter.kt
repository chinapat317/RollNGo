package org.classapp.rollngo

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CafeAdapter(
    private val items: List<Cafe>,
    private val onItemClick: (Cafe) -> Unit
) : RecyclerView.Adapter<CafeAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cafe = items[position]
        holder.textView.text = cafe.display
        holder.textView.setOnClickListener { onItemClick(cafe) }
    }

    override fun getItemCount(): Int = items.size
}