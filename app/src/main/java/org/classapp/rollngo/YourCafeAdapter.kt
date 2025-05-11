package org.classapp.rollngo

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class YourCafeAdapter(
    private val cafes: List<Pair<String, String>>, // Pair<cafeKey, cafeName>
    private val onDelete: (String) -> Unit
) : RecyclerView.Adapter<YourCafeAdapter.ViewHolder>() {

    class ViewHolder(val layout: LinearLayout) : RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val nameView = TextView(context).apply {
                id = android.R.id.text1
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val deleteBtn = Button(context).apply {
                id = android.R.id.button1
                text = "🗑"
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            addView(nameView)
            addView(deleteBtn)
        }
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (key, name) = cafes[position]
        holder.layout.findViewById<TextView>(android.R.id.text1).text = name
        holder.layout.findViewById<Button>(android.R.id.button1).setOnClickListener {
            onDelete(key)
        }
    }

    override fun getItemCount(): Int = cafes.size
}
