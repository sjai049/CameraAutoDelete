package com.cameraautodelete

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class PreferenceUtils {


    private val mContext: Context? = null

    private var mSettings: SharedPreferences? = null
    private var mEditor: Editor? = null

    companion object {

        const val APP_PREF = "SMARTDELETE_DB"
        @SuppressLint("StaticFieldLeak")
        private var sInstance: PreferenceUtils? = null
        @SuppressLint("StaticFieldLeak")
        private var sFCMInstance: PreferenceUtils? = null

        @JvmStatic
        open fun getInstance(context: Context): PreferenceUtils {
            if (sInstance == null) {
                sInstance = PreferenceUtils(context)
            }
            return sInstance as PreferenceUtils
        }


    }

    constructor()

    @SuppressLint("CommitPrefEdits")
    constructor(mContext: Context?) {
        mSettings = mContext?.getSharedPreferences(APP_PREF, MODE_PRIVATE)
        mEditor = mSettings?.edit()
    }


    /***
     * Set a value for the key
     */
    fun setValue(key: String, value: String) {
        mEditor!!.putString(key, value)
        mEditor!!.apply()
    }

    /****
     * Gets the value from the settings stored natively on the device.
     */
    fun getValue(key: String): String {
        return mSettings!!.getString(key, "")!!
    }

    /****
     * Gets the value from the preferences stored natively on the device.
     *
     * @param defValue Default value for the key, if one is not found.
     */
    fun getValue(key: String, defValue: Boolean): Boolean {
        return mSettings!!.getBoolean(key, defValue)
    }

    fun setValue(key: String, value: Boolean) {
        mEditor!!.putBoolean(key, value)
        mEditor!!.apply()
    }

    /****
     * Clear all the preferences store in this [Editor]
     */
    fun clear() {
        mEditor!!.clear().apply()
    }

    /**
     * Removes preference entry for the given key.
     */
    fun removeValue(key: String) {
        if (mEditor != null) {
            mEditor!!.remove(key).apply()
        }
    }
}
