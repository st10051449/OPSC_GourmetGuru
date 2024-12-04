package com.opsc7311poe.gourmetguru_opscpoe.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.opsc7311poe.gourmetguru_opscpoe.LebaneseFragment
import com.opsc7311poe.gourmetguru_opscpoe.R
import com.opsc7311poe.gourmetguru_opscpoe.models.ApiRecipeData


class RecipeAdapter(private var recipes: List<ApiRecipeData>, private val cuisineName: String,  private val onItemClick: (ApiRecipeData) -> Unit) : RecyclerView.Adapter<RecipeAdapter.ViewHolder>() {


    fun updateData(newRecipes: List<ApiRecipeData>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeName.text = recipe.name

        // Load the image if available
        recipe.imageUrl?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .into(holder.recipeImage)
        }

        // Set up click listener for the passed onItemClick function
        holder.itemView.setOnClickListener {
            onItemClick(recipe)
        }
    }

    override fun getItemCount(): Int {
        return recipes.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeName: TextView = itemView.findViewById(R.id.tvRecipeName)
        val recipeImage: ImageView = itemView.findViewById(R.id.ivRecipeImage)
    }


}
