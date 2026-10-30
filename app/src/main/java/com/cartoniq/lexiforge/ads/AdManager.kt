package com.cartoniq.lexiforge.ads

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/** Google's official AdMob TEST unit IDs. Replace before publishing. */
object AdUnits {
    const val BANNER = "ca-app-pub-9771662959477951/7260637718"
    const val INTERSTITIAL = "ca-app-pub-9771662959477951/7260637718"
    const val REWARDED = "ca-app-pub-9771662959477951/7260637718"
}

class AdManager(private val context: Context) {

    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null

    var rewardedReady by mutableStateOf(false)
        private set

    fun initialize() {
        MobileAds.initialize(context)
        loadInterstitial()
        loadRewarded()
    }

    private fun loadInterstitial() {
        InterstitialAd.load(
            context,
            AdUnits.INTERSTITIAL,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                }
            }
        )
    }

    private fun loadRewarded() {
        RewardedAd.load(
            context,
            AdUnits.REWARDED,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    rewardedReady = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                    rewardedReady = false
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
        val ad = interstitial
        if (ad == null) {
            loadInterstitial()
            onClosed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                loadInterstitial()
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitial = null
                loadInterstitial()
                onClosed()
            }
        }
        ad.show(activity)
    }

    fun showRewarded(activity: Activity, onReward: () -> Unit, onClosed: () -> Unit) {
        val ad = rewarded
        if (ad == null) {
            loadRewarded()
            onClosed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                rewardedReady = false
                loadRewarded()
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewarded = null
                rewardedReady = false
                loadRewarded()
                onClosed()
            }
        }
        ad.show(activity, OnUserEarnedRewardListener { onReward() })
    }
}
