package com.atalgaba.dd_coin_purse.ui.home

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.atalgaba.dd_coin_purse.R
import com.atalgaba.dd_coin_purse.customs.inputs.InputFilterIntBetween
import com.atalgaba.dd_coin_purse.customs.objects.Currencies
import com.atalgaba.dd_coin_purse.customs.persistence.models.Currency
import com.atalgaba.dd_coin_purse.databinding.FragmentPurseBinding
import com.atalgaba.dd_coin_purse.helpers.CurrencyHelper
import com.atalgaba.dd_coin_purse.ui.components.CurrencyView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.max


class PurseFragment : Fragment(), Currencies.OnCurrencyUpdateListener {

    private var _binding: FragmentPurseBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var mActivity: Activity
    private lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivity = requireActivity()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPurseBinding.inflate(inflater, container, false)
        mView = binding.root

        refreshPurse()
        Currencies.registerOnCurrencyUpdateListener(this)

        binding.floatingConversionButton.setOnClickListener {
            showConversionDialog()
        }

        return mView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Currencies.unregisterOnCurrencyUpdateListener(this)
        _binding = null
    }

    private fun refreshPurse() {
        val purse: LinearLayout = mView.findViewById(R.id.purse_list)

        purse.removeAllViews()

        Currencies.enabled.forEach { currency: Currency ->
            run {
                purse.addView(getCurrencyView(currency))
            }
        }
    }

    private fun showConversionDialog() {
        Log.d(TAG, "Floating Conversion Button Clicked")
        MaterialAlertDialogBuilder(mActivity)
            .setTitle(R.string.dialog_currency_conversion_title)
            .setMessage(R.string.dialog_currency_conversion_message)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                // todo Add exchange method
                Log.d(TAG, "Exchange process should be executed")
                CurrencyHelper.optimizePurse(Currencies.available)
            }
            .setNegativeButton(R.string.action_no) { _, _ -> }
            .show()
    }

    private fun getCurrencyView(currency: Currency): CurrencyView {
        val currencyView = CurrencyHelper.getCurrencyView(mActivity, currency)

        currencyView.isFocusable = true
        currencyView.isClickable = true
        currencyView.isLongClickable = true

        currencyView.setOnClickListener {
            val inflater = LayoutInflater.from(mActivity)
            val coinDialogView = inflater.inflate(R.layout.dialog_edit_coin, null)

            val textInputLayout: TextInputLayout? =
                coinDialogView.findViewById(R.id.text_input_layout)
            val textInputEditText: TextInputEditText? =
                coinDialogView.findViewById(R.id.text_input_edit_text)

            textInputLayout?.hint = getString(
                R.string.dialog_coin_edit_title,
                currency.getName(mActivity),
                getString(R.string.title_coin_pieces)
            )
            textInputEditText?.filters = arrayOf(InputFilterIntBetween(0))

            val dialog = MaterialAlertDialogBuilder(mActivity)
                .setView(coinDialogView)
                .setPositiveButton(resources.getString(R.string.action_add)) { _, _ ->
                    // Add Coins

                    val currentCoins = currency.quantity
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0
                    val newCoins = currentCoins + inputCoins

                    Log.d(TAG, "New coins: $currentCoins + $inputCoins = $newCoins")

                    currency.quantity = newCoins
                    currency.update()
                }
                .setNegativeButton(resources.getString(R.string.action_remove)) { _, _ ->
                    // Remove Coins

                    val currentCoins = currency.quantity
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0
                    val newCoins = max(currentCoins - inputCoins, 0)

                    Log.d(TAG, "New coins: $currentCoins + $inputCoins = $newCoins")

                    currency.quantity = newCoins
                    currency.update()
                }
                .setNeutralButton(resources.getString(R.string.action_overwrite)) { _, _ ->
                    // Overwrite Coins

                    val currentCoins = currency.quantity
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0

                    Log.d(TAG, "New coins: $currentCoins => $inputCoins")

                    currency.quantity = inputCoins
                    currency.update()
                }
                .create()

            textInputLayout?.editText?.setOnFocusChangeListener { _, _ ->
                textInputLayout.editText?.postDelayed({
                    val imm: InputMethodManager? =
                        mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.showSoftInput(textInputEditText, InputMethodManager.SHOW_IMPLICIT)
                }, 50)
            }

            dialog.setOnShowListener {
                textInputLayout?.requestFocus()
            }

            dialog.show()

        }

        currencyView.setOnLongClickListener {
            Log.d(TAG, "Longclick on the currency ${currency.nameId}")
            // todo: display dialog conversion between current and dragged over
            // check https://material.io/components/cards/android#making-a-card-draggable
            true
        }

        return currencyView
    }

    companion object {
        const val TAG: String = "HomeFragment"
    }

    override fun onUpdate() {
        Log.d(TAG, "Currencies updated")
        refreshPurse()
    }
}