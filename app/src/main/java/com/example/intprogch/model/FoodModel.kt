package com.example.intprogch.model

// Data class for a food menu item.
// All fields have defaults so Firestore can deserialize documents automatically.
data class FoodModel(
    val foodId: String = "",
    val foodName: String = "",
    val foodPrice: Double = 0.0,
    val foodCategory: String = ""
)
