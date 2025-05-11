package org.classapp.rollngo

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddCafeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cafe)

        val nameInput = findViewById<EditText>(R.id.editCafeName)
        val addressInput = findViewById<EditText>(R.id.editAddress)
        val descriptionInput = findViewById<EditText>(R.id.editDescription)
        val priceInput = findViewById<EditText>(R.id.editPrice)
        val latInput = findViewById<EditText>(R.id.editLatitude)
        val lngInput = findViewById<EditText>(R.id.editLongitude)
        val gamesInput = findViewById<EditText>(R.id.editGames)
        val facilitiesInput = findViewById<EditText>(R.id.editFacilities)
        val submitButton = findViewById<Button>(R.id.buttonSubmitCafe)

        val database = FirebaseDatabase.getInstance().getReference("cafes")
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        submitButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val address = addressInput.text.toString().trim()
            val description = descriptionInput.text.toString().trim()
            val price = priceInput.text.toString().trim()
            val lat = latInput.text.toString().toDoubleOrNull()
            val lng = lngInput.text.toString().toDoubleOrNull()
            val games = gamesInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val facilities = facilitiesInput.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (name.isEmpty() || address.isEmpty() || description.isEmpty() || price.isEmpty() || lat == null || lng == null) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cafeId = database.push().key ?: return@setOnClickListener

            val cafeData = mapOf(
                "name" to name,
                "address" to address,
                "description" to description,
                "price" to price,
                "position" to listOf(lat, lng),
                "games" to games,
                "facility" to facilities,
                "addedBy" to userId
            )

            database.child(cafeId).setValue(cafeData).addOnSuccessListener {
                Toast.makeText(this, "Cafe added!", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to add cafe: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
