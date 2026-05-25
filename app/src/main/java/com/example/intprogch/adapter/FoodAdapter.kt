package com.example.intprogch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.intprogch.R
import com.example.intprogch.model.FoodModel

class FoodAdapter(
    context: Context,
    private val foodList: MutableList<FoodModel>
) : ArrayAdapter<FoodModel>(context, R.layout.list_item_food, foodList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_food, parent, false)

        val food = getItem(position) ?: return view

        view.findViewById<TextView>(R.id.tvFoodName).text  = food.foodName
        view.findViewById<TextView>(R.id.tvPrice).text     = "₱%.2f".format(food.foodPrice)
        view.findViewById<TextView>(R.id.tvCategory).text  = food.foodCategory

        return view
    }

    fun updateList(newList: List<FoodModel>) {
        foodList.clear()
        foodList.addAll(newList)
        notifyDataSetChanged()
    }
}
