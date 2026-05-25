package com.example.intprogch

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.intprogch.model.FoodModel
import com.google.firebase.firestore.FirebaseFirestore

class UpdateFoodActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_FOOD_ID       = "extra_food_id"
        const val EXTRA_FOOD_NAME     = "extra_food_name"
        const val EXTRA_FOOD_PRICE    = "extra_food_price"
        const val EXTRA_FOOD_CATEGORY = "extra_food_category"
    }

    private lateinit var tvFoodId: TextView
    private lateinit var etFoodName: EditText
    private lateinit var etFoodPrice: EditText
    private lateinit var etFoodCategory: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    private var currentFoodId    = ""
    private var originalFoodName = ""

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_food)

        tvFoodId       = findViewById(R.id.tvFoodId)
        etFoodName     = findViewById(R.id.etFoodName)
        etFoodPrice    = findViewById(R.id.etFoodPrice)
        etFoodCategory = findViewById(R.id.etFoodCategory)
        btnUpdate      = findViewById(R.id.btnUpdate)
        btnDelete      = findViewById(R.id.btnDelete)

        // Read data passed from MainActivity via Intent
        currentFoodId    = intent.getStringExtra(EXTRA_FOOD_ID) ?: ""
        originalFoodName = intent.getStringExtra(EXTRA_FOOD_NAME) ?: ""
        val price        = intent.getDoubleExtra(EXTRA_FOOD_PRICE, 0.0)
        val category     = intent.getStringExtra(EXTRA_FOOD_CATEGORY) ?: ""

        if (currentFoodId.isEmpty()) {
            Toast.makeText(this, "Error: no item selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvFoodId.text = "ID: ${currentFoodId.takeLast(8).uppercase()}"
        etFoodName.setText(originalFoodName)
        etFoodPrice.setText(if (price > 0) price.toString() else "")
        etFoodCategory.setText(category)

        btnUpdate.setOnClickListener { updateFood() }
        btnDelete.setOnClickListener { confirmDelete() }
        findViewById<android.widget.Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun updateFood() {
        val name     = etFoodName.text.toString().trim()
        val priceStr = etFoodPrice.text.toString().trim()
        val category = etFoodCategory.text.toString().trim()

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

        btnUpdate.isEnabled = false
        btnUpdate.text = "Updating..."

        val updatedFood = FoodModel(
            foodId       = currentFoodId,
            foodName     = name,
            foodPrice    = price,
            foodCategory = category
        )

        // Overwrite the entire document with the updated data
        db.collection("RestaurantMenu").document(currentFoodId)
            .set(updatedFood)
            .addOnSuccessListener {
                Toast.makeText(this, "$name updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                btnUpdate.isEnabled = true
                btnUpdate.text = "Update Item"
            }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Delete \"$originalFoodName\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteFood() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFood() {
        btnDelete.isEnabled = false
        btnDelete.text = "Deleting..."

        db.collection("RestaurantMenu").document(currentFoodId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "$originalFoodName deleted.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                btnDelete.isEnabled = true
                btnDelete.text = "Delete Item"
            }
    }
}
