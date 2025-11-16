package com.example.recipegroceryhelper

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class GroceryListsFragment : Fragment() {

    // data class to represent grocery list
    data class GroceryList(
        val id: String = "",
        val name: String = "",
        val timestamp: Long = 0
    )

    // firebase authentication and database references
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    // list to store all the grocery lists from the firebase database
    private val groceryLists = mutableListOf<GroceryList>()

    // Adapter to display grocery lists in ListView
    private lateinit var adapter: GroceryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflate layout for this fragment
        val view = inflater.inflate(R.layout.activity_grocery_lists, container, false)

        // initialize the firebase services
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // find views from the layout
        val groceryListName = view.findViewById<EditText>(R.id.NewListName)
        val addListButton = view.findViewById<Button>(R.id.AddList)
        val listView = view.findViewById<ListView>(R.id.ViewLists)

        // set up adapter to display lists and handle clicks
        adapter = GroceryListAdapter(requireContext(), groceryLists, ::onListClicked, ::onListDeleted)
        listView.adapter = adapter

        // add a new list when add button clicked
        addListButton.setOnClickListener {
            val listName = groceryListName.text.toString().trim()
            if (listName.isNotEmpty()) {
                addList(listName)
                groceryListName.text.clear()
            }
        }

        // load the existing grocery lists from firebase
        loadGroceryLists()

        return view
    }

    // add a new grocery list to firebase database
    private fun addList(listName: String) {
        // get the current user ID
        val userId = auth.currentUser?.uid ?: return

        // createe unique id for the new list
        val listId = database.reference.push().key ?: return

        // create new grocerylist object
        val groceryList = GroceryList(listId, listName)

        // save list to Firebase under the users id and list id
        database.reference
            .child("users/$userId/groceryLists/$listId")
            .setValue(groceryList)
    }

    // loads all grocery lists from firebase database
    private fun loadGroceryLists() {
        val userId = auth.currentUser?.uid ?: return

        // listen for changes to the user's grocery lists
        database.reference
            // reference to the users grocery lists in the database
            .child("users/$userId/groceryLists")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear the current list
                    groceryLists.clear()

                    // loop through each grocery list in firebase
                    for (listSnapshot in snapshot.children) {
                        val list = listSnapshot.getValue(GroceryList::class.java)
                        if (list != null) {
                            groceryLists.add(list)
                        }
                    }

                    // update ListView to show new data
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // open the grocery list when clicked to view its items
    private fun onListClicked(groceryList: GroceryList) {
        // Create intent to open GroceryItemsActivity
        val intent = Intent(requireContext(), GroceryItemsActivity::class.java)

        // Pass list ID and name to the new activity
        intent.putExtra("LIST_ID", groceryList.id)
        intent.putExtra("LIST_NAME", groceryList.name)

        startActivity(intent)
    }

    // delete grocery list from firebase
    private fun onListDeleted(groceryList: GroceryList) {
        val userId = auth.currentUser?.uid ?: return

        // Remove list from Firebase
        database.reference
            .child("users/$userId/groceryLists/${groceryList.id}")
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "List deleted", Toast.LENGTH_SHORT).show()
            }
    }
}
