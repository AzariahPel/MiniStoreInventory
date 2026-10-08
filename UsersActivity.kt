package com.azartech.tradestore

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast

class UsersActivity : Base() {

 private lateinit var list: LinearLayout

 override fun onCreate(b: Bundle?) {
  super.onCreate(b)

  if (!need("users")) return

  val layout = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  setContentView(layout)

  layout.addView(title("Manage Users"))

  val addButton = btn("+ ADD USER")
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
   showUserDialog()
  }

  loadUsers()
 }

 private fun loadUsers() {

  list.removeAllViews()

  val cursor = db.readableDatabase.rawQuery(
   """
            SELECT id, username, role, active
            FROM users
            ORDER BY username
            """.trimIndent(),
   null
  )

  cursor.use {

   while (it.moveToNext()) {

    val userId = it.getInt(0)
    val username = it.getString(1)
    val role = it.getString(2)
    val active = it.getInt(3) == 1

    val status = if (active) {
     "Active"
    } else {
     "Disabled"
    }

    val userButton = btn(
     "$username — $role — $status"
    )

    userButton.setOnClickListener {

     val values = ContentValues()

     values.put(
      "active",
      if (active) 0 else 1
     )

     db.writableDatabase.update(
      "users",
      values,
      "id = ?",
      arrayOf(userId.toString())
     )

     loadUsers()

     Toast.makeText(
      this,
      if (active) {
       "User disabled"
      } else {
       "User enabled"
      },
      Toast.LENGTH_SHORT
     ).show()
    }

    list.addView(userButton)
   }
  }
 }

 private fun showUserDialog() {

  val box = LinearLayout(this).apply {
   orientation = LinearLayout.VERTICAL
   setPadding(16, 16, 16, 16)
  }

  val usernameInput = edit("Username")
  val passwordInput = edit("Password")

  val roleSpinner = Spinner(this)

  val roles = listOf(
   Session.CASHIER,
   Session.MANAGER,
   Session.ADMIN
  )

  roleSpinner.adapter = ArrayAdapter(
   this,
   android.R.layout.simple_spinner_dropdown_item,
   roles
  )

  box.addView(usernameInput)
  box.addView(passwordInput)
  box.addView(roleSpinner)

  AlertDialog.Builder(this)
   .setTitle("Add User")
   .setView(box)
   .setPositiveButton("SAVE") { _, _ ->

    val username =
     usernameInput.text.toString().trim()

    val password =
     passwordInput.text.toString()

    val role =
     roleSpinner.selectedItem.toString()

    if (username.isEmpty()) {

     Toast.makeText(
      this,
      "Username is required",
      Toast.LENGTH_SHORT
     ).show()

     return@setPositiveButton
    }

    if (password.isEmpty()) {

     Toast.makeText(
      this,
      "Password is required",
      Toast.LENGTH_SHORT
     ).show()

     return@setPositiveButton
    }

    val values = ContentValues().apply {

     put(
      "username",
      username
     )

     put(
      "password",
      password
     )

     put(
      "role",
      role
     )

     put(
      "active",
      1
     )
    }

    val result = db.writableDatabase.insert(
     "users",
     null,
     values
    )

    if (result == -1L) {

     Toast.makeText(
      this,
      "Could not add user. Username may already exist.",
      Toast.LENGTH_LONG
     ).show()

    } else {

     Toast.makeText(
      this,
      "User added successfully",
      Toast.LENGTH_SHORT
     ).show()

     loadUsers()
    }
   }
   .setNegativeButton("CANCEL", null)
   .show()
 }
}