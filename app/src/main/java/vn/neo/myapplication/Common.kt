package vn.neo.myapplication

import android.content.Context
import android.preference.PreferenceManager

object Common {
    const val SCREEN_TABLET = "SCREEN_TABLET"
    fun Context.isTablet(): Boolean {
        //return resources.getBoolean(R.bool.isTablet)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        return preferences.getBoolean(SCREEN_TABLET, false)
    }
}