package com.atalgaba.dd_coin_purse.helpers

import android.content.Context
import com.atalgaba.dd_coin_purse.BuildConfig

object ResourcesHelper {
    fun getStringResourceByName(context: Context, string: String): String {
        val packageName = context.packageName
        val resId = context.resources.getIdentifier(string, "string", packageName)
        return context.resources.getString(resId)
    }
}