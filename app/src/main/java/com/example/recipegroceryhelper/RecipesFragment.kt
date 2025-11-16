package com.example.recipegroceryhelper

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RecipesFragment : Fragment() {

    // RecyclerView to display recipe cards in a grid layout
    private lateinit var recyclerView: RecyclerView

    // Adapter responsible for binding recipe data to the RecyclerView items
    private lateinit var recipeAdapter: RecipeAdapter

    // Material Design search bar at the top of the screen
    private lateinit var searchBar: SearchBar

    // The expanded search view that appears when the user clicks the search bar
    private lateinit var searchView: SearchView

    // Reference to the MealDB API interface
    private val mealDbApi: MealDbApi

    init {
        // Build a Retrofit instance with the base URL of the MealDB API
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/") // API base URL
            .addConverterFactory(GsonConverterFactory.create()) // convert JSON into Kotlin classes
            .build()

        // Create an implementation of the MealDbApi interface
        mealDbApi = retrofit.create(MealDbApi::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the fragment layout
        val view = inflater.inflate(R.layout.fragment_recipes, container, false)

        // Connect layout views to Kotlin variables
        recyclerView = view.findViewById(R.id.recycler_view_recipes)
        searchBar = view.findViewById(R.id.search_bar)
        searchView = view.findViewById(R.id.search_view)

        // Set up RecyclerView appearance and adapter
        setupRecyclerView()

        // Set up search functionality
        setupSearch()

        // Load default recipes only when the fragment is first created
        if (savedInstanceState == null) {
            fetchRecipesByFirstLetter("a") // Show recipes starting with "a" initially
        }

        return view
    }

    private fun setupRecyclerView() {
        // Initialize adapter with empty list until data loads
        recipeAdapter = RecipeAdapter(emptyList())

        // Attach adapter to the RecyclerView
        recyclerView.adapter = recipeAdapter

        // Display recipes in a 2-column grid
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    private fun setupSearch() {
        // Connect the SearchView with the SearchBar interaction
        searchView.setupWithSearchBar(searchBar)

        // Trigger search when user presses the search button on the keyboard
        searchView.editText.setOnEditorActionListener { _, _, _ ->
            val query = searchView.text.toString()

            // Make API call only if input is not empty
            if (query.isNotEmpty()) {
                searchRecipesByName(query)
            }

            // Collapse the search bar UI after submitting
            searchView.hide()
            true
        }
    }

    private fun fetchRecipesByFirstLetter(letter: String) {
        // Make API request for meals starting with a specific letter
        mealDbApi.listMealsByFirstLetter(letter)
            .enqueue(object : Callback<MealDbResponse> {

                override fun onResponse(
                    call: Call<MealDbResponse>,
                    response: Response<MealDbResponse>
                ) {
                    // If successful, update RecyclerView with meal data
                    if (response.isSuccessful) {
                        val meals = response.body()?.meals ?: emptyList()
                        recipeAdapter.updateData(meals)
                    }
                }

                override fun onFailure(call: Call<MealDbResponse>, t: Throwable) {
                    // Log error if API request fails
                    Log.e("RecipesFragment", "Failed to fetch recipes", t)
                }
            })
    }

    private fun searchRecipesByName(query: String) {
        // API call to search meals by name
        mealDbApi.searchByName(query)
            .enqueue(object : Callback<MealDbResponse> {

                override fun onResponse(
                    call: Call<MealDbResponse>,
                    response: Response<MealDbResponse>
                ) {
                    // Replace list data with search results
                    if (response.isSuccessful) {
                        val meals = response.body()?.meals ?: emptyList()
                        recipeAdapter.updateData(meals)
                    }
                }

                override fun onFailure(call: Call<MealDbResponse>, t: Throwable) {
                    // Log error if search fails
                    Log.e("RecipesFragment", "Failed to search recipes", t)
                }
            })
    }
}
