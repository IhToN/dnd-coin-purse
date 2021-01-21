package com.atalgaba.dd_coin_purse.customs.objects

import android.util.Log
import com.abubusoft.kripton.android.sqlite.TransactionResult
import com.atalgaba.dd_coin_purse.BuildConfig
import com.atalgaba.dd_coin_purse.R
import com.atalgaba.dd_coin_purse.customs.persistence.data_sources.BindCurrencyDataSource
import com.atalgaba.dd_coin_purse.customs.persistence.models.Currency

object Currencies {
    const val TAG = "Currencies"

    private val list: List<Currency> = getCurrencies()

    private val ON_CURRENCY_UPDATE_LISTENERS: MutableSet<OnCurrencyUpdateListener> = mutableSetOf()

    val available: List<Currency>
        get() = list

    val enabled: List<Currency>
        get() = list.filter { it.enabled }

    private fun getDefaultCurrencies(): List<Currency> {
        val currencies: MutableList<Currency> = mutableListOf()

        currencies.add(
            Currency(
                1,
                "title_coin_copper",
                "title_coin_copper_code",
                true,
                1,
                0
            )
        )
        currencies.add(
            Currency(
                1,
                "title_coin_silver",
                "title_coin_silver_code",
                true,
                10,
                0
            )
        )
        currencies.add(
            Currency(
                1,
                "title_coin_electrum",
                "title_coin_electrum_code",
                true,
                50,
                0
            )
        )
        currencies.add(
            Currency(
                1,
                "title_coin_gold",
                "title_coin_gold_code",
                true,
                100,
                0
            )
        )
        currencies.add(
            Currency(
                1,
                "title_coin_platinum",
                "title_coin_platinum_code",
                true,
                1000,
                0
            )
        )

        return currencies.toList()
    }

    private fun getCurrencies(): List<Currency> {
        var currencies: List<Currency> = arrayListOf()
        BindCurrencyDataSource.getInstance().execute { daoFactory ->
            if(BuildConfig.DEBUG) {
                Log.d(TAG, "Removing current currencies.")
                daoFactory.currencyDAO.clearCurrencies()
            }

            currencies = daoFactory.currencyDAO.selectAll()
            if (currencies.isEmpty()) {
                currencies = getDefaultCurrencies()
                daoFactory.currencyDAO.insert(currencies)
            }
            TransactionResult.COMMIT
        }
        return currencies
    }

    // region Listeners
    fun registerOnCurrencyUpdateListener(listener: OnCurrencyUpdateListener) = ON_CURRENCY_UPDATE_LISTENERS.add(listener)

    fun unregisterOnCurrencyUpdateListener(listener: OnCurrencyUpdateListener) = ON_CURRENCY_UPDATE_LISTENERS.remove(listener)

    fun executeOnCurrencyUpdateListeners() = ON_CURRENCY_UPDATE_LISTENERS.forEach { it.onUpdate() }
    // endregion

    interface OnCurrencyUpdateListener {
        fun onUpdate()
    }
}