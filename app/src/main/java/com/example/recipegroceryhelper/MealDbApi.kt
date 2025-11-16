package com.example.recipegroceryhelper

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {

    // Search for meals by name
    @GET("api/json/v1/1/search.php")
    fun searchByName(@Query("s") query: String): Call<MealDbResponse>

    // Get a list of meals by main ingredient
    @GET("api/json/v1/1/filter.php")
    fun filterByIngredient(@Query("i") ingredient: String): Call<MealDbResponse>

    // Lookup a full meal details by id
    @GET("api/json/v1/1/lookup.php")
    fun lookupMealById(@Query("i") id: String): Call<MealDbResponse>
    
    // List all meals by first letter
    @GET("api/json/v1/1/search.php")
    fun listMealsByFirstLetter(@Query("f") letter: String): Call<MealDbResponse>
}
