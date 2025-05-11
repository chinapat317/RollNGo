package org.classapp.rollngo

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class BottomTab @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.activity_bottom_tab, this, true)

        findViewById<android.widget.Button>(R.id.buttonLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            context.startActivity(Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        findViewById<android.widget.Button>(R.id.buttonGoSearch).setOnClickListener {
            context.startActivity(Intent(context, SearchActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.buttonProfile).setOnClickListener {
            context.startActivity(Intent(context, UserProfileActivity::class.java))
        }
    }
}
