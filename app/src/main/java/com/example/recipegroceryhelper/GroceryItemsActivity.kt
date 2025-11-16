package com.example.recipegroceryhelper

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID

class GroceryItemsActivity : AppCompatActivity() {

    // Request codes for permissions + camera activity
    private val CAMERA_REQUEST_CODE = 101
    private val CAMERA_PERMISSION_CODE = 102

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    // Adapter + data list
    private lateinit var adapter: GroceryItemAdapter
    private val groceryItems = mutableListOf<GroceryItem>()

    // ID of the grocery list we are viewing
    private lateinit var listId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grocery_items)

        // Retrieve list ID + name from previous activity
        listId = intent.getStringExtra("LIST_ID") ?: return
        val listName = intent.getStringExtra("LIST_NAME")

        // Display the name of the grocery list
        findViewById<TextView>(R.id.ListTitle).text = listName

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // UI elements
        val etNewItemName = findViewById<EditText>(R.id.NewItem)
        val btnAddItem = findViewById<Button>(R.id.AddItem)
        val btnAddPhoto = findViewById<Button>(R.id.BtnCaptureImage)
        val btnBack = findViewById<Button>(R.id.BtnBack)
        val listView = findViewById<ListView>(R.id.Items)

        // Set up the ListView adapter
        adapter = GroceryItemAdapter(this, groceryItems,
            { item, isChecked -> 
                // Checkbox logic (unused for now)
            },
            { item ->
                // Clicking an item opens edit/delete dialog
                showEditOrDeleteDialog(item)
            })

        listView.adapter = adapter

        // Return to previous screen
        btnBack.setOnClickListener { finish() }

        // Add new item without a photo
        btnAddItem.setOnClickListener {
            val itemName = etNewItemName.text.toString().trim()
            if (itemName.isNotEmpty()) {
                addNewItem(itemName)
                etNewItemName.text.clear()
            } else {
                Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show()
            }
        }

        // Capture image for item
        btnAddPhoto.setOnClickListener { checkCameraPermissionAndOpen() }

        // Load items from Firebase
        loadGroceryItems()
    }


    /* ---------------------- DIALOGS (Edit/Delete) ---------------------- */

    private fun showEditOrDeleteDialog(item: GroceryItem) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(this)
            .setTitle("Choose Action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditItemDialog(item)
                    1 -> deleteItem(item)
                }
            }
            .show()
    }

    private fun showEditItemDialog(item: GroceryItem) {
        val editText = EditText(this)
        editText.setText(item.name)

        AlertDialog.Builder(this)
            .setTitle("Edit Item Name")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) updateItemName(item, newName)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateItemName(item: GroceryItem, newName: String) {
        val userId = auth.currentUser?.uid ?: return
        val updatedItem = item.copy(name = newName)

        database.reference.child("users")
            .child(userId)
            .child("groceryLists")
            .child(listId)
            .child("items")
            .child(item.id)
            .setValue(updatedItem)
    }


    /* ---------------------- LOADING ITEMS ---------------------- */

    private fun loadGroceryItems() {
        val userId = auth.currentUser?.uid ?: return

        database.reference.child("users").child(userId)
            .child("groceryLists").child(listId).child("items")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    groceryItems.clear()

                    // Convert Firebase data into GroceryItem objects
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(GroceryItem::class.java)
                        if (item != null) groceryItems.add(item)
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Optional: show error toast
                }
            })
    }


    /* ---------------------- ADD + DELETE ITEMS ---------------------- */

    private fun addNewItem(itemName: String, imageUrl: String = "") {
        val userId = auth.currentUser?.uid ?: return

        // Generate unique ID for item
        val itemId = database.reference.push().key ?: return

        val item = GroceryItem(itemId, itemName, imageUrl)

        // Save under: users → uid → groceryLists → listId → items → itemId
        database.reference.child("users").child(userId)
            .child("groceryLists").child(listId).child("items").child(itemId)
            .setValue(item)
    }

    private fun deleteItem(item: GroceryItem) {
        val userId = auth.currentUser?.uid ?: return

        database.reference.child("users").child(userId)
            .child("groceryLists").child(listId).child("items").child(item.id)
            .removeValue()
    }


    /* ---------------------- CAMERA + IMAGE UPLOAD ---------------------- */

    private fun checkCameraPermissionAndOpen() {
        // If permission granted → open camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            // Ask for permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    // Called after user responds to permission popup
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                openCamera()
            else
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Receives the captured photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // Extract image bitmap from camera response
            val photo: Bitmap = data?.extras?.get("data") as Bitmap

            val itemName = findViewById<EditText>(R.id.NewItem).text.toString().trim()

            if (itemName.isNotEmpty()) {
                uploadImageAndAddItem(photo, itemName)
                findViewById<EditText>(R.id.NewItem).text.clear()
            } else {
                Toast.makeText(this, "Please enter an item name before taking a photo", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadImageAndAddItem(photo: Bitmap, itemName: String) {
        val userId = auth.currentUser?.uid ?: return

        val storageRef = storage.reference

        // Create unique filename
        val imageId = UUID.randomUUID().toString()
        val imagePath = "users/$userId/groceryLists/$listId/images/$imageId.jpg"
        val imageRef = storageRef.child(imagePath)

        // Convert bitmap → byte array
        val baos = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Upload to Firebase Storage
        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Save item with image download URL
                    addNewItem(itemName, uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
