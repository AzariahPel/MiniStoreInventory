package com.azartech.tradestore

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.*

class ProductsActivity : Base() {
    lateinit var list: LinearLayout

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        if (!need("products")) return
        build()
    }

    private fun build() {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }
        setContentView(r)
        r.addView(title("Stock Items"))

        val search = edit("Search name or barcode")
        r.addView(search, LinearLayout.LayoutParams(-1, 56))

        val add = btn("+ ADD ITEM")
        r.addView(add)

        list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        r.addView(
            ScrollView(this).apply { addView(list) },
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        add.setOnClickListener { dialog(null) }
        search.setOnEditorActionListener { _, _, _ ->
            load(search.text.toString())
            true
        }
        load("")
    }

    private fun load(q: String) {
        list.removeAllViews()
        val c = db.readableDatabase.rawQuery(
            "SELECT id,name,barcode,category,price,cost,qty,reorder FROM products " +
                    "WHERE name LIKE ? OR barcode LIKE ? ORDER BY name",
            arrayOf("%$q%", "%$q%")
        )

        c.use {
            while (it.moveToNext()) {
                val id = it.getInt(0)
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(12, 12, 12, 12)
                    background = getDrawable(R.drawable.bg_card)
                }

                card.addView(TextView(this).apply {
                    text = "${it.getString(1)}  •  ${it.getString(2)}\n" +
                            "${it.getString(3)}  •  K${it.getDouble(4)}  •  Stock ${it.getInt(6)}"
                    textSize = 16f
                })

                val row = LinearLayout(this)
                val e = btn("EDIT")
                val d = btn("DELETE")
                row.addView(e, LinearLayout.LayoutParams(0, 52, 1f))
                row.addView(d, LinearLayout.LayoutParams(0, 52, 1f))
                card.addView(row)

                e.setOnClickListener { dialog(id) }
                d.setOnClickListener {
                    db.writableDatabase.delete("products", "id=?", arrayOf(id.toString()))
                    load(q)
                }

                list.addView(
                    card,
                    LinearLayout.LayoutParams(-1, 110).apply {
                        setMargins(0, 8, 0, 0)
                    }
                )
            }
        }
    }

    private fun dialog(id: Int?) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val n = edit("Product name")
        val b = edit("Barcode")
        val cat = edit("Category")
        val price = edit("Selling price")
        val cost = edit("Cost price")
        val qty = edit("Quantity")

        box.addView(n)
        box.addView(b)
        box.addView(cat)
        box.addView(price)
        box.addView(cost)
        box.addView(qty)

        if (id != null) {
            val c = db.readableDatabase.rawQuery(
                "SELECT name,barcode,category,price,cost,qty FROM products WHERE id=?",
                arrayOf(id.toString())
            )
            c.use {
                if (it.moveToFirst()) {
                    n.setText(it.getString(0))
                    b.setText(it.getString(1))
                    cat.setText(it.getString(2))
                    price.setText(it.getString(3))
                    cost.setText(it.getString(4))
                    qty.setText(it.getString(5))
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle(if (id == null) "Add Product" else "Edit Product")
            .setView(box)
            .setPositiveButton("SAVE") { _, _ ->
                val v = ContentValues()
                v.put("name", n.text.toString())
                v.put("barcode", b.text.toString())
                v.put("category", cat.text.toString())
                v.put("price", price.text.toString().toDoubleOrNull() ?: 0.0)
                v.put("cost", cost.text.toString().toDoubleOrNull() ?: 0.0)
                v.put("qty", qty.text.toString().toIntOrNull() ?: 0)

                if (id == null) {
                    db.writableDatabase.insert("products", null, v)
                } else {
                    db.writableDatabase.update(
                        "products", v, "id=?", arrayOf(id.toString())
                    )
                }
                load("")
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}
