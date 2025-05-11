package org.classapp.rollngo

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.Button
import android.widget.Toast

class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName ?: "Unknown"
        val userPhotoUrl = user?.photoUrl
        val userId = user?.uid ?: return

        val nameView = findViewById<TextView>(R.id.textUserName)
        val photoView = findViewById<ImageView>(R.id.imageUserPhoto)

        nameView.text = userName

        if (userPhotoUrl != null) {
            Glide.with(this)
                .load(userPhotoUrl)
                .circleCrop()
                .into(photoView)
        }

        // Setup RecyclerView for favorite cafes
        val favList = mutableListOf<Cafe>()
        val favAdapter = FavoriteCafeAdapter(favList) { cafe ->
            val intent = Intent(this, MapActivity::class.java).apply {
                putExtra("cafe_name", cafe.name)
                putExtra("lat", cafe.lat)
                putExtra("lng", cafe.lng)
                putExtra("description", cafe.description)
                putExtra("address", cafe.address)
                putExtra("price", cafe.price)
                putStringArrayListExtra("games", ArrayList(cafe.games))
                putStringArrayListExtra("facility", ArrayList(cafe.facility))
            }
            startActivity(intent)
        }

        val favRecycler = findViewById<RecyclerView>(R.id.favCafeList)
        favRecycler.layoutManager = LinearLayoutManager(this)
        favRecycler.adapter = favAdapter

        // Load user's favorite cafes
        val favRef = FirebaseDatabase.getInstance().getReference("favorites").child(userId)
        val cafeRef = FirebaseDatabase.getInstance().getReference("cafes")

        favRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val favCafeNames = snapshot.children.mapNotNull { it.key }

                cafeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(cafeSnapshot: DataSnapshot) {
                        favList.clear()

                        for (cafeNode in cafeSnapshot.children) {
                            val name = cafeNode.child("name").getValue(String::class.java) ?: continue
                            if (name !in favCafeNames) continue

                            val description = cafeNode.child("description").getValue(String::class.java) ?: ""
                            val address = cafeNode.child("address").getValue(String::class.java) ?: ""
                            val price = cafeNode.child("price").getValue(String::class.java) ?: ""
                            val games = cafeNode.child("games").children.mapNotNull { it.getValue(String::class.java) }
                            val facilities = cafeNode.child("facility").children.mapNotNull { it.getValue(String::class.java) }
                            val position = cafeNode.child("position").children.mapNotNull { it.getValue(Double::class.java) }

                            if (position.size >= 2) {
                                favList.add(
                                    Cafe(
                                        name = name,
                                        lat = position[0],
                                        lng = position[1],
                                        display = name,
                                        description = description,
                                        address = address,
                                        price = price,
                                        games = games,
                                        facility = facilities
                                    )
                                )
                            }
                        }

                        favAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        val addCafeBtn = findViewById<Button>(R.id.buttonAddNewCafe)
        addCafeBtn.setOnClickListener {
            val intent = Intent(this, AddCafeActivity::class.java)
            startActivity(intent)
        }

        val yourCafeList = mutableListOf<Pair<String, String>>() // key, name
        val yourCafeAdapter = YourCafeAdapter(yourCafeList) { cafeKey ->
            val cafeRef = FirebaseDatabase.getInstance().getReference("cafes").child(cafeKey)
            cafeRef.removeValue().addOnSuccessListener {
                Toast.makeText(this, "Cafe deleted", Toast.LENGTH_SHORT).show()
            }
        }

        val yourCafeRecycler = findViewById<RecyclerView>(R.id.yourCafeList)
        yourCafeRecycler.layoutManager = LinearLayoutManager(this)
        yourCafeRecycler.adapter = yourCafeAdapter

        val allCafesRef = FirebaseDatabase.getInstance().getReference("cafes")
        allCafesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                yourCafeList.clear()
                for (cafeSnap in snapshot.children) {
                    val addedBy = cafeSnap.child("addedBy").getValue(String::class.java)
                    val name = cafeSnap.child("name").getValue(String::class.java) ?: continue
                    if (addedBy == userId) {
                        yourCafeList.add(Pair(cafeSnap.key ?: continue, name))
                    }
                }
                yourCafeAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
