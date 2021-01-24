package com.atalgaba.dd_coin_pouch.helpers

import android.app.Activity
import android.content.Context
import com.atalgaba.dd_coin_pouch.R

object AdsHelper {
    private var activity: Activity? = null

    private var _areAdsEnabled: Boolean = true

    var areAdsEnabled: Boolean
        get() = _areAdsEnabled
        set(value) {
            _areAdsEnabled = value
            if (activity !== null) {
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                with(sharedPref.edit()) {
                    putBoolean(activity?.getString(R.string.preference_ads_enabled), value)
                    apply()
                }
            }
        }

    fun initialize(activity: Activity) {
        this.activity = activity
    }
}