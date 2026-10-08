package com.azartech.tradestore

import android.content.*
import android.database.sqlite.*

class DB(c:Context):SQLiteOpenHelper(c,"tradestore.db",null,1){
    override fun onCreate(db:SQLiteDatabase){
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, role TEXT, active INTEGER DEFAULT 1)")
        db.execSQL("CREATE TABLE products(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, barcode TEXT UNIQUE, category TEXT, price REAL, cost REAL, qty INTEGER DEFAULT 0, reorder INTEGER DEFAULT 5)")
        db.execSQL("CREATE TABLE sales(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, total REAL, discount REAL, payment TEXT, created TEXT)")
        db.execSQL("CREATE TABLE sale_items(id INTEGER PRIMARY KEY AUTOINCREMENT, sale_id INTEGER, product_id INTEGER, qty INTEGER, price REAL)")
        db.execSQL("CREATE TABLE suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT)")
        db.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, phone TEXT)")
        db.execSQL("CREATE TABLE categories(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE)")
        db.execSQL("CREATE TABLE taxes(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, rate REAL)")
        db.execSQL("CREATE TABLE purchases(id INTEGER PRIMARY KEY AUTOINCREMENT, supplier TEXT, total REAL, received INTEGER, created TEXT)")
        db.execSQL("CREATE TABLE purchase_items(id INTEGER PRIMARY KEY AUTOINCREMENT, purchase_id INTEGER, product_id INTEGER, qty INTEGER, cost REAL)")
        seed(db)
    }
    private fun seed(db:SQLiteDatabase){
        val u=db.compileStatement("INSERT INTO users(username,password,role) VALUES(?,?,?)")
        listOf(arrayOf("cashier","cashier123",Session.CASHIER),arrayOf("manager","manager123",Session.MANAGER),arrayOf("admin","admin123",Session.ADMIN)).forEach{a->u.bindString(1,a[0]);u.bindString(2,a[1]);u.bindString(3,a[2]);u.executeInsert()}
        val p=db.compileStatement("INSERT INTO products(name,barcode,category,price,cost,qty,reorder) VALUES(?,?,?,?,?,?,?)")
        val data=listOf(arrayOf("Rice 10kg","89410021","Grocery",48.0,40.0,42,10),arrayOf("Cooking Oil 1L","89410022","Grocery",19.5,15.0,18,8),arrayOf("Soap Bar","89410023","Household",5.0,3.0,65,10),arrayOf("USB Cable","89410024","Electronics",15.0,8.0,11,5))
        data.forEach{a->p.bindString(1,a[0] as String);p.bindString(2,a[1] as String);p.bindString(3,a[2] as String);p.bindDouble(4,a[3] as Double);p.bindDouble(5,a[4] as Double);p.bindLong(6,(a[5] as Int).toLong());p.bindLong(7,(a[6] as Int).toLong());p.executeInsert()}
        listOf("Grocery","Household","Electronics").forEach{db.execSQL("INSERT INTO categories(name) VALUES(?)",arrayOf(it))}
        db.execSQL("INSERT INTO suppliers(name,phone) VALUES('ABC Wholesalers','70000001')")
        db.execSQL("INSERT INTO customers(name,phone) VALUES('Walk-in Customer','')")
        db.execSQL("INSERT INTO taxes(name,rate) VALUES('GST',10)")
    }
    override fun onUpgrade(db:SQLiteDatabase,old:Int,new:Int){db.execSQL("DROP TABLE IF EXISTS sale_items");db.execSQL("DROP TABLE IF EXISTS sales");db.execSQL("DROP TABLE IF EXISTS purchase_items");db.execSQL("DROP TABLE IF EXISTS purchases");db.execSQL("DROP TABLE IF EXISTS products");db.execSQL("DROP TABLE IF EXISTS users");db.execSQL("DROP TABLE IF EXISTS suppliers");db.execSQL("DROP TABLE IF EXISTS customers");db.execSQL("DROP TABLE IF EXISTS categories");db.execSQL("DROP TABLE IF EXISTS taxes");onCreate(db)}
    fun authenticate(u:String,p:String):String?{val c=readableDatabase.rawQuery("SELECT role FROM users WHERE username=? AND password=? AND active=1",arrayOf(u,p));c.use{if(it.moveToFirst())return it.getString(0)};return null}
}
