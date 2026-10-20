package com.example.lab09

data class RecetaModel(
    val id: Int,
    val name: String,
    val ingredients: List<String>?,
    val instructions: List<String>?,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val servings: Int?,
    val difficulty: String?,
    val cuisine: String?,
    val caloriesPerServing: Int?,
    val image: String?,
    val rating: Double?,
    val reviewCount: Int?
)

data class RecetasRespuesta(
    val recipes: List<RecetaModel>,
    val total: Int
)