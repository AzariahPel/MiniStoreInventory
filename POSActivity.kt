package com.azartech.tradestore

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class POSActivity : Base() {

 data class Item(
  val id: Int,
  val name: String,
  val price: Double,
  var qty: Int
 )

 private val cart = mutableListOf<Item>()

 private lateinit var cartText: TextView
 private lateinit var productList: LinearLayout

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)

  if (!need("pos")) return

  val root = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  setContentView(root)

  root.addView(title("POS / Sales"))

  // Shopping cart display
  cartText = TextView(this).apply {
   textSize = 17f
   background = getDrawable(R.drawable.bg_card)
   setPadding(12, 12, 12, 12)
  }

  root.addView(cartText)

  // Product list
  productList = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
  }

  val scrollView = ScrollView(this).apply {
   addView(productList)
  }

  root.addView(
   scrollView,
   LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    0,
    1f
   )
  )

  // Complete sale button
  val paymentButton = btn("COMPLETE SALE")

  root.addView(paymentButton)

  paymentButton.setOnClickListener {
   checkout()
  }

  load()
 }

 private fun load() {

  productList.removeAllViews()

  val cursor = db.readableDatabase.rawQuery(
   """
            SELECT id, name, price, qty
            FROM products
            WHERE qty > 0
            ORDER BY name
            """.trimIndent(),
   null
  )

  cursor.use {

   while (it.moveToNext()) {

    val productId = it.getInt(0)
    val productName = it.getString(1)
    val productPrice = it.getDouble(2)
    val productStock = it.getInt(3)

    val button = btn(
     "$productName  • K$productPrice  • Stock $productStock"
    )

    button.setOnClickListener {

     add(
      Item(
       id = productId,
       name = productName,
       price = productPrice,
       qty = 1
      )
     )
    }

    productList.addView(button)
   }
  }

  refresh()
 }

 private fun add(item: Item) {

  val existingItem = cart.find {
   it.id == item.id
  }

  if (existingItem != null) {
   existingItem.qty++
  } else {
   cart.add(item)
  }

  refresh()
 }

 private fun refresh() {

  if (cart.isEmpty()) {

   cartText.text = "Cart: empty"

   return
  }

  val cartLines = cart.joinToString("\n") {
   "${it.name} x${it.qty} = K${it.qty * it.price}"
  }

  val total = cart.sumOf {
   it.qty * it.price
  }

  cartText.text = """
            Cart:
            $cartLines
            
            Total: K$total
        """.trimIndent()
 }

 private fun checkout() {

  if (cart.isEmpty()) {

   Toast.makeText(
    this,
    "Add an item first",
    Toast.LENGTH_SHORT
   ).show()

   return
  }

  val paymentOptions = arrayOf(
   "Cash",
   "Card"
  )

  AlertDialog.Builder(this)
   .setTitle("Payment method")
   .setItems(paymentOptions) { _, which ->

    val total = cart.sumOf {
     it.qty * it.price
    }

    val values = ContentValues()

    values.put(
     "username",
     Session.user(this)
    )

    values.put(
     "total",
     total
    )

    values.put(
     "discount",
     0
    )

    values.put(
     "payment",
     paymentOptions[which]
    )

    values.put(
     "created",
     System.currentTimeMillis().toString()
    )

    val saleId = db.writableDatabase.insert(
     "sales",
     null,
     values
    )

    if (saleId == -1L) {

     Toast.makeText(
      this,
      "Unable to save sale",
      Toast.LENGTH_LONG
     ).show()

     return@setItems
    }

    // Save sale items and reduce inventory
    cart.forEach { item ->

     val saleItem = ContentValues()

     saleItem.put(
      "sale_id",
      saleId
     )

     saleItem.put(
      "product_id",
      item.id
     )

     saleItem.put(
      "qty",
      item.qty
     )

     saleItem.put(
      "price",
      item.price
     )

     db.writableDatabase.insert(
      "sale_items",
      null,
      saleItem
     )

     db.writableDatabase.execSQL(
      "UPDATE products SET qty = qty - ? WHERE id = ?",
      arrayOf(
       item.qty,
       item.id
      )
     )
    }

    cart.clear()

    refresh()
    load()

    Toast.makeText(
     this,
     "Sale completed: K$total",
     Toast.LENGTH_LONG
    ).show()
   }
   .show()
 }
}