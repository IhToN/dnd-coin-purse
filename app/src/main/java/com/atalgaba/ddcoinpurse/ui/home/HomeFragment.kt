package com.atalgaba.ddcoinpurse.ui.home

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.atalgaba.ddcoinpurse.R
import com.atalgaba.ddcoinpurse.customs.inputs.InputFilterIntBetween
import com.atalgaba.ddcoinpurse.enums.Currency
import com.atalgaba.ddcoinpurse.helpers.CurrencyHelper
import com.atalgaba.ddcoinpurse.ui.components.CurrencyView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.max

class HomeFragment : Fragment(), OnSharedPreferenceChangeListener {

    private lateinit var mActivity: Activity
    private lateinit var mView: View

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivity = requireActivity()
        sharedPreferences = mActivity.getPreferences(Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_purse, container, false)
        mView = root

        refreshPurse()

        return root
    }

    private fun refreshPurse() {
        val purse: LinearLayout = mView.findViewById(R.id.purse_list)

        purse.removeAllViews()

        CurrencyHelper.getEnabledCurrencies(mActivity).forEach { currency: Currency ->
            run {
                purse.addView(getCurrencyView(currency))
            }
        }
    }

    private fun getCurrencyView(currency: Currency): CurrencyView {
        val currencyView = CurrencyHelper.getCurrencyView(mActivity, currency)

        currencyView.isFocusable = true
        currencyView.isClickable = true
        currencyView.isLongClickable = true

        currencyView.setOnClickListener {
            val inflater = LayoutInflater.from(mActivity)
            val coinDialogView = inflater.inflate(R.layout.dialog_edit_coin, null)

            val textInputEditText: TextInputEditText? =
                coinDialogView.findViewById(R.id.coin_text_field)
            textInputEditText?.filters = arrayOf(InputFilterIntBetween(0))

            MaterialAlertDialogBuilder(mActivity)
                .setView(coinDialogView)
                .setTitle(resources.getString(currency.stringId))
                .setPositiveButton(resources.getString(R.string.action_add)) { _, _ ->
                    // Add Coins

                    val currentCoins = CurrencyHelper.getCoins(mActivity, currency)
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0
                    val newCoins = currentCoins + inputCoins

                    Log.d(TAG, "New coins: $currentCoins + $inputCoins = $newCoins")

                    CurrencyHelper.setCoins(mActivity, currency, newCoins)
                }
                .setNegativeButton(resources.getString(R.string.action_remove)) { _, _ ->
                    // Remove Coins

                    val currentCoins = CurrencyHelper.getCoins(mActivity, currency)
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0
                    val newCoins = max(currentCoins - inputCoins, 0)

                    Log.d(TAG, "New coins: $currentCoins + $inputCoins = $newCoins")

                    CurrencyHelper.setCoins(mActivity, currency, newCoins)
                }
                .setNeutralButton(resources.getString(R.string.action_overwrite)) { _, _ ->
                    // Overwrite Coins

                    val currentCoins = CurrencyHelper.getCoins(mActivity, currency)
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0

                    Log.d(TAG, "New coins: $currentCoins => $inputCoins")

                    CurrencyHelper.setCoins(mActivity, currency, inputCoins)
                }
                .show()
            true
        }

        currencyView.setOnLongClickListener {
            Log.d("HOLI", "Moneda pizá un montón")
            true
        }

        return currencyView
    }

    companion object {
        val TAG: String = "HomeFragment"
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d(TAG, "Shared Preferences changed")
        refreshPurse()
    }
}