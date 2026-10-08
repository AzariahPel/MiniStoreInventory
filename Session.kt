package com.azartech.tradestore
import android.content.Context
object Session {
    private const val PREF="session"; private const val USER="user"; private const val ROLE="role"
    const val CASHIER="Cashier"; const val MANAGER="Store Manager"; const val ADMIN="System Administrator"
    fun login(c:Context,u:String,r:String){ c.getSharedPreferences(PREF,0).edit().putString(USER,u).putString(ROLE,r).apply() }
    fun logout(c:Context){ c.getSharedPreferences(PREF,0).edit().clear().apply() }
    fun user(c:Context)=c.getSharedPreferences(PREF,0).getString(USER,"") ?: ""
    fun role(c:Context)=c.getSharedPreferences(PREF,0).getString(ROLE,"") ?: ""
    fun logged(c:Context)=user(c).isNotBlank()
    fun allowed(r:String,key:String)= when(r){ CASHIER->key in setOf("pos","profile"); MANAGER->key in setOf("pos","products","purchase","masters","import","reports","profile"); ADMIN->true; else->false }
}
