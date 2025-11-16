package com.example.recipegroceryhelper

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class Recipe(val id: String, val name: String, val description: String, val imageUrl: String)

class RecipeAdapter(private var recipes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)
        val recipeName: TextView = itemView.findViewById(R.id.recipe_name)
        val recipeDescription: TextView = itemView.findViewById(R.id.recipe_description)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeName.text = recipe.name
        holder.recipeDescription.text = "" // Remove ingredients from the main list

        Glide.with(holder.itemView.context)
            .load(recipe.imageUrl)
            .into(holder.recipeImage)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, RecipeDetailActivity::class.java)
            intent.putExtra("RECIPE_ID", recipe.id)
            intent.putExtra("RECIPE_NAME", recipe.name)
            intent.putExtra("RECIPE_INGREDIENTS", recipe.description)
            intent.putExtra("RECIPE_IMAGE_URL", recipe.imageUrl)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = recipes.size

    fun updateData(newRecipes: List<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged()
    }
}