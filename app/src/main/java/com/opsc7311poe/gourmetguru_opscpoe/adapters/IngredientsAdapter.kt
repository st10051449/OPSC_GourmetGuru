package com.opsc7311poe.gourmetguru_opscpoe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.opsc7311poe.gourmetguru_opscpoe.R

class IngredientsAdapter(private val ingredients: List<String>) :
    RecyclerView.Adapter<IngredientsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ingredientText: TextView = view.findViewById(R.id.tvIngredient) // tvIngredient is a TextView in your ingredient item layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient, parent, false) // Define item_ingredient layout
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ingredientText.text = ingredients[position]
    }

    override fun getItemCount() = ingredients.size
}
