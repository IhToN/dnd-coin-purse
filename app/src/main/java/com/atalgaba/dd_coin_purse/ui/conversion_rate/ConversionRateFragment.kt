package com.atalgaba.dd_coin_purse.ui.conversion_rate

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.atalgaba.dd_coin_purse.R
import com.atalgaba.dd_coin_purse.customs.objects.Currencies
import com.atalgaba.dd_coin_purse.customs.persistence.models.Currency
import com.atalgaba.dd_coin_purse.databinding.FragmentConversionRateBinding
import com.atalgaba.dd_coin_purse.ui.components.ConversionCurrencyItemView
import com.atalgaba.dd_coin_purse.ui.purse.PurseFragment
import java.lang.Exception

class ConversionRateFragment : Fragment(), Currencies.OnCurrencyUpdateListener {
    companion object {
        const val TAG: String = "ConversionRateFragment"
    }

    private var _binding: FragmentConversionRateBinding? = null

    // todo: check performance optimization

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mActivity: Activity
    private lateinit var mView: View

    private lateinit var currencies: List<Pair<Currency, Double>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversionRateBinding.inflate(inflater, container, false)
        mView = binding.root

        Currencies.registerOnCurrencyUpdateListener(this)

        initializeCurrencies()

        return mView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Currencies.unregisterOnCurrencyUpdateListener(this)
        _binding = null
    }

    private fun initializeCurrencies() {
        currencies =
            if (this::currencies.isInitialized)
                Currencies.enabled
                    .toMutableList()
                    .map {
                        it to (currencies.firstOrNull { (currency, _) -> currency == it }?.second
                            ?: 0.0)
                    }
            else
                Currencies.enabled.toMutableList().map { it to 0.0 }

        val mainCurrency =
            currencies.firstOrNull { (_, quantity) -> quantity > 0 } ?: currencies.first()
        calculateCurrencies(
            mainCurrency.first,
            if (mainCurrency.second > 0) mainCurrency.second else 1.0
        )
    }

    private fun calculateCurrencies(from: Currency, value: Double) {
        currencies = currencies.map { (currency, _) ->
            when (currency) {
                from -> currency to value // retain
                else -> currency to (value * from.value / currency.value) // replace
            }
        }
        refreshTable(from)
    }

    private fun refreshTable(filterCurrency: Currency? = null, clearTable: Boolean = false) {
        val list: LinearLayout = mView.findViewById(R.id.conversion_rate_list)

        if (clearTable) {
            initializeCurrencies()
            list.removeAllViews()
        }

        if (list.childCount == 0) {
            currencies.forEach { (currency, quantity) ->
                run {
                    list.addView(getConversionCurrencyItemView(currency, quantity))
                }
            }
        } else {
            list.children.forEach {
                if (it is ConversionCurrencyItemView && it.currency != filterCurrency) {
                    val currencyPair: Pair<Currency, Double>? =
                        currencies.firstOrNull { (currency, _) ->
                            currency == it.currency
                        }
                    if (currencyPair != null) {
                        it.quantity = currencyPair.second
                    }
                }
            }
        }
    }

    private fun getConversionCurrencyItemView(
        currency: Currency,
        quantity: Double
    ): ConversionCurrencyItemView {
        val conversionCurrencyItemView = ConversionCurrencyItemView(mActivity)
        conversionCurrencyItemView.currency = currency
        conversionCurrencyItemView.quantity = quantity

        conversionCurrencyItemView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val cursorPosition: Int =
                    conversionCurrencyItemView.quantityTextView?.editText?.selectionEnd ?: 0
                var newValue = "0.0"
                if (!s.isNullOrEmpty()) {
                    newValue = s.toString()
                }

                if (conversionCurrencyItemView.hasFocus()) {
                    calculateCurrencies(currency, newValue.toDouble())
                } else {
                    try {
                        conversionCurrencyItemView.removeTextChangedListener(this)

                        if (newValue != "") {
                            val vals = String.format("%.3f", newValue.replace(",", "").toDouble())
                                .split('.').toMutableList()
                            vals[0] = vals[0].trimStart('0').padStart(1, '0')
                            vals[1] = vals[1].trimEnd('0')
                            val parsedValue =
                                vals.joinToString(".").trimEnd('.')
                            conversionCurrencyItemView.setText(parsedValue)

                            val diff = parsedValue.length - newValue.length
                            conversionCurrencyItemView.quantityTextView?.editText?.setSelection(
                                cursorPosition + diff
                            )
                        }
                        conversionCurrencyItemView.addTextChangedListener(this)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        conversionCurrencyItemView.addTextChangedListener(this)
                    }
                }
            }
        })

        return conversionCurrencyItemView
    }

    override fun onCurrencyUpdate() {
        Log.d(PurseFragment.TAG, "Currencies updated")
        refreshTable(clearTable = true)
    }
}