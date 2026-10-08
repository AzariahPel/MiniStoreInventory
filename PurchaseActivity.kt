package com.azartech.tradestore

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class PurchaseActivity : Base() {

 private lateinit var list: LinearLayout

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)

  if (!need("purchase")) return

  val layout = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  setContentView(layout)

  layout.addView(title("Purchase Order"))

  val addButton = btn("+ RECEIVE PURCHASE")
  layout.addView(addButton)

  list = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
  }

  val scrollView = ScrollView(this).apply {
   addView(list)
  }

  layout.addView(
   scrollView,
   LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    0,
    1f
   )
  )

  addButton.setOnClickListener {
   showPurchaseDialog()
  }

  loadPurchases()
 }

 private fun loadPurchases() {

  list.removeAllViews()

  val cursor = db.readableDatabase.rawQuery(
   """
            SELECT id, supplier, total, created
            FROM purchases
            ORDER BY id DESC
            """.trimIndent(),
   null
  )

  cursor.use {

   while (it.moveToNext()) {

    val purchaseId = it.getInt(0)
    val supplier = it.getString(1)
    val total = it.getDouble(2)
    val created = it.getString(3)

    val purchaseView = TextView(this).apply {

     text = """
                        PO-$purchaseId
                        Supplier: $supplier
                        Total: K${"%.2f".format(total)}
                        Date: $created
                    """.trimIndent()

     textSize = 16f
     background = getDrawable(R.drawable.bg_card)

     setPadding(
      14,
      14,
      14,
      14
     )
    }

    val params = LinearLayout.LayoutParams(
     LinearLayout.LayoutParams.MATCH_PARENT,
     LinearLayout.LayoutParams.WRAP_CONTENT
    )

    params.setMargins(
     0,
     8,
     0,
     0
    )

    list.addView(
     purchaseView,
     params
    )
   }
  }
 }

 private fun showPurchaseDialog() {

  val box = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  val supplier = edit("Supplier")
  val productId = edit("Product ID (1-4 demo)")
  val quantity = edit("Quantity")
  val cost = edit("Unit cost")

  box.addView(supplier)
  box.addView(productId)
  box.addView(quantity)
  box.addView(cost)

  AlertDialog.Builder(this)
   .setTitle("Receive Purchase")
   .setView(box)
   .setPositiveButton("SAVE") { _, _ ->

    val productIdValue =
     productId.text.toString().toIntOrNull()

    val quantityValue =
     quantity.text.toString().toIntOrNull() ?: 0

    val costValue =
     cost.text.toString().toDoubleOrNull() ?: 0.0

    if (productIdValue == null) {
     Toast.makeText(
      this,
      "Enter a valid product ID",
      Toast.LENGTH_SHORT
     ).show()
     return@setPositiveButton
    }

    if (quantityValue <= 0) {
     Toast.makeText(
      this,
      "Quantity must be greater than 0",
      Toast.LENGTH_SHORT
     ).show()
     return@setPositiveButton
    }

    if (costValue < 0) {
     Toast.makeText(
      this,
      "Cost cannot be negative",
      Toast.LENGTH_SHORT
     ).show()
     return@setPositiveButton
    }

    val total = quantityValue * costValue

    val purchaseValues = ContentValues().apply {
     put(
      "supplier",
      supplier.text.toString().trim()
     )

     put(
      "total",
      total
     )

     put(
      "received",
      1
     )

     put(
      "created",
      System.currentTimeMillis().toString()
     )
    }

    val database = db.writableDatabase

    val purchaseId = database.insert(
     "purchases",
     null,
     purchaseValues
    )

    if (purchaseId == -1L) {
     Toast.makeText(
      this,
      "Could not save purchase",
      Toast.LENGTH_SHORT
     ).show()
     return@setPositiveButton
    }

    val purchaseItemValues = ContentValues().apply {
     put(
      "purchase_id",
      purchaseId
     )

     put(
      "product_id",
      productIdValue
     )

     put(
      "qty",
      quantityValue
     )

     put(
      "cost",
      costValue
     )
    }

    database.insert(
     "purchase_items",
     null,
     purchaseItemValues
    )

    database.execSQL(
     "UPDATE products SET qty = qty + ? WHERE id = ?",
     arrayOf(
      quantityValue,
      productIdValue
     )
    )

    loadPurchases()

    Toast.makeText(
     this,
     "Purchase received successfully",
     Toast.LENGTH_SHORT
    ).show()
   }
   .setNegativeButton("CANCEL", null)
   .show()
 }
}