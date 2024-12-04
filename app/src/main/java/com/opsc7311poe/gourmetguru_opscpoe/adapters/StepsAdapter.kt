package com.opsc7311poe.gourmetguru_opscpoe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.opsc7311poe.gourmetguru_opscpoe.R

class StepsAdapter(private val steps: List<String>) :
    RecyclerView.Adapter<StepsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stepText: TextView = view.findViewById(R.id.tvStep) // tvStep is a TextView in your step item layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_step, parent, false) // Define item_step layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.stepText.text = steps[position]
    }

    override fun getItemCount() = steps.size
}
