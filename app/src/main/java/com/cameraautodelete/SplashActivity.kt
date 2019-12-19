package com.cameraautodelete

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.YearMonth
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


public open class SplashActivity : AppCompatActivity() {

    public var mPreferenceUtils: PreferenceUtils? = null
    var mUserId: String = ""
    var mUserToken: String = ""
    lateinit var animSlideIn: Animation
    lateinit var animSlideOut: Animation
    var PLAY_SERVICES_RESOLUTION_REQUEST: Int = 9000
    var UPDATE_INTERVAL: Long = 5000
    var FASTEST_INTERVAL: Long = 5000
    private var toast: Toast? = null

    var minYearBase = 1950
    var maxYearBase = Calendar.getInstance().get(Calendar.YEAR) + 1
    var yearPosBase = 0
    var monthPosBase = 0
    var dayPosBase = 0
    var yearListBase: ArrayList<String> = ArrayList()
    var dayListBase: ArrayList<String> = ArrayList()

    fun onNetworkChange(isConnected: Boolean){

    }

    fun callSectionAPI(apiName: String){

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        Handler().postDelayed(
            Runnable {
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finish()
            },2000
        )
    }


    public fun initPreference() {
        mPreferenceUtils = PreferenceUtils.getInstance(this)
    }

    override fun onPause() {
        super.onPause()
    }


}


















