package com.atalgaba.dd_coin_pouch.helpers

import com.atalgaba.dd_coin_pouch.customs.persistence.models.Currency

object SavedInstanceHelper {
    var conversionBaseCurrencyQuantity: Double = 1.0

    var calculatorCurrencies: MutableList<Pair<Currency, Int?>>? = null
    var calculatorPlayers: Int = 1

}