package com.example.intprogch

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.intprogch.model.FoodModel
import com.google.firebase.firestore.FirebaseFirestore

class AddFoodActivity : AppCompatActivity() {

    private lateinit var etFoodName: EditText
    private lateinit var etFoodPrice: EditText
    private lateinit var etFoodCategory: EditText
    private lateinit var btnSave: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        etFoodName     = findViewById(R.id.etFoodName)
        etFoodPrice    = findViewById(R.id.etFoodPrice)
        etFoodCategory = findViewById(R.id.etFoodCategory)
        btnSave        = findViewById(R.id.btnSave)

        findViewById<android.widget.Button>(R.id.btnBack).setOnClickListener { finish() }
        btnSave.setOnClickListener { saveFood() }
    }

    private fun saveFood() {
        val name     = etFoodName.text.toString().trim()
        val priceStr = etFoodPrice.text.toString().trim()
        val category = etFoodCategory.text.toString().trim()

        // Validate
        if (name.isEmpty()) {
            etFoodName.error = "Food name is required"
            etFoodName.requestFocus()
            return
        }
        if (priceStr.isEmpty()) {
            etFoodPrice.error = "Price is required"
            etFoodPrice.requestFocus()
            return
        }
        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            etFoodPrice.error = "Enter a valid price greater than 0"
            etFoodPrice.requestFocus()
            return
        }
        if (category.isEmpty()) {
            etFoodCategory.error = "Category is required"
            etFoodCategory.requestFocus()
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        // Let Firestore generate the document ID
        val docRef = db.collection("RestaurantMenu").document()
        val foodId = docRef.id

        val food = FoodModel(
            foodId       = foodId,
            foodName     = name,
            foodPrice    = price,
            foodCategory = category
        )

        docRef.set(food)
            .addOnSuccessListener {
                Toast.makeText(this, "$name added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                btnSave.isEnabled = true
                btnSave.text = "Save Item"
            }
    }
}
