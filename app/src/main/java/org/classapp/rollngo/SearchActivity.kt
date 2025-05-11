package org.classapp.rollngo

import android.content.ClipDescription
import android.content.Intent
import android.location.Address
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

data class Cafe(
    val name: String,
    val lat: Double,
    val lng: Double,
    val display: String,
    val description: String,
    val address: String,
    val price: String,
    val games: List<String>,
    val facility: List<String>
)

class SearchActivity : AppCompatActivity() {

    private lateinit var databaseRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CafeAdapter
    private val cafeList = mutableListOf<Cafe>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchBox = findViewById<EditText>(R.id.editTextSearch)
        val searchButton = findViewById<ImageButton>(R.id.buttonSearch)
        recyclerView = findViewById(R.id.recyclerSearchResults)

        adapter = CafeAdapter(cafeList) { selectedCafe ->
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("cafe_name", selectedCafe.name)
                putExtra("lat", selectedCafe.lat)
                putExtra("lng", selectedCafe.lng)
                putExtra("description", selectedCafe.description)
                putExtra("address", selectedCafe.address)
                putExtra("price", selectedCafe.price)
                putStringArrayListExtra("games", ArrayList(selectedCafe.games))
                putStringArrayListExtra("facility", ArrayList(selectedCafe.facility))
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        databaseRef = FirebaseDatabase.getInstance().getReference("cafes")

        // Firebase test write
        val testRef = FirebaseDatabase.getInstance().getReference("testConnection")
        testRef.setValue("connected from SearchActivity")

        searchButton.setOnClickListener {
            val keyword = searchBox.text.toString().trim()
            if (keyword.isNotEmpty()) {
                searchCafes(keyword)
            } else {
                Toast.makeText(this, "Please enter a cafe name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchCafes(query: String) {
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cafeList.clear()
                for (cafeSnap in snapshot.children) {
                    val name = cafeSnap.child("name").getValue(String::class.java) ?: continue
                    if (name.contains(query, ignoreCase = true)) {
                        val games = cafeSnap.child("games").children.mapNotNull {
                            it.getValue(String::class.java)
                        }
                        val displayedGames = games.take(2).joinToString(", ")

                        val price = cafeSnap.child("price").getValue(String::class.java) ?: "N/A"
                        val priceText = "💰 $price"

                        val position = cafeSnap.child("position").children.mapNotNull {
                            it.getValue(Double::class.java)
                        }

                        if (position.size >= 2) {
                            val lat = position[0]
                            val lng = position[1]
                            val description = cafeSnap.child("description").getValue(String::class.java) ?: ""
                            val address = cafeSnap.child("address").getValue(String::class.java) ?: ""
                            val facility = cafeSnap.child("facility").children.mapNotNull { it.getValue(String::class.java) }

                            val display = "$name\n🎲 $displayedGames\n$priceText"

                            cafeList.add(
                                Cafe(
                                    name = name,
                                    lat = lat,
                                    lng = lng,
                                    display = display,
                                    description = description,
                                    address = address,
                                    price = price,
                                    games = games,
                                    facility = facility
                                )
                            )
                        }
                    }
                }

                if (cafeList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    val dbUrl = databaseRef.ref.toString()
                    val message = "No cafes found in the database.\n\nDatabase path:\n$dbUrl"

                    AlertDialog.Builder(this@SearchActivity)
                        .setTitle("Cafe Not Found")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    recyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SearchActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

