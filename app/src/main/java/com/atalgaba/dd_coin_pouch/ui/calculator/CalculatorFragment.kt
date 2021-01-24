package com.atalgaba.dd_coin_pouch.ui.calculator

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.atalgaba.dd_coin_pouch.R
import com.atalgaba.dd_coin_pouch.customs.inputs.InputFilterIntBetween
import com.atalgaba.dd_coin_pouch.customs.objects.Currencies
import com.atalgaba.dd_coin_pouch.customs.persistence.models.Currency
import com.atalgaba.dd_coin_pouch.databinding.FragmentCalculatorBinding
import com.atalgaba.dd_coin_pouch.ui.components.CalculatorCurrencyView
import com.atalgaba.dd_coin_pouch.ui.pouch.PouchFragment
import com.google.android.flexbox.FlexboxLayout
import kotlin.math.floor

class CalculatorFragment : Fragment(), Currencies.OnCurrencyUpdateListener {

    companion object {
        const val TAG: String = "CalculatorFragment"
    }

    private var _binding: FragmentCalculatorBinding? = null

    private lateinit var mActivity: Activity
    private lateinit var mView: View

    private lateinit var currencies: MutableList<Pair<Currency, Int?>>
    private var players: Int = 1

    // todo: check performance optimization

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        mView = binding.root

        Currencies.registerOnCurrencyUpdateListener(this)

        initializeCurrencies()
        refreshTable()
        divideBetweenPlayers()

        return mView
    }

    private fun initializeCurrencies() {
        currencies =
            if (this::currencies.isInitialized)
                Currencies.enabled
                    .sortedByDescending { it.value }
                    .map {
                        it to currencies.firstOrNull { (currency, _) -> currency == it }?.second
                    }
                    .toMutableList()
            else
                Currencies.enabled
                    .sortedByDescending { it.value }
                    .map { it to null }
                    .toMutableList()
    }


    private fun updateCoinsForEach(forEach: String?) {
        Log.d(TAG, "For each: ${forEach ?: "Nothing"}")

        if (forEach.isNullOrEmpty()) {
            binding.forEachTitle.visibility = View.GONE
            binding.forEachContent.visibility = View.GONE
        } else {
            binding.forEachTitle.visibility = View.VISIBLE
            binding.forEachContent.visibility = View.VISIBLE
        }
        binding.forEachContent.text = forEach
    }

    private fun updateCoinsRemaining(remaining: String?) {
        Log.d(TAG, "Remaining: ${remaining ?: "Nothing"}")
        if (remaining.isNullOrEmpty()) {
            binding.remainingTitle.visibility = View.GONE
            binding.remainingContent.visibility = View.GONE
        } else {
            binding.remainingTitle.visibility = View.VISIBLE
            binding.remainingContent.visibility = View.VISIBLE
        }
        binding.remainingContent.text = remaining
    }

    private fun coinToString(currency: Currency, quantity: Int): String? {
        return if (quantity > 0) "$quantity ${currency.getCode(mActivity)},"
        else null
    }

    private fun refreshTable(filterCurrency: Currency? = null, clearTable: Boolean = false) {
        val list: FlexboxLayout = mView.findViewById(R.id.calculator_coins)

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

            list.addView(getPlayersItemView(players))
        } else {
            list.children.forEach {
                if (it is CalculatorCurrencyView && it.currency != filterCurrency) {
                    val currencyPair: Pair<Currency, Int?>? =
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

    private fun divideBetweenPlayers() {
        val filterCurrencies =
            currencies.filter { pair -> pair.second != null && pair.second!! > 0 }

        val forEach: String = filterCurrencies.mapNotNull { (currency, quantity) ->
            val calculated = floor(((quantity ?: 0) / players).toDouble()).toInt()

            coinToString(currency, calculated)
        }.joinToString(" ").trim(',', ' ')

        val remaining: String = filterCurrencies.mapNotNull { (currency, quantity) ->
            val calculated = (quantity ?: 0).rem(players)

            coinToString(currency, calculated)
        }.joinToString(" ").trim(',', ' ')

        updateCoinsForEach(forEach)
        updateCoinsRemaining(remaining)
    }

    private fun getConversionCurrencyItemView(
        currency: Currency? = null,
        quantity: Int? = 0
    ): CalculatorCurrencyView {
        val calculatorCurrencyView = CalculatorCurrencyView(mActivity)
        calculatorCurrencyView.currency = currency
        calculatorCurrencyView.quantity = quantity


        calculatorCurrencyView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                currencies = currencies.map { pair ->
                    pair.copy(
                        second = if (pair.first != currency) {
                            pair.second
                        } else if (!s?.toString().isNullOrEmpty()) {
                            s.toString().toInt()
                        } else {
                            null
                        }
                    )
                }.toMutableList()
                divideBetweenPlayers()
            }
        })

        return calculatorCurrencyView
    }

    private fun getPlayersItemView(quantity: Int?): CalculatorCurrencyView {
        val playersView = CalculatorCurrencyView(mActivity)
        playersView.currency = null
        playersView.quantity = quantity

        playersView.setHint(getString(R.string.players))

        playersView.quantityTextView?.editText?.filters = arrayOf(InputFilterIntBetween(1))

        playersView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                players = if (!s?.toString().isNullOrEmpty()) {
                    s.toString().toInt()
                } else {
                    1
                }

                divideBetweenPlayers()
            }
        })

        return playersView
    }

    override fun onCurrencyUpdate() {
        Log.d(PouchFragment.TAG, "Currencies updated")
        refreshTable(clearTable = true)
        divideBetweenPlayers()
    }
}