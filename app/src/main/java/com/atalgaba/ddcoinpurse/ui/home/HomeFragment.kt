package com.atalgaba.ddcoinpurse.ui.home

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.atalgaba.ddcoinpurse.R
import com.atalgaba.ddcoinpurse.enums.Currency
import com.atalgaba.ddcoinpurse.helpers.CurrencyHelper
import com.atalgaba.ddcoinpurse.ui.components.CurrencyView

class HomeFragment : Fragment() {

    private lateinit var preferences: SharedPreferences
    private lateinit var mActivity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_purse, container, false)

        val purse: LinearLayout = root.findViewById(R.id.purse_list)

            CurrencyHelper.getEnabledCurrencies(mActivity).forEach { currency: Currency ->
                run {
                    val currencyView = CurrencyView(mActivity)
                    currencyView.currency = currency
                    currencyView.quantity = CurrencyHelper.getCoins(mActivity, currency)

                    purse.addView(currencyView)
                }
            }


        return root
    }
}