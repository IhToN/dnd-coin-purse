package com.atalgaba.ddcoinpurse.helpers

import android.app.Activity
import android.content.Context
import com.atalgaba.ddcoinpurse.R
import com.atalgaba.ddcoinpurse.enums.Currency
import com.atalgaba.ddcoinpurse.ui.components.CurrencyView

object CurrencyHelper {
    fun setCoins(activity: Activity, currency: Currency, quantity: Int) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit())
        {
            putInt(activity.getString(currency.preferenceId), quantity)
            apply()
        }
    }


    fun getCoins(activity: Activity, currency: Currency) =
        activity.getPreferences(Context.MODE_PRIVATE)
            .getInt(activity.getString(currency.preferenceId), 0)

    fun getCurrencies(activity: Activity): Map<Currency, Int> =
        Currency.values().associateBy({ it }, { getCoins(activity, it) })

    fun isCurrencyEnabled(activity: Activity, currency: Currency) =
        activity.getPreferences(Context.MODE_PRIVATE)
            .getBoolean(activity.getString(currency.preferenceUsageId), true)

    fun getEnabledCurrencies(activity: Activity): List<Currency> =
        Currency.values().filter { currency -> isCurrencyEnabled(activity, currency) }

    fun getCurrencyView(activity: Activity, currency: Currency): CurrencyView {
        val currencyView = CurrencyView(activity)
        currencyView.currency = currency
        currencyView.quantity = getCoins(activity, currency)

        return currencyView
    }
}