package com.atalgaba.dd_coin_pouch.customs.persistence.data_sources

import com.abubusoft.kripton.android.annotation.BindDataSource
import com.atalgaba.dd_coin_pouch.customs.persistence.daos.CurrencyDAO

@BindDataSource(daoSet = [CurrencyDAO::class], fileName = "currency.db", log=true)
interface CurrencyDataSource