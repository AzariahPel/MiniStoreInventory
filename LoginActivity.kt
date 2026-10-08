package com.azartech.tradestore

import android.content.Intent
import android.os.Bundle
import android.widget.*

class LoginActivity : Base() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (Session.logged(this)) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        setContentView(root)

        root.addView(title("TradeStore Login"))
        root.addView(TextView(this).apply {
            text = "Simple retail management system"
            textSize = 16f
        })

        val u = edit("Username")
        val p = edit("Password")
        p.inputType = 129
        root.addView(u, lp())
        root.addView(p, lp())

        val login = btn("LOGIN")
        root.addView(login, lp(18))

        root.addView(TextView(this).apply {
            text = "Demo accounts\n" +
                    "Cashier: cashier / cashier123\n" +
                    "Store Manager: manager / manager123\n" +
                    "Administrator: admin / admin123"
            setPadding(0, 20, 0, 0)
        })

        login.setOnClickListener {
            val role = db.authenticate(u.text.toString().trim(), p.text.toString())
            if (role == null) {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            } else {
                Session.login(this, u.text.toString().trim(), role)
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
        }
    }

    private fun lp(top: Int = 10) = LinearLayout.LayoutParams(-1, 56).apply {
        setMargins(0, top, 0, 0)
    }
}
