package com.example.intprogch

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.intprogch.adapter.FoodAdapter
import com.example.intprogch.model.FoodModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : AppCompatActivity() {

    private lateinit var listViewMenu: ListView
    private lateinit var btnAddFood: Button
    private lateinit var btnLogout: Button
    private lateinit var etSearch: EditText
    private lateinit var tvItemCount: TextView
    private lateinit var tvEmpty: TextView

    private val allFoodItems   = mutableListOf<FoodModel>()
    private val displayedItems = mutableListOf<FoodModel>()
    private lateinit var foodAdapter: FoodAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // Holds the real-time snapshot listener so we can remove it onDestroy
    private var listenerReg: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        listViewMenu = findViewById(R.id.listViewMenu)
        btnAddFood   = findViewById(R.id.fabAddFood)
        btnLogout    = findViewById(R.id.btnLogout)
        etSearch     = findViewById(R.id.etSearch)
        tvItemCount  = findViewById(R.id.tvItemCount)
        tvEmpty      = findViewById(R.id.layoutEmpty)

        foodAdapter = FoodAdapter(this, displayedItems)
        listViewMenu.adapter = foodAdapter

        // Tap a list item → go to UpdateFoodActivity
        listViewMenu.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val food = displayedItems[position]
            val intent = Intent(this, UpdateFoodActivity::class.java).apply {
                putExtra(UpdateFoodActivity.EXTRA_FOOD_ID,       food.foodId)
                putExtra(UpdateFoodActivity.EXTRA_FOOD_NAME,     food.foodName)
                putExtra(UpdateFoodActivity.EXTRA_FOOD_PRICE,    food.foodPrice)
                putExtra(UpdateFoodActivity.EXTRA_FOOD_CATEGORY, food.foodCategory)
            }
            startActivity(intent)
        }

        btnAddFood.setOnClickListener {
            startActivity(Intent(this, AddFoodActivity::class.java))
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show()
                    goToLogin()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMenu(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadMenu()
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerReg?.remove()
    }

    private fun loadMenu() {
        tvItemCount.text = "Loading..."

        // Real-time listener on the RestaurantMenu collection
        listenerReg = db.collection("RestaurantMenu")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                allFoodItems.clear()
                snapshot?.documents?.forEach { doc ->
                    val food = doc.toObject(FoodModel::class.java)
                    if (food != null) allFoodItems.add(food)
                }

                filterMenu(etSearch.text.toString())
            }
    }

    private fun filterMenu(query: String) {
        val filtered = if (query.isBlank()) {
            allFoodItems.toList()
        } else {
            val lower = query.lowercase()
            allFoodItems.filter {
                it.foodName.lowercase().contains(lower) ||
                it.foodCategory.lowercase().contains(lower)
            }
        }

        foodAdapter.updateList(filtered)
        tvItemCount.text = "${filtered.size} item(s) found"

        val isEmpty = filtered.isEmpty()
        listViewMenu.visibility = if (isEmpty) View.GONE  else View.VISIBLE
        tvEmpty.visibility      = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
