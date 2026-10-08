package com.azartech.tradestore
import android.content.*
import android.widget.*
open class Base:androidx.appcompat.app.AppCompatActivity(){
    lateinit var db:DB
    override fun onStart(){super.onStart();db=DB(this)}
    fun need(key:String):Boolean{if(!Session.logged(this)){startActivity(Intent(this,LoginActivity::class.java));finish();return false};if(!Session.allowed(Session.role(this),key)){Toast.makeText(this,"Access denied",Toast.LENGTH_SHORT).show();finish();return false};return true}
    fun title(t:String):TextView=TextView(this).apply{text=t;textSize=26f;setTextColor(getColor(com.azartech.tradestore.R.color.text_primary));setPadding(0,0,0,18)}
    fun btn(t:String)=Button(this).apply{text=t;minimumHeight=52}
    fun edit(h:String)=EditText(this).apply{hint=h;setPadding(14,0,14,0);background=getDrawable(R.drawable.bg_input)}
}
