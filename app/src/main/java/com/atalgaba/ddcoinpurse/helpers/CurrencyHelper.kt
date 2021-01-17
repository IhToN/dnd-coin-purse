package com.atalgaba.ddcoinpurse.helpers

import android.app.Activity
import android.content.Context
import com.atalgaba.ddcoinpurse.R
import com.atalgaba.ddcoinpurse.enums.Currency

object CurrencyHelper {
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
}