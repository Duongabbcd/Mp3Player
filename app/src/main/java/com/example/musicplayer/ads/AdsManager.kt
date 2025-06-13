package com.statussaver.video.photo.dowloader.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.admob.max.dktlibrary.AdmobUtils
import com.admob.max.dktlibrary.AdmobUtils.NativeAdCallbackNew
import com.admob.max.dktlibrary.AdmobUtils.checkAdsTest
import com.admob.max.dktlibrary.AppOpenManager
import com.admob.max.dktlibrary.CollapsibleBanner
import com.admob.max.dktlibrary.GoogleENative
import com.admob.max.dktlibrary.utils.admod.BannerHolderAdmob
import com.admob.max.dktlibrary.utils.admod.InterHolderAdmob
import com.admob.max.dktlibrary.utils.admod.NativeHolderAdmob
import com.admob.max.dktlibrary.utils.admod.callback.AdCallBackInterLoad
import com.admob.max.dktlibrary.utils.admod.callback.AdsInterCallBack
import com.admob.max.dktlibrary.utils.admod.callback.NativeAdmobCallback
import com.applovin.sdk.AppLovinSdkUtils
import com.example.musicplayer.R
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd

object AdsManager {
    var isDebug = true
    var isTestDevice = false

    var BANNER_SPLASH = ""

    var AOA_SPLASH = ""
    var INTER_SPLASH = InterHolderAdmob("")
    var AOA_SPLASH_OPEN_NOTI = ""

    var INTER_SPLASH_OPEN_NOTI = InterHolderAdmob("")

    var NATIVE_LANGUAGE = NativeHolderAdmob("")
    var NATIVE_LANGUAGE_ID2 = NativeHolderAdmob("")
    var NATIVE_LANGUAGE_ID3 = NativeHolderAdmob("")
    var NATIVE_INTRO_3 = NativeHolderAdmob("")

    var INTER_SKIP_INTRO = InterHolderAdmob("")

    var NATIVE_EXPLORE_RECENT = NativeHolderAdmob("")
    var NATIVE_EXPLORE_IMAGES = NativeHolderAdmob("")
    var NATIVE_EXPLORE_VIDEOS = NativeHolderAdmob("")
    var NATIVE_EXPLORE_ARCHIVE = NativeHolderAdmob("")
    var NATIVE_SAVED = NativeHolderAdmob("")
    var NATIVE_RECOVERY = NativeHolderAdmob("")
    var NATIVE_SETTINGS = NativeHolderAdmob("")
    var NATIVE_SETTINGS_1 = NativeHolderAdmob("")
    var INTER_MESSAGES = InterHolderAdmob("")
    var INTER_BACK_MESSAGES = InterHolderAdmob("")
    var INTER_ITEM_CLICK = InterHolderAdmob("")

    var BANNER_MESSAGE_READER = ""
    var BANNER_MESSAGE_READER_1 = BannerHolderAdmob("")

    var BANNER_MEDIA_DETAILS = ""
    var BANNER_MEDIA_DETAILS_1 = BannerHolderAdmob("")

    var ONRESUME = ""

    var countClickMessage = 0
    var countClickMessageBack = 0
    var countClickItem = 0

    fun showAdBanner(activity: Activity, adsEnum: String, view: ViewGroup, line: View, isCheckTestDevice:Boolean= false ) {

        if (!AdmobUtils.isNetworkConnected(activity)) {
            view.visibility = View.GONE
            line.visibility = View.GONE
            return
        }
        AdmobUtils.loadAdBanner(activity, adsEnum, view, isCheckTestDevice, object :
            AdmobUtils.BannerCallBack {
            override fun onClickAds() {

            }

            override fun onFailed(message: String) {
                view.visibility = View.GONE
                line.visibility = View.GONE
            }

            override fun onLoad() {

            }


            override fun onPaid(adValue: AdValue?, mAdView: AdView?) {
            }

        })
    }

    fun showAdBannerCollapsible(
        activity: Activity,
        adsEnum: BannerHolderAdmob,
        view: ViewGroup,
        line: View,
        isCheckTestDevice:Boolean= false,
    ) {

        if (isTestDevice) {
            view.visibility = View.GONE
            line.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            view.visibility = View.GONE
            line.visibility = View.GONE
            return
        }
        AdmobUtils.loadAdBannerCollapsibleReload(
            activity,
            adsEnum,
            CollapsibleBanner.BOTTOM,
            view,
            isCheckTestDevice,
            object : AdmobUtils.BannerCollapsibleAdCallback {
                override fun onBannerAdLoaded(
                    adSize: AdSize
                ) {
                    val params: ViewGroup.LayoutParams =
                        view.layoutParams
                    params.height = adSize.getHeightInPixels(activity)
                    view.layoutParams = params
                }

                override fun onClickAds() {

                }

                override fun onAdFail(message: String) {
                    view.visibility = View.GONE
                    line.visibility = View.GONE

                }

                override fun onAdPaid(adValue: AdValue, mAdView: AdView) {
                }
            })
    }






    fun loadAndShowNative(
        activity: Activity,
        nativeAdContainer: ViewGroup,
        isCheckTestDevice:Boolean= false,
        NativeHolderAdmob: NativeHolderAdmob
    ) {

        if (!AdmobUtils.isNetworkConnected(activity)) {
            nativeAdContainer.visibility = View.GONE
            return
        }
        AdmobUtils.loadAndShowNativeAdsWithLayoutAds(
            activity,
            NativeHolderAdmob,
            nativeAdContainer,
            R.layout.ad_template_medium,
            GoogleENative.UNIFIED_MEDIUM,
            isCheckTestDevice,
            object : NativeAdCallbackNew {
                override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                    checkAdsTest(ad = ad)
                }

                override fun onNativeAdLoaded() {
                }

                override fun onAdFail(error: String) {
                    nativeAdContainer.visibility = View.GONE
                }

                override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {
                  //do nothing
                }


                override fun onClickAds() {

                }
            })
    }

    fun loadAndShowNativeCollapsible(
        activity: Activity,
        nativeHolderAdmob: NativeHolderAdmob,
        nativeAdContainer: ViewGroup,
        isCheckTestDevice: Boolean = false
    ) {
        try {
            if (!AdmobUtils.isNetworkConnected(activity)) {
                nativeAdContainer.visibility = View.GONE
                return
            }
            val params: ViewGroup.LayoutParams = nativeAdContainer.layoutParams
            params.height = AppLovinSdkUtils.dpToPx(activity,230)
            nativeAdContainer.layoutParams = params

            AdmobUtils.loadAndShowNativeAdsWithLayoutAdsCollapsible(activity, nativeHolderAdmob, nativeAdContainer,
                R.layout.ad_template_mediumcollapsible, GoogleENative.UNIFIED_MEDIUM, isCheckTestDevice,object : NativeAdCallbackNew {
                    override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                    }

                    override fun onNativeAdLoaded() {
                        val params: ViewGroup.LayoutParams = nativeAdContainer.layoutParams
                        params.height = AppLovinSdkUtils.dpToPx(activity,330)
                        nativeAdContainer.layoutParams = params
                    }

                    override fun onAdFail(error: String) {
                        nativeAdContainer.visibility = View.GONE
                    }

                    override fun onAdPaid(adValue: AdValue?, adUnitAds: String?) {

                    }

                    override fun onClickAds() {
                        val params: ViewGroup.LayoutParams = nativeAdContainer.layoutParams
                        params.height = AppLovinSdkUtils.dpToPx(activity,80)
                        nativeAdContainer.layoutParams = params
                    }
                })
        }catch (_:Exception){
            nativeAdContainer.visibility = View.GONE
        }catch (_:OutOfMemoryError){
            nativeAdContainer.visibility = View.GONE
        }
    }

    fun loadAndShowInterSplash(
        context: AppCompatActivity,
        interHolder: InterHolderAdmob,
        isdialog : Boolean,
        callback: AdListenerWithNative,
        isCheckTestDevice: Boolean =false
    ) {
        if (isTestDevice) {
            callback.onAdClosedOrFailed()
            return
        }
        if (!AdmobUtils.isNetworkConnected(context)) {
            callback.onAdClosedOrFailed()
            return
        }
        AppOpenManager.getInstance().isAppResumeEnabled = true
        AdmobUtils.loadAndShowAdInterstitial(context, interHolder, isCheckTestDevice, object : AdsInterCallBack {
            override fun onStartAction() {
            }

            override fun onEventClickAdClosed() {
                callback.onAdClosedOrFailedWithNative()
            }

            override fun onAdShowed() {
                AppOpenManager.getInstance().isAppResumeEnabled = false
                Handler().postDelayed({
                    try {
                        AdmobUtils.dismissAdDialog()
                    } catch (_: Exception) {

                    }
                }, 800)
            }

            override fun onAdLoaded() {

            }

            override fun onAdFail(p0: String?) {
                println("onAdFail 123: $p0")
                callback.onAdClosedOrFailedWithNative()
            }

            override fun onClickAds() {

            }

            override fun onPaid(p0: AdValue?, p1: String?) {

            }
        }, enableLoadingDialog = isdialog)
    }

    fun loadNative(context: Context, nativeHolder: NativeHolderAdmob,         isCheckTestDevice: Boolean =false) {
        if (isTestDevice) {
            return
        }
        if (!AdmobUtils.isNetworkConnected(context)) {
            return
        }
        AdmobUtils.loadAndGetNativeAds(context, nativeHolder, isCheckTestDevice, object : NativeAdmobCallback {
            override fun onLoadedAndGetNativeAd(ad: NativeAd?) {
                checkAdsTest(ad)
            }

            override fun onNativeAdLoaded() {

            }

            override fun onAdFail(error: String?) {
                Log.e("Admob", "onAdFail: ${nativeHolder.ads}" + error)
            }

            override fun onPaid(p0: AdValue?, p1: String?) {

            }
        })
    }

    fun showNativeLanguage(activity: Activity, viewGroup: ViewGroup, holder: NativeHolderAdmob,         isCheckTestDevice: Boolean =false) {
        if (isTestDevice) {
            viewGroup.visibility = View.GONE
            return
        }
        if (!AdmobUtils.isNetworkConnected(activity)) {
            viewGroup.visibility = View.GONE
            return
        }

        AdmobUtils.showNativeAdsWithLayout(activity,
            holder,
            viewGroup,
            R.layout.ad_template_medium,
            GoogleENative.UNIFIED_MEDIUM,
            isCheckTestDevice,
            object : AdmobUtils.AdsNativeCallBackAdmod {
                override fun NativeLoaded() {
                    viewGroup.visibility = View.VISIBLE
                }

                override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                }

                override fun NativeFailed(massage: String) {
                    AdmobUtils.showNativeAdsWithLayout(activity,
                        holder,
                        viewGroup,
                        R.layout.ad_template_medium,
                        GoogleENative.UNIFIED_MEDIUM,
                        isCheckTestDevice,
                        object : AdmobUtils.AdsNativeCallBackAdmod {
                            override fun NativeLoaded() {
                                viewGroup.visibility = View.VISIBLE
                            }

                            override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                            }

                            override fun NativeFailed(massage: String) {
                                viewGroup.visibility = View.GONE
                            }

                        })
                }

            })
    }

    fun showAdNativeLanguage(activity: Activity, nativeAdContainer: ViewGroup, native: NativeHolderAdmob,
                             checkTestAd: Boolean = false,        isCheckTestDevice: Boolean =false, callback: () -> Unit,
                     ) {
        if (!AdmobUtils.isNetworkConnected(activity)) {
            callback.invoke()
            nativeAdContainer.visibility = View.GONE
            return
        }
        if(checkTestAd){
            if(isTestDevice){
                callback.invoke()
                nativeAdContainer.visibility = View.GONE
                return
            }
        }
        try {
            AdmobUtils.showNativeAdsWithLayout(
                activity,
                native,
                nativeAdContainer,
                R.layout.ad_template_medium,
                GoogleENative.UNIFIED_MEDIUM, isCheckTestDevice, object : AdmobUtils.AdsNativeCallBackAdmod {
                    override fun NativeLoaded() {
                        callback.invoke()
                    }

                    override fun onPaid(adValue: AdValue?, adUnitAds: String?) {

                    }

                    override fun NativeFailed(massage: String) {
                        callback.invoke()
                        nativeAdContainer.visibility = View.GONE
                        Log.d("NativeFailed", "NativeFailed: $massage")
                    }

                }
            )
        }catch (e: Exception){
            nativeAdContainer.visibility = View.GONE
        }
    }

    fun loadAndShowInterSP2(
        context: Context,
        interHolder: InterHolderAdmob,
        type: String,
        callback: AdListenerWithNative,
        isCheckTestDevice: Boolean =false
    ) {
        if (isTestDevice) {
            callback.onAdClosedOrFailed()
            return
        }
        if (!AdmobUtils.isNetworkConnected(context)) {
            callback.onAdClosedOrFailed()
            return
        }
        when (type) {
            "INTER_SKIP_INTRO" -> {
                if (RemoteConfig.inter_skip_intro_160425 == "0") {
                    callback.onAdClosedOrFailed()
                    return
                }
                val x: Int = try {
                    RemoteConfig.inter_skip_intro_160425.toInt()
                } catch (e: Exception) {
                    10
                }
                if (RemoteConfig.countInterSkip % x == 0) {
                    RemoteConfig.countInterSkip++
                } else {
                    RemoteConfig.countInterSkip++
                    callback.onAdClosedOrFailed()
                    return
                }
            }

            "INTER_MESSAGES" -> {
                if (RemoteConfig.inter_messages_160425 == "0") {
                    callback.onAdClosedOrFailed()
                    return
                }
                countClickMessage ++

                if (countClickMessage % RemoteConfig.inter_messages_160425.toInt()!=0){
                    callback.onAdClosedOrFailed()
                    return
                }

            }
            "INTER_BACK_MESSAGES" -> {
                if (RemoteConfig.inter_back_messages_160425 == "0") {
                    callback.onAdClosedOrFailed()
                    return
                }
                countClickMessageBack ++
                if (countClickMessageBack % RemoteConfig.inter_back_messages_160425.toInt()!=0){
                    callback.onAdClosedOrFailed()
                    return
                }
            }

            "INTER_ITEM_CLICK" ->  {
                if (RemoteConfig.inter_item_click160425 == "0") {
                    callback.onAdClosedOrFailed()
                    return
                }
                countClickItem ++
                if (countClickItem % RemoteConfig.inter_item_click160425.toInt()!=0){
                    callback.onAdClosedOrFailed()
                    return
                }
            }

        }

        AppOpenManager.getInstance().isAppResumeEnabled = true
        AdmobUtils.loadAndShowAdInterstitial(
            context as AppCompatActivity,
            interHolder,
            isCheckTestDevice,
            object : AdsInterCallBack {
                override fun onStartAction() {
                    println("onStartAction")
                }

                override fun onEventClickAdClosed() {
                    println("onEventClickAdClosed")
                    callback.onAdClosedOrFailedWithNative()
                }

                override fun onAdShowed() {
                    AppOpenManager.getInstance().isAppResumeEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            AdmobUtils.dismissAdDialog()
                        } catch (_: Exception) {

                        }
                    }, 800)
                }

                override fun onAdLoaded() {

                }

                override fun onAdFail(p0: String?) {
                    println("onAdFail")
                    callback.onAdClosedOrFailed()
                }

                override fun onClickAds() {

                }

                override fun onPaid(p0: AdValue?, p1: String?) {
                }
            },
            true
        )
    }


    fun showAdBannerWithCallBack(
        activity: Context,
        adsEnum: String,
        view: ViewGroup,
        line: View,
        isCheckTestDevice: Boolean,
        callback: () -> Unit
    ) {
        if (AdmobUtils.isNetworkConnected(activity)) {
            AdmobUtils.loadAdBanner(
                activity as Activity,
                adsEnum,
                view,
                isCheckTestDevice = isCheckTestDevice,
                object : AdmobUtils.BannerCallBack {
                    override fun onLoad() {
                        view.visibility =
                            View.VISIBLE
                        line.visibility = View.VISIBLE
                        callback.invoke()
                    }

                    override fun onClickAds() {}
                    override fun onFailed(message: String) {
                        view.visibility =
                            View.GONE
                        line.visibility = View.GONE
                        callback.invoke()
                    }

                    override fun onPaid(adValue: AdValue?, mAdView: AdView?) {}
                })
        } else {
            view.visibility = View.GONE
            line.visibility = View.GONE
        }
    }

    interface AdListenerWithNative {
        fun onAdClosedOrFailed()
        fun onAdClosedOrFailedWithNative()
    }
}