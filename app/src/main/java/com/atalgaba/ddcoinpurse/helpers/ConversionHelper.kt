package com.atalgaba.ddcoinpurse.helpers

import com.atalgaba.ddcoinpurse.enums.Currency

object ConversionHelper {
    val conversionTable: Map<Currency, Map<Currency, Double>> =
        mapOf(
            Currency.COPPER to mapOf(
                Currency.COPPER to 1.0,
                Currency.SILVER to 0.1,
                Currency.ELECTRUM to 0.02,
                Currency.GOLD to 0.01,
                Currency.PLATINUM to 0.001
            ),
            Currency.SILVER to mapOf(
                Currency.COPPER to 10.0,
                Currency.SILVER to 1.0,
                Currency.ELECTRUM to 0.2,
                Currency.GOLD to 0.1,
                Currency.PLATINUM to 0.01
            ),
            Currency.ELECTRUM to mapOf(
                Currency.COPPER to 50.0,
                Currency.SILVER to 5.0,
                Currency.ELECTRUM to 1.0,
                Currency.GOLD to 0.5,
                Currency.PLATINUM to 0.05
            ),
            Currency.GOLD to mapOf(
                Currency.COPPER to 100.0,
                Currency.SILVER to 10.0,
                Currency.ELECTRUM to 2.0,
                Currency.GOLD to 1.0,
                Currency.PLATINUM to 0.1
            ),
            Currency.PLATINUM to mapOf(
                Currency.COPPER to 1000.0,
                Currency.SILVER to 100.0,
                Currency.ELECTRUM to 20.0,
                Currency.GOLD to 10.0,
                Currency.PLATINUM to 1.0
            )
        )

    fun convert(coins: Double, from: Currency, to: Currency): Double = coins * (conversionTable[from]?.get(to) ?: 0.0)
}