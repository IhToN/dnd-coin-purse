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
import com.atalgaba.dd_coin_pouch.helpers.CurrencyHelper
import com.atalgaba.dd_coin_pouch.helpers.SavedInstanceHelper
import com.atalgaba.dd_coin_pouch.ui.components.CalculatorCurrencyView
import com.atalgaba.dd_coin_pouch.ui.pouch.PouchFragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlin.math.floor

class CalculatorFragment : Fragment(), Currencies.OnCurrencyUpdateListener {

    companion object {
        const val TAG: String = "CalculatorFragment"
    }

    private var _binding: FragmentCalculatorBinding? = null

    private lateinit var mActivity: Activity
    private lateinit var mView: View

    private var coinsForEach: List<Pair<Currency, Int?>>? = null
    private var coinsRemaining: List<Pair<Currency, Int?>>? = null

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

        initializeFloatingButtons()

        refreshTable()
        divideBetweenPlayers()

        return mView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Currencies.unregisterOnCurrencyUpdateListener(this)
    }

    private fun initializeFloatingButtons() {
        binding.floatingDeleteButton.setOnClickListener {
            showDeleteDialog()
        }

        binding.floatingTransferButton.setOnClickListener {
            showTransferDialog()
        }
    }

    private fun initializeCurrencies() {
        SavedInstanceHelper.calculatorCurrencies =
            if (!SavedInstanceHelper.calculatorCurrencies.isNullOrEmpty())
                Currencies.enabled
                    .sortedByDescending { it.value }
                    .map {
                        it to SavedInstanceHelper.calculatorCurrencies!!.firstOrNull { (currency, _) -> currency == it }?.second
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
            SavedInstanceHelper.calculatorCurrencies?.forEach { (currency, quantity) ->
                run {
                    list.addView(getConversionCurrencyItemView(currency, quantity))
                }
            }

            list.addView(getPlayersItemView(SavedInstanceHelper.calculatorPlayers))
        } else {
            list.children.forEach {
                if (it is CalculatorCurrencyView && it.currency != filterCurrency) {
                    val currencyPair: Pair<Currency, Int?>? =
                        SavedInstanceHelper.calculatorCurrencies?.firstOrNull { (currency, _) ->
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
            SavedInstanceHelper.calculatorCurrencies?.filter { pair -> pair.second != null && pair.second!! > 0 }

        coinsForEach = filterCurrencies?.map { (currency, quantity) ->
            Pair(
                currency,
                floor(((quantity ?: 0) / SavedInstanceHelper.calculatorPlayers).toDouble()).toInt()
            )
        }

        coinsRemaining = filterCurrencies?.map { (currency, quantity) ->
            Pair(
                currency,
                (quantity ?: 0).rem(SavedInstanceHelper.calculatorPlayers)
            )
        }

        val forEach: String = coinsForEach?.mapNotNull { (currency, quantity) ->
            coinToString(
                currency,
                quantity ?: 0
            )
        }?.joinToString(" ")?.trim(',', ' ') ?: ""

        val remaining: String = coinsRemaining?.mapNotNull { (currency, quantity) ->
            coinToString(
                currency,
                quantity ?: 0
            )
        }?.joinToString(" ")?.trim(',', ' ') ?: ""

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
                SavedInstanceHelper.calculatorCurrencies =
                    SavedInstanceHelper.calculatorCurrencies?.map { pair ->
                        pair.copy(
                            second = if (pair.first != currency) {
                                pair.second
                            } else if (!s?.toString().isNullOrEmpty()) {
                                s.toString().toInt()
                            } else {
                                null
                            }
                        )
                    }?.toMutableList()
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
                SavedInstanceHelper.calculatorPlayers = if (!s?.toString().isNullOrEmpty()) {
                    s.toString().toInt()
                } else {
                    1
                }

                divideBetweenPlayers()
            }
        })

        return playersView
    }

    private fun undoDeletion() {
        val previousLoot = SavedInstanceHelper.calculatorCurrencies?.toMutableList()
        Snackbar.make(mView, R.string.snackbar_calculator_reset, Snackbar.LENGTH_SHORT)
            .setAction(R.string.action_undo) {
                SavedInstanceHelper.calculatorCurrencies = previousLoot
                refreshTable()
            }.show()
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(mActivity)
            .setTitle(R.string.dialog_calculator_delete_title)
            .setMessage(R.string.dialog_calculator_delete_message)
            .setPositiveButton(R.string.action_yes) { _, _ ->

                undoDeletion()

                SavedInstanceHelper.calculatorCurrencies = Currencies.enabled
                    .sortedByDescending { it.value }
                    .map { it to null }
                    .toMutableList()
                refreshTable()
            }
            .setNegativeButton(R.string.action_no) { _, _ -> }
            .show()
    }

    private fun undoTransfer() {
        val previousPouch: List<Pair<Currency, Int>> =
            Currencies.available.map { currency ->
                val quantity = currency.quantity
                Pair(currency, quantity)
            }

        Snackbar.make(mView, R.string.snackbar_pouch_modified, Snackbar.LENGTH_SHORT)
            .setAction(R.string.action_undo) {
                previousPouch.forEach { (currency, quantity) ->
                    currency.quantity = quantity
                    currency.update()
                }
                refreshTable()
            }.show()
    }

    private fun showTransferDialog() {
        MaterialAlertDialogBuilder(mActivity)
            .setTitle(R.string.dialog_calculator_transfer_title)
            .setMessage(R.string.dialog_calculator_transfer_message)
            .setPositiveButton(R.string.action_yes) { _, _ ->

                undoTransfer()

                coinsForEach?.forEach { (currency, quantity) ->
                    if (quantity != null) {
                        currency.quantity += quantity
                        currency.update()
                    }
                }
            }
            .setNegativeButton(R.string.action_no) { _, _ -> }
            .show()
    }

    override fun onCurrencyUpdate() {
        Log.d(PouchFragment.TAG, "Currencies updated")
        refreshTable(clearTable = true)
        divideBetweenPlayers()
    }
}