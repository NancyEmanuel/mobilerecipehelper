package com.example.recipegroceryhelper

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Setup Toolbar with back button
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Get data from the intent
        val recipeName = intent.getStringExtra("RECIPE_NAME")
        val recipeIngredients = intent.getStringExtra("RECIPE_INGREDIENTS")
        val recipeImageUrl = intent.getStringExtra("RECIPE_IMAGE_URL")

        // Find views in the layout
        val nameTextView = findViewById<TextView>(R.id.detail_recipe_name)
        val ingredientsTextView = findViewById<TextView>(R.id.detail_recipe_ingredients)
        val imageView = findViewById<ImageView>(R.id.detail_recipe_image)

        // Set the main data
        nameTextView.text = recipeName
        title = recipeName

        // Sort and display the ingredients
        val ingredientsList = recipeIngredients?.split(',')?.map { it.trim() } ?: emptyList()
        val harderIngredientsSet = setOf("Pepperoni", "Spaghetti", "Meatballs", "Marinara", "Tortilla", "Ground Beef", "Bun", "Beef Patty", "Dumpling", "Broth", "Feta Cheese")

        val (hard, simple) = ingredientsList.partition { ingredient ->
            harderIngredientsSet.any { hardIngredient -> ingredient.contains(hardIngredient, ignoreCase = true) }
        }

        val simpleText = if (simple.isNotEmpty()) "Simple Ingredients:\n" + simple.joinToString("\n") else ""
        val hardText = if (hard.isNotEmpty()) "Harder Ingredients:\n" + hard.joinToString("\n") else ""

        // Join the two sections with a double newline only if both exist
        val sortedIngredientsText = listOf(simpleText, hardText).filter { it.isNotEmpty() }.joinToString("\n\n")

        ingredientsTextView.text = sortedIngredientsText

        // Load the image
        Glide.with(this)
            .load(recipeImageUrl)
            .into(imageView)
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle the back button click
        onBackPressed()
        return true
    }
}