package com.abhishek.employeemanagment.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import es.dmoral.toasty.Toasty
import java.text.SimpleDateFormat
import java.util.*

fun Context.isConnected(): Boolean {
    val connectivityManager = this.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        connectivityManager.activeNetworkInfo?.run {
            return when (type) {
                TYPE_WIFI -> true
                TYPE_MOBILE -> true
                TYPE_ETHERNET -> true
                else -> false
            }
        }
    }
    return false
}

fun <T> List<T>.toArrayList(): ArrayList<T> {
    return ArrayList(this)
}

fun <T> HashSet<T>.toArrayList(): ArrayList<T> {
    val listToReturn = ArrayList<T>()

    for (item in listToReturn) {
        listToReturn.add(item)
    }

    return listToReturn
}

fun Context.toast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.toastyError(msg: String) {
    Toasty.error(this, msg).show()
}

fun Context.toastySuccess(msg: String) {
    Toasty.success(this, msg).show()
}

fun Context.toastyInfo(msg: String) {
    Toasty.info(this, msg).show()
}

fun returnCurrentTime(): String {
//    val c = Calendar.getInstance()
//    val date = c.get(Calendar.DATE)
//    val month = c.get(Calendar.MONTH) + 1
//    val year = c.get(Calendar.YEAR)
//    val hour = c.get(Calendar.HOUR_OF_DAY)
//    val minute = c.get(Calendar.MINUTE)
//    return "$date/$month/$year/$hour/$minute"

    val c = Calendar.getInstance()
    val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm aa")
    return sdf.format(c.time)
}

fun checkIsUpdateNeeded(localLastDate: String, lastTimeDBUpdate: String) {
    Date(localLastDate).after(Date(lastTimeDBUpdate))
    Log.d("TAG", Date(localLastDate).toString())
}

fun getItemByKeyFromSharedPreference(key: String, context: Context): String? {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    return sharedPref.getString(key, "")
}

fun saveItemByKeyInSharedPreference(key: String, value: String, context: Context) {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    sharedPref.edit().putString(key, value).apply()
}
