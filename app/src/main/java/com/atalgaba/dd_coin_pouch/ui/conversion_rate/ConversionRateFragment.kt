package com.atalgaba.dd_coin_pouch.ui.conversion_rate

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.atalgaba.dd_coin_pouch.R
import com.atalgaba.dd_coin_pouch.customs.objects.Currencies
import com.atalgaba.dd_coin_pouch.customs.persistence.models.Currency
import com.atalgaba.dd_coin_pouch.databinding.FragmentConversionRateBinding
import com.atalgaba.dd_coin_pouch.ui.components.ConversionCurrencyItemView
import com.atalgaba.dd_coin_pouch.ui.pouch.PouchFragment
import java.lang.Exception
import java.text.NumberFormat

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
                    .sortedBy { it.value }
                    .toMutableList()
                    .map {
                        it to (currencies.firstOrNull { (currency, _) -> currency == it }?.second
                            ?: 0.0)
                    }
            else
                Currencies.enabled.sortedBy { it.value }.toMutableList().map { it to 0.0 }

        val mainCurrency =
            currencies.firstOrNull { (_, quantity) -> quantity > 0 } ?: currencies.first()
        calculateCurrencies(
            mainCurrency.first,
            if (mainCurrency.second > 0) mainCurrency.second else 1.0
        )
    }

    private fun calculateCurrencies(from: Currency, value: Double) {
        currencies = currencies.map { (currency, _) ->
            Log.d(TAG, "${from.getName(mActivity)} => ${currency.getName(mActivity)}: $value * ${from.value} / ${currency.value}")
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

        conversionCurrencyItemView.trimQuantity()

        conversionCurrencyItemView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                var newValue = "0.0"
                if (!s.isNullOrEmpty()) {
                    newValue = s.toString()
                }

                if (conversionCurrencyItemView.hasFocus()) {
                    val nf = NumberFormat.getInstance()
                    calculateCurrencies(currency, nf.parse(newValue)?.toDouble() ?: 0.0)
                } else {
                    try {
                        conversionCurrencyItemView.removeTextChangedListener(this)

                        if (newValue != "") {
                            conversionCurrencyItemView.trimQuantity()
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
        Log.d(PouchFragment.TAG, "Currencies updated")
        refreshTable(clearTable = true)
    }
}