package com.example.recipegroceryhelper

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipeDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        // Set up the top toolbar as an action bar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Enable the back arrow in the top-left corner
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Retrieve the recipe details passed from the previous screen
        val recipeId = intent.getStringExtra("RECIPE_ID")
        val recipeName = intent.getStringExtra("RECIPE_NAME")
        val recipeImageUrl = intent.getStringExtra("RECIPE_IMAGE_URL")

        // Connect UI elements to variables
        val nameTextView = findViewById<TextView>(R.id.detail_recipe_name)
        val ingredientsTextView = findViewById<TextView>(R.id.detail_recipe_ingredients)
        val instructionsTextView = findViewById<TextView>(R.id.detail_recipe_instructions)
        val imageView = findViewById<ImageView>(R.id.detail_recipe_image)

        // Set name & image immediately (before fetching extra details)
        nameTextView.text = recipeName
        title = recipeName // also update toolbar title

        // Load the recipe image using Glide
        Glide.with(this).load(recipeImageUrl).into(imageView)

        // Fetch full recipe details using the recipe ID (instructions & ingredients)
        if (recipeId != null) {
            fetchFullRecipeDetails(recipeId, ingredientsTextView, instructionsTextView)
        } else {
            // If ID is missing, show an error message
            Toast.makeText(this, "Error: Recipe ID not found.", Toast.LENGTH_SHORT).show()
        }
    }

    // Makes an API call to get ingredients and full instructions
    private fun fetchFullRecipeDetails(
        id: String,
        ingredientsTextView: TextView,
        instructionsTextView: TextView
    ) {

        // Create a Retrofit instance for the MealDB API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/") // base URL
            .addConverterFactory(GsonConverterFactory.create()) // JSON → Kotlin
            .build()

        val mealDbApi = retrofit.create(MealDbApi::class.java)

        // API request to look up the full recipe using its ID
        mealDbApi.lookupMealById(id).enqueue(object : Callback<MealDbResponse> {

            override fun onResponse(
                call: Call<MealDbResponse>,
                response: Response<MealDbResponse>
            ) {
                if (response.isSuccessful) {
                    // Only one meal is returned, so we take the first item
                    val meal = response.body()?.meals?.firstOrNull()

                    if (meal != null) {
                        // Set instructions (replace weird line breaks with clean ones)
                        instructionsTextView.text =
                            meal.instructions?.replace("\r\n", "\n")

                        // Format the 20 possible ingredients into readable bullet points
                        ingredientsTextView.text = formatIngredients(meal)
                    }
                }
            }

            override fun onFailure(call: Call<MealDbResponse>, t: Throwable) {
                // Log the error & show a toast if the API request fails
                Log.e("RecipeDetailActivity", "Failed to fetch details", t)
                Toast.makeText(
                    this@RecipeDetailActivity,
                    "Failed to load recipe details.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Converts the 20 ingredient + measure pairs into bullet points
    private fun formatIngredients(meal: Meal): String {

        val ingredients = mutableListOf<String>()

        // A map of ingredient → measurement
        // (MealDB always stores up to 20 ingredient pairs)
        val ingredientMap = mapOf(
            meal.ingredient1 to meal.strMeasure1, meal.ingredient2 to meal.strMeasure2,
            meal.ingredient3 to meal.strMeasure3, meal.ingredient4 to meal.strMeasure4,
            meal.ingredient5 to meal.strMeasure5, meal.ingredient6 to meal.strMeasure6,
            meal.ingredient7 to meal.strMeasure7, meal.ingredient8 to meal.strMeasure8,
            meal.ingredient9 to meal.strMeasure9, meal.ingredient10 to meal.strMeasure10,
            meal.ingredient11 to meal.strMeasure11, meal.ingredient12 to meal.strMeasure12,
            meal.ingredient13 to meal.strMeasure13, meal.ingredient14 to meal.strMeasure14,
            meal.ingredient15 to meal.strMeasure15, meal.ingredient16 to meal.strMeasure16,
            meal.ingredient17 to meal.strMeasure17, meal.ingredient18 to meal.strMeasure18,
            meal.ingredient19 to meal.strMeasure19, meal.ingredient20 to meal.strMeasure20
        )

        // Loop through all 20 possible pairs and only add non-empty ones
        for ((ingredient, measure) in ingredientMap) {
            if (!ingredient.isNullOrBlank()) {
                ingredients.add("• ${ingredient.trim()} (${measure?.trim()})")
            }
        }

        // Return the bullet list as a formatted block of text
        return ingredients.joinToString("\n")
    }

    // Handles the back arrow in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}