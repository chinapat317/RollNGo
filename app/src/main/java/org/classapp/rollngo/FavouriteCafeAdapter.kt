package org.classapp.rollngo

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FavoriteCafeAdapter(
    private val cafes: List<Cafe>,
    private val onClick: (Cafe) -> Unit
) : RecyclerView.Adapter<FavoriteCafeAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cafe = cafes[position]
        holder.textView.text = cafe.name
        holder.textView.setOnClickListener { onClick(cafe) }
    }

    override fun getItemCount(): Int = cafes.size
}
