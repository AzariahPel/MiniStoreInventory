package com.azartech.tradestore

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MastersActivity : Base() {

 private lateinit var info: TextView

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)

  if (!need("masters")) return

  val layout = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  setContentView(layout)

  layout.addView(title("Masters"))

  // Master data options
  val masterTypes = listOf(
   "Customers",
   "Suppliers",
   "Stock Categories",
   "Manage Taxes"
  )

  masterTypes.forEach { type ->

   val button = btn(type)

   layout.addView(button)

   button.setOnClickListener {
    simple(type)
   }
  }

  // Display record counts
  info = TextView(this).apply {
   setPadding(0, 24, 0, 0)
  }

  layout.addView(info)

  showCounts()
 }

 private fun showCounts() {

  val database = db.readableDatabase

  val customers = getCount(database, "customers")
  val suppliers = getCount(database, "suppliers")
  val categories = getCount(database, "categories")
  val taxes = getCount(database, "taxes")

  info.text = """
            Customers: $customers
            Suppliers: $suppliers
            Categories: $categories
            Taxes: $taxes
        """.trimIndent()
 }

 private fun getCount(
  database: android.database.sqlite.SQLiteDatabase,
  table: String
 ): Int {

  return database.rawQuery(
   "SELECT COUNT(*) FROM $table",
   null
  ).use { cursor ->

   if (cursor.moveToFirst()) {
    cursor.getInt(0)
   } else {
    0
   }
  }
 }

 private fun simple(type: String) {

  val box = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  val nameInput = edit("Name")

  box.addView(nameInput)

  if (type == "Manage Taxes") {

   val rateInput = edit("Rate %")

   box.addView(rateInput)

   AlertDialog.Builder(this)
    .setTitle(type)
    .setView(box)
    .setPositiveButton("SAVE") { _, _ ->

     val values = ContentValues()

     values.put(
      "name",
      nameInput.text.toString().trim()
     )

     values.put(
      "rate",
      rateInput.text.toString()
       .toDoubleOrNull() ?: 0.0
     )

     db.writableDatabase.insert(
      "taxes",
      null,
      values
     )

     showCounts()
    }
    .setNegativeButton("CANCEL", null)
    .show()

  } else {

   val table = when (type) {

    "Customers" -> "customers"

    "Suppliers" -> "suppliers"

    "Stock Categories" -> "categories"

    else -> "categories"
   }

   AlertDialog.Builder(this)
    .setTitle(type)
    .setView(box)
    .setPositiveButton("SAVE") { _, _ ->

     val values = ContentValues()

     values.put(
      "name",
      nameInput.text.toString().trim()
     )

     db.writableDatabase.insert(
      table,
      null,
      values
     )

     showCounts()
    }
    .setNegativeButton("CANCEL", null)
    .show()
  }
 }
}