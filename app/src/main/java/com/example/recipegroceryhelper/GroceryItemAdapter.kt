package com.example.recipegroceryhelper

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

// adapter to display grocery items in a list with images and delete buttons
class GroceryItemAdapter(
    context: Context,
    private val itemsList: List<GroceryItemsActivity.GroceryItem>, // list of grocery items to display
    private val onDelete: (GroceryItemsActivity.GroceryItem) -> Unit // callback when delete button is clicked
) : ArrayAdapter<GroceryItemsActivity.GroceryItem>(context, 0, itemsList) {

    // creates and populates each list item view
    override fun getView(position: Int, oldView: View?, parent: ViewGroup): View {
        // get the grocery item at this position
        val currentItem = itemsList[position]

        // reuse existing view if available, otherwise inflate a new one
        val itemView = oldView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_grocery, parent, false)

        // find all the UI elements in the item layout
        val itemText = itemView.findViewById<TextView>(R.id.itemName)
        val deleteButton = itemView.findViewById<ImageButton>(R.id.deleteItemButton)
        val photoView = itemView.findViewById<ImageView>(R.id.itemImage)

        // set the name of the grocery item
        itemText.text = currentItem.name

        // show the item's photo if it has one
        if (currentItem.imageUrl.isNotEmpty()) {
            photoView.visibility = View.VISIBLE
            Glide.with(context)
                .load(currentItem.imageUrl)
                .centerCrop()
                .into(photoView)
        } else {
            // hide the image view if no photo exists
            photoView.visibility = View.GONE
        }

        // handle delete button click
        deleteButton.setOnClickListener {
            onDelete(currentItem)
        }

        return itemView
    }
}