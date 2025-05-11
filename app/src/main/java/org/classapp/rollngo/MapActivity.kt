package org.classapp.rollngo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.TextView
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.widget.Toast


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val locationPermissionCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val cafeName = intent.getStringExtra("cafe_name")
        val description = intent.getStringExtra("description")
        val address = intent.getStringExtra("address")
        val price = intent.getStringExtra("price")
        val games = intent.getStringArrayListExtra("games") ?: arrayListOf()
        val facility = intent.getStringArrayListExtra("facility") ?: arrayListOf()

        findViewById<TextView>(R.id.textCafeName).text = cafeName
        findViewById<TextView>(R.id.textCafeDescription).text = "📝 $description"
        findViewById<TextView>(R.id.textCafeAddress).text = "📍 $address"
        findViewById<TextView>(R.id.textCafeGames).text = "🎲 Games: ${games.joinToString(", ")}"
        findViewById<TextView>(R.id.textCafeFacilities).text = "🏷 Facilities: ${facility.joinToString(", ")}"
        findViewById<TextView>(R.id.textCafePrice).text = "💰 $price"

        // 🔹 Favorite button setup
        val buttonFavorite = findViewById<Button>(R.id.buttonFavorite)
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (cafeName != null && userId != null) {
            val favRef = FirebaseDatabase.getInstance()
                .getReference("favorites")
                .child(userId)
                .child(cafeName)

            favRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Already favorited → show "Unfavourite"
                        buttonFavorite.text = "Unfavourite"
                        buttonFavorite.setOnClickListener {
                            favRef.removeValue().addOnSuccessListener {
                                Toast.makeText(this@MapActivity, "Removed from favorites", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        // Not yet favorited
                        buttonFavorite.text = "Add to Favorites"
                        buttonFavorite.setOnClickListener {
                            favRef.setValue(true).addOnSuccessListener {
                                Toast.makeText(this@MapActivity, "Added to favorites", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MapActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

        } else {
            buttonFavorite.isEnabled = false
            buttonFavorite.text = "Login Required"
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val cafeName = intent.getStringExtra("cafe_name") ?: "Cafe"
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lng = intent.getDoubleExtra("lng", 0.0)
        val cafeLocation = LatLng(lat, lng)

        map.addMarker(MarkerOptions().position(cafeLocation).title(cafeName))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cafeLocation, 17f))
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
            return
        }

        map.isMyLocationEnabled = true

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                map.addMarker(MarkerOptions().position(userLatLng).title("You are here"))
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        }
    }
}
