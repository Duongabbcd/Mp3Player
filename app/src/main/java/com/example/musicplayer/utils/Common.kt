package com.example.musicplayer.utils

import android.app.Activity
import android.app.AlertDialog
import android.app.Application.MODE_MULTI_PROCESS
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.admob.max.dktlibrary.AppOpenManager
import com.example.musicplayer.BuildConfig
import com.example.musicplayer.R
import com.example.musicplayer.screen.home.MainActivity
import com.example.ratingdialog.RatingDialog
import java.util.Locale

object Common {
    var countDone = 0
    var countShowRate = 0

    var isShowRate = false
    var showTime = 0

    var isEnableClick = true

    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.inVisible() {
        visibility = View.INVISIBLE
    }

    fun View.gone() {
        visibility = View.GONE
    }

    fun Context.toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
//
//    fun scheduleNotification(context: Context, time: Long) {
//        val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
//            .setInitialDelay(time, TimeUnit.SECONDS)
//            .build()
//
//        WorkManager.getInstance(context).enqueue(notificationRequest)
//    }

    fun getLang(mContext: Context): String {
        val preferences =
            mContext.getSharedPreferences(mContext.packageName, Context.MODE_MULTI_PROCESS)
        return preferences.getString("KEY_LANG", "en") ?: "English (UK)"
    }

    fun setLang(context: Context, open: String?) {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_MULTI_PROCESS)
        preferences.edit().putString("KEY_LANG", open).apply()
    }

    fun getPreLanguageflag(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(mContext.packageName, MODE_MULTI_PROCESS)
        return preferences.getInt("KEY_FLAG", R.drawable.english)
    }

    fun setPreLanguageflag(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putInt("KEY_FLAG", flag).apply()
    }

    fun getPreLanguage(mContext: Context): String {
        val preferences = mContext.getSharedPreferences(mContext.packageName, MODE_MULTI_PROCESS)
        return preferences.getString("KEY_LANGUAGE", "en").toString()
    }

    fun setPreLanguage(context: Context, language: String?) {
        if (TextUtils.isEmpty(language)) return
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putString("KEY_LANGUAGE", language).apply()
    }

    fun setPosition(context: Context, open: Int) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putInt("KEY_POSITION", open).apply()
    }

    fun getPosition(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(mContext.packageName, MODE_MULTI_PROCESS)
        return preferences.getInt("KEY_POSITION", -1)
    }

    fun setLocale(context: Context, lang: String?) {
        val myLocale = lang?.let { Locale(it) }
        val res = context.resources
        val dm = res.displayMetrics
        val conf = res.configuration
        conf.setLocale(myLocale)
        res.updateConfiguration(conf, dm)
    }

    fun setRemoteKey(context: Context, key: String, value: String) {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_MULTI_PROCESS)
        preferences.edit().putString(key, value).apply()
    }

    fun getRemoteKey(context: Context, key: String, default: String): String {
        val preferences =
            context.getSharedPreferences(context.packageName, Context.MODE_MULTI_PROCESS)
        return preferences.getString(key, default).toString()
    }

     fun showRate(context: Activity) {
        val ratingDialog1 = RatingDialog.Builder(context).session(1).date(1).ignoreRated(false)
            .setNameApp(context.resources.getString(R.string.app_name)).setIcon(R.mipmap.ic_launcher_round)
            .setEmail("nguyenhuuchinh.031019931@gmail.com").isShowButtonLater(true).isClickLaterDismiss(true)
            .setTextButtonLater("Maybe Later").setOnlickMaybeLate {
                AppOpenManager.getInstance().enableAppResumeWithActivity(MainActivity::class.java)
            }.setOnlickRate {
                AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity::class.java)
            }.setDeviceInfo(
                BuildConfig.VERSION_NAME,
                Build.VERSION.SDK_INT.toString(),
                Build.MANUFACTURER + "_" + Build.MODEL
            ) .ratingButtonColor(Color.parseColor("#004BBB"))
            .build()
        ratingDialog1 . setCanceledOnTouchOutside (false)
        ratingDialog1 . show ()
    }

    fun logEventFirebase(context: Context, eventName: String) {
//        val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
//        val bundle = Bundle()
//        bundle.putString("onEvent", context.javaClass.simpleName)
//        firebaseAnalytics.logEvent(eventName + "_" + BuildConfig.VERSION_CODE, bundle)
//        Log.d("===Event", eventName + "_" + BuildConfig.VERSION_CODE)
    }

    fun getCountOpenApp(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_CountOpenApp", 0)
    }

    fun setCountOpenApp(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_CountOpenApp", flag).apply()
    }

    fun getCountRate(mContext: Context): Int {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getInt("KEY_CountRate", 0)
    }

    fun setCountRate(context: Context, flag: Int) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putInt("KEY_CountRate", flag).apply()
    }

    fun getFirstUse2(mContext: Context): Boolean {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getBoolean("KEY_FirstUse2", true)
    }

    fun setFirstUse2(context: Context, isFirstUse: Boolean) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putBoolean("KEY_FirstUse2", isFirstUse).apply()
    }


    fun getFirstUse(mContext: Context): Boolean {
        val preferences = mContext.getSharedPreferences(
            mContext.packageName,
            Context.MODE_MULTI_PROCESS
        )
        return preferences.getBoolean("KEY_FirstUse", true)
    }

    fun setFirstUse(context: Context, isFirstUse: Boolean) {
        val preferences = context.getSharedPreferences(
            context.packageName,
            Context.MODE_MULTI_PROCESS
        )
        preferences.edit().putBoolean("KEY_FirstUse", isFirstUse).apply()
    }


    fun showDialogGoToSetting(context: Context, onClickListener: (Boolean) -> Unit) {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(R.string.title_grant_Permission)
        alertDialog.setMessage(context.getString(R.string.message_grant_Permission))
        alertDialog.setCancelable(true)
        alertDialog.setButton(
            DialogInterface.BUTTON_POSITIVE, context.getString(R.string.goto_setting)
        ) { _: DialogInterface?, _: Int ->
            onClickListener(true)
            alertDialog.dismiss()
        }

        // Negative button: Cancel
        alertDialog.setButton(
            DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel)
        ) { _: DialogInterface?, _: Int ->
            onClickListener(false)
            alertDialog.dismiss()
        }

        // Handle cancel (e.g., tapped outside or pressed back)
        alertDialog.setOnCancelListener {
            onClickListener(false)
        }

        alertDialog.show ()

        // Set background to white
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))

        // Set positive button text color to black
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
        // Set title and message text color to black
        val titleId = context.resources.getIdentifier("alertTitle", "id", "android")
        val messageId = android.R.id.message

        alertDialog.findViewById<TextView>(titleId)?.setTextColor(Color.BLACK)
        alertDialog.findViewById<TextView>(messageId)?.setTextColor(Color.BLACK)
    }

    fun setCountDownTime(context: Context, totalTime: Long) {
        val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
        preferences.edit().putLong("KEY_COUNTDOWN", totalTime).apply()
    }

  fun getCountDownTime(context: Context)  : Long{
      val preferences = context.getSharedPreferences(context.packageName, MODE_MULTI_PROCESS)
      return preferences.getLong("KEY_COUNTDOWN", 0L)
    }


}