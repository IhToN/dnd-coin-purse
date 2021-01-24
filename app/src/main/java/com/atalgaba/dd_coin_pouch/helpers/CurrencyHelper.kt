package com.atalgaba.dd_coin_pouch.helpers

import android.app.Activity
import android.util.Log
import com.atalgaba.dd_coin_pouch.customs.persistence.models.Currency
import com.atalgaba.dd_coin_pouch.ui.components.CurrencyView
import kotlin.math.floor

object CurrencyHelper {
    const val TAG = "CurrencyHelper"
//    fun setCoins(activity: Activity, currency: Currency, quantity: Int) {
//        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
//        with(sharedPref.edit())
//        {
//            putInt(activity.getString(currency.preferenceId), quantity)
//            apply()
//        }
//    }
//
//
//    fun getCoins(activity: Activity, currency: Currency) =
//        activity.getPreferences(Context.MODE_PRIVATE)
//            .getInt(activity.getString(currency.preferenceId), 0)
//
//    fun getCurrencies(activity: Activity): Map<Currency, Int> =
//        Currency.values().associateBy({ it }, { getCoins(activity, it) })
//
//    fun isCurrencyEnabled(activity: Activity, currency: Currency) =
//        activity.getPreferences(Context.MODE_PRIVATE)
//            .getBoolean(activity.getString(currency.preferenceUsageId), true)
//
//    fun getEnabledCurrencies(activity: Activity): List<Currency> =
//        Currency.values().filter { currency -> isCurrencyEnabled(activity, currency) }

    fun getTotalBaseCoins(currencies: List<Currency>) = currencies.sumBy { it.value * it.quantity }

    fun optimizePouch(currencies: List<Currency>) {
        var totalCoins = getTotalBaseCoins(currencies)
        currencies.sortedByDescending { it.value }.forEach { currency ->
            currency.quantity = floor((totalCoins / currency.value).toDouble()).toInt()
            totalCoins -= currency.quantity * currency.value
            currency.update()
        }
        Log.d(TAG, "Total Coins $currencies")
    }

    fun getCurrencyView(activity: Activity, currency: Currency): CurrencyView {
        val currencyView = CurrencyView(activity)
        currencyView.currency = currency
        currencyView.quantity = currency.quantity

        return currencyView
    }
}