package com.atalgaba.dd_coin_purse.customs.persistence.daos

import com.abubusoft.kripton.android.annotation.*
import com.atalgaba.dd_coin_purse.customs.persistence.models.Currency


@BindDao(Currency::class)
interface CurrencyDAO {
    @BindSqlSelect(orderBy = "value")
    fun selectAll(): List<Currency>

    @BindSqlSelect(orderBy = "value")
    fun selectEnabled(): List<Currency>

    @BindSqlSelect(where = "codeId=:codeId")
    fun selectByCodeId(codeId: Int): List<Currency>

    @BindSqlSelect(where = "nameId=:nameId")
    fun selectByNameId(nameId: Int): List<Currency>

    @BindSqlInsert
    fun insert(bean: Currency)

    @BindSqlInsert
    fun insert(bean: List<Currency>)

    @BindSqlUpdate(where = "id=:bean.id")
    fun update(bean: Currency)

    @BindSqlDelete
    fun clearCurrencies()
}