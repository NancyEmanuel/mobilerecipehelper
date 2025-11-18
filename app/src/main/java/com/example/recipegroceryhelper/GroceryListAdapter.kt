package com.example.recipegroceryhelper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.TextView

// adapter to display grocery lists with on click and delete functions
class GroceryListAdapter(
    context: Context,
    private val lists: List<GroceryListsFragment.GroceryList>, // list of grocery lists to display
    private val onListClicked: (GroceryListsFragment.GroceryList) -> Unit, // callback when a list is clicked
    private val onListDeleted: (GroceryListsFragment.GroceryList) -> Unit // callback when delete button is clicked
) : ArrayAdapter<GroceryListsFragment.GroceryList>(context, 0, lists) {

    // creates and populates each list item view
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // get grocery list at this position
        val list = lists[position]

        // reuse existing view if available, else inflate new one
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_grocery_list, parent, false)

        // find the UI elements in the item layout
        val groceryListName = view.findViewById<TextView>(R.id.listName)
        val deleteButton = view.findViewById<ImageButton>(R.id.deleteList)

        // set the name of the grocery list
        groceryListName.text = list.name

        // handle delete button click
        deleteButton.setOnClickListener {
            onListDeleted(list)
        }

        // handle clicking on the entire list item to open it
        view.setOnClickListener {
            onListClicked(list)
        }

        return view
    }
}
