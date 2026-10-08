package com.azartech.tradestore

import android.content.ContentValues
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class ImportDataActivity : Base() {

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        // Check whether the logged-in user has import permission
        if (!need("import")) return

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        setContentView(layout)

        // Screen title
        layout.addView(title("Import Data"))

        // Instructions
        layout.addView(
            TextView(this).apply {
                text = """
                    Paste CSV rows for stock items.

                    Format:
                    name,barcode,category,price,cost,quantity
                """.trimIndent()
            }
        )

        // CSV input
        val input = EditText(this).apply {
            hint = "One product per line"
            gravity = Gravity.TOP
            minLines = 8
            background = getDrawable(R.drawable.bg_input)
        }

        layout.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // Import button
        val importButton = btn("IMPORT STOCK ITEMS")
        layout.addView(importButton)

        importButton.setOnClickListener {

            var count = 0

            val lines = input.text.toString().lines()

            for (line in lines) {

                if (line.isBlank()) {
                    continue
                }

                val values = line.split(",")

                if (values.size >= 6) {

                    val contentValues = ContentValues()

                    contentValues.put(
                        "name",
                        values[0].trim()
                    )

                    contentValues.put(
                        "barcode",
                        values[1].trim()
                    )

                    contentValues.put(
                        "category",
                        values[2].trim()
                    )

                    contentValues.put(
                        "price",
                        values[3].trim().toDoubleOrNull() ?: 0.0
                    )

                    contentValues.put(
                        "cost",
                        values[4].trim().toDoubleOrNull() ?: 0.0
                    )

                    contentValues.put(
                        "qty",
                        values[5].trim().toIntOrNull() ?: 0
                    )

                    val result = db.writableDatabase.insert(
                        "products",
                        null,
                        contentValues
                    )

                    if (result > 0) {
                        count++
                    }
                }
            }

            Toast.makeText(
                this,
                "Imported $count item(s)",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}