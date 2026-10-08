package com.azartech.tradestore

import android.content.Intent
import android.os.Bundle
import android.widget.*

class DashboardActivity : Base() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (!need("profile")) return

        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
        }
        setContentView(r)

        r.addView(title("TradeStore Dashboard"))
        r.addView(TextView(this).apply {
            text = "Welcome ${Session.user(this@DashboardActivity)}\n${Session.role(this@DashboardActivity)}"
            textSize = 17f
        })

        val grid = GridLayout(this).apply {
            columnCount = 2
            rowCount = 6
            useDefaultMargins = true
        }
        r.addView(grid, LinearLayout.LayoutParams(-1, 0, 1f))

        fun add(t: String, key: String, c: Class<*>) {
            val x = btn(t)
            x.isEnabled = Session.allowed(Session.role(this), key)
            x.setOnClickListener { startActivity(Intent(this, c)) }
            grid.addView(
                x,
                GridLayout.LayoutParams().apply {
                    width = 0
                    height = 70
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
            )
        }

        add("POS / Sales", "pos", POSActivity::class.java)
        add("Stock Items", "products", ProductsActivity::class.java)
        add("Purchase Order", "purchase", PurchaseActivity::class.java)
        add("Masters", "masters", MastersActivity::class.java)
        add("Import Data", "import", ImportDataActivity::class.java)
        add("Reports", "reports", ReportsActivity::class.java)
        add("Manage Users", "users", UsersActivity::class.java)
        add("My Profile", "profile", ProfileActivity::class.java)
    }
}
