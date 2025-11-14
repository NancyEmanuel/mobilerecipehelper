package com.example.recipegroceryhelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecipesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var searchView: com.google.android.material.search.SearchView
    private val allRecipes = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipes, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_recipes)
        val searchBar = view.findViewById<com.google.android.material.search.SearchBar>(R.id.search_bar)
        searchView = view.findViewById(R.id.search_view)

        setupRecyclerView()
        setupSearch(searchBar)

        return view
    }

    private fun setupRecyclerView() {
        // Create sample data with a mix of ingredients and better image URLs
        allRecipes.add(Recipe("Pizza", "Pepperoni, Cheese, Tomato", "https://images.unsplash.com/photo-1594007654729-407eedc4be65"))
        allRecipes.add(Recipe("Soup", "Chicken, Dumpling, Broth, Carrots, Celery", "https://images.unsplash.com/photo-1594931891515-e2a27a6c6a46"))
        allRecipes.add(Recipe("Salad", "Lettuce, Tomato, Cucumber, Feta Cheese", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd"))
        allRecipes.add(Recipe("Pasta", "Spaghetti, Meatballs, Marinara, Garlic, Olive Oil", "https://images.unsplash.com/photo-1579684947550-22e945225d9a"))
        allRecipes.add(Recipe("Tacos", "Ground Beef, Tortilla, Salsa, Cheese, Lettuce", "https://images.unsplash.com/photo-1552332386-f8dd00dc2f85"))
        allRecipes.add(Recipe("Burger", "Beef Patty, Bun, Lettuce, Tomato, Onion", "https://images.unsplash.com/photo-1571091718767-18b5b1457add"))

        recipeAdapter = RecipeAdapter(allRecipes)
        recyclerView.adapter = recipeAdapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    private fun setupSearch(searchBar: com.google.android.material.search.SearchBar) {
        searchView.setupWithSearchBar(searchBar)

        searchView.editText.setOnEditorActionListener { _, _, _ ->
            val query = searchView.text.toString()
            filterRecipes(query)
            true
        }
    }

    private fun filterRecipes(query: String) {
        val filteredList = if (query.isEmpty()) {
            allRecipes
        } else {
            allRecipes.filter { it.name.contains(query, ignoreCase = true) }
        }
        recipeAdapter.updateData(filteredList)
    }
}
