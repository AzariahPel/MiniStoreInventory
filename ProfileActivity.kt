package com.azartech.tradestore

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class ProfileActivity : Base() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        if (!need("profile")) return

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
        }

        setContentView(layout)

        layout.addView(title("My Profile"))

        val username = Session.user(this)
        val role = Session.role(this)

        val profileInfo = TextView(this).apply {
            text = """
                Username: $username
                Role: $role
            """.trimIndent()

            textSize = 18f
            background = getDrawable(R.drawable.bg_card)
            setPadding(14, 14, 14, 14)
        }

        layout.addView(profileInfo)

        val logoutButton = btn("LOG OUT")

        layout.addView(logoutButton)

        logoutButton.setOnClickListener {

            Session.logout(this)

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finishAffinity()
        }
    }
}