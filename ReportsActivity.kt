package com.azartech.tradestore

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class ReportsActivity : Base() {

 private lateinit var out: TextView

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)

  if (!need("reports")) return

  val layout = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  setContentView(layout)

  layout.addView(title("Reports"))

  out = TextView(this).apply {
   textSize = 16f
   background = getDrawable(R.drawable.bg_card)
   setPadding(14, 14, 14, 14)
  }

  layout.addView(out)

  val reportTypes = listOf(
   "Daily Activity Report",
   "Sales Report",
   "Purchase Report",
   "Sales Outstanding",
   "Purchase Outstanding",
   "Low Stock Items Report",
   "Purchase Order Details"
  )

  reportTypes.forEach { reportName ->

   val button = btn(reportName)

   layout.addView(button)

   button.setOnClickListener {
    generateReport(reportName)
   }
  }

  generateReport("Daily Activity Report")
 }

 private fun generateReport(reportName: String) {

  val database = db.readableDatabase

  // Total sales and number of sales transactions
  val salesCursor = database.rawQuery(
   """
            SELECT 
                COALESCE(SUM(total), 0),
                COUNT(*)
            FROM sales
            """.trimIndent(),
   null
  )

  var totalSales = 0.0
  var salesCount = 0

  salesCursor.use {

   if (it.moveToFirst()) {
    totalSales = it.getDouble(0)
    salesCount = it.getInt(1)
   }
  }

  // Number of low-stock products
  val lowStockCursor = database.rawQuery(
   """
            SELECT COUNT(*)
            FROM products
            WHERE qty <= reorder
            """.trimIndent(),
   null
  )

  var lowStockCount = 0

  lowStockCursor.use {

   if (it.moveToFirst()) {
    lowStockCount = it.getInt(0)
   }
  }

  // Total purchases
  val purchaseCursor = database.rawQuery(
   """
            SELECT COALESCE(SUM(total), 0)
            FROM purchases
            """.trimIndent(),
   null
  )

  var totalPurchases = 0.0

  purchaseCursor.use {

   if (it.moveToFirst()) {
    totalPurchases = it.getDouble(0)
   }
  }

  out.text = """
            $reportName
            
            Total sales: K${"%.2f".format(totalSales)}
            Sales transactions: $salesCount
            
            Total purchases: K${"%.2f".format(totalPurchases)}
            
            Low-stock products: $lowStockCount
            
            Reports are generated from the local TradeStore database.
        """.trimIndent()
 }
}