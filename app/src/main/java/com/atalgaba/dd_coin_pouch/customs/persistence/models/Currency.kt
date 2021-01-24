package com.atalgaba.dd_coin_pouch.customs.persistence.models

import android.content.Context
import com.abubusoft.kripton.android.annotation.BindSqlType
import com.abubusoft.kripton.android.sqlite.TransactionResult
import com.atalgaba.dd_coin_pouch.customs.objects.Currencies
import com.atalgaba.dd_coin_pouch.customs.persistence.data_sources.BindCurrencyDataSource
import com.atalgaba.dd_coin_pouch.helpers.ResourcesHelper

@BindSqlType(name = "currencies")
class Currency(
    var id: Long?,
    var nameId: String,
    var codeId: String,
    var enabled: Boolean,
    var value: Int = 1,
    var quantity: Int = 0
) {
    fun update() {
        BindCurrencyDataSource.open().execute { daoFactory ->
            {
                daoFactory.currencyDAO.update(this)
                Currencies.executeOnCurrencyUpdateListeners()
                TransactionResult.COMMIT
            }()
        }
    }

    fun getName(context: Context) = ResourcesHelper.getStringResourceByName(context, nameId)
    fun getCode(context: Context) = ResourcesHelper.getStringResourceByName(context, codeId)
}