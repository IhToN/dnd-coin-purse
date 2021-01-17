package com.atalgaba.ddcoinpurse.enums

import androidx.annotation.StringRes
import com.atalgaba.ddcoinpurse.R

enum class Currency(
    val value: Int,
    @StringRes val stringId: Int,
    @StringRes val shorthandId: Int,
    @StringRes val preferenceId: Int,
    @StringRes val preferenceUsageId: Int
) {
    COPPER(
        0,
        R.string.title_coin_copper,
        R.string.title_coin_copper_shorthand,
        R.string.preference_copper,
        R.string.preference_copper_usage
    ),
    SILVER(
        1,
        R.string.title_coin_silver,
        R.string.title_coin_silver_shorthand,
        R.string.preference_silver,
        R.string.preference_silver_usage
    ),
    ELECTRUM(
        2,
        R.string.title_coin_electrum,
        R.string.title_coin_electrum_shorthand,
        R.string.preference_electrum,
        R.string.preference_electrum_usage
    ),
    GOLD(
        3,
        R.string.title_coin_gold,
        R.string.title_coin_gold_shorthand,
        R.string.preference_gold,
        R.string.preference_gold_usage
    ),
    PLATINUM(
        4,
        R.string.title_coin_platinum,
        R.string.title_coin_platinum_shorthand,
        R.string.preference_platinum,
        R.string.preference_platinum_usage
    );

    /*
        Custom Attrs (attrs_currency.xml):
        <enum name="cp" value="0"/>
        <enum name="sp" value="1"/>
        <enum name="ep" value="2"/>
        <enum name="gp" value="3"/>
        <enum name="pp" value="4"/>
     */

    companion object {
        private val currencies = values().associateBy(Currency::value)

        fun fromValue(type: Int): Currency =
            currencies[type] ?: error("{$type} is not a valid currency.")
    }
}