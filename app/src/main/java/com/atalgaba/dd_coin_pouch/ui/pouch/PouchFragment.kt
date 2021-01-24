package com.atalgaba.dd_coin_pouch.ui.pouch

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.atalgaba.dd_coin_pouch.BuildConfig
import com.atalgaba.dd_coin_pouch.R
import com.atalgaba.dd_coin_pouch.customs.inputs.InputFilterIntBetween
import com.atalgaba.dd_coin_pouch.customs.objects.Currencies
import com.atalgaba.dd_coin_pouch.customs.persistence.models.Currency
import com.atalgaba.dd_coin_pouch.databinding.FragmentPouchBinding
import com.atalgaba.dd_coin_pouch.helpers.AdsHelper
import com.atalgaba.dd_coin_pouch.helpers.CurrencyHelper
import com.atalgaba.dd_coin_pouch.ui.components.CurrencyView
import com.google.android.gms.ads.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.max


class PouchFragment : Fragment(), Currencies.OnCurrencyUpdateListener {
    companion object {
        const val TAG: String = "PouchFragment"
    }

    private var _binding: FragmentPouchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private lateinit var mActivity: Activity
    private lateinit var mView: View

    private lateinit var adView: AdView
    private val adSize: AdSize
        get() {
            val outMetrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val display = mActivity.display
                display?.getRealMetrics(outMetrics)
            } else {
                @Suppress("DEPRECATION")
                val display = mActivity.windowManager.defaultDisplay
                @Suppress("DEPRECATION")
                display.getMetrics(outMetrics)
            }

            val density = outMetrics.density

            var adWidthPixels = binding.adLayout.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(mActivity, adWidth)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mActivity = requireActivity()

        adView = AdView(mActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPouchBinding.inflate(inflater, container, false)
        mView = binding.root

        refreshPouch()
        Currencies.registerOnCurrencyUpdateListener(this)

        binding.floatingConversionButton.setOnClickListener {
            showConversionDialog()
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            TooltipCompat.setTooltipText(
                binding.floatingConversionButton,
                mActivity.getString(R.string.action_conversion)
            )
        }

        if (AdsHelper.areAdsEnabled) {
            binding.adLayout.addView(adView)
            initBanner()
        }

        return mView
    }

    override fun onResume() {
        super.onResume()
        if (AdsHelper.areAdsEnabled) loadBanner()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Currencies.unregisterOnCurrencyUpdateListener(this)
        _binding = null
    }

    private fun refreshPouch() {
        val pouch: LinearLayout = mView.findViewById(R.id.pouch_list)

        pouch.removeAllViews()

        Currencies.enabled.forEach { currency: Currency ->
            run {
                pouch.addView(getCurrencyView(currency))
            }
        }
    }

    private fun undoCurrency(currentCurrencies: List<Currency>) {
        val originalCurrencies = currentCurrencies.map { it to it.quantity }
        Snackbar.make(mView, R.string.snackbar_pouch_modified, Snackbar.LENGTH_SHORT)
            .setAction(R.string.action_undo) {
                originalCurrencies.forEach { pair ->
                    pair.first.quantity = pair.second
                    pair.first.update()
                }
            }.show()
    }

    private fun undoCurrency(currency: Currency) {
        val originalQuantity = currency.quantity
        Snackbar.make(mView, R.string.snackbar_pouch_modified, Snackbar.LENGTH_SHORT)
            .setAction(R.string.action_undo) {
                currency.quantity = originalQuantity
                currency.update()
            }.show()
    }

    private fun showConversionDialog() {
        Log.d(TAG, "Floating Conversion Button Clicked")
        MaterialAlertDialogBuilder(mActivity)
            .setTitle(R.string.dialog_currency_conversion_title)
            .setMessage(R.string.dialog_currency_conversion_message)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                Log.d(TAG, "Exchange process should be executed")

                undoCurrency(Currencies.available)
                CurrencyHelper.optimizePouch(Currencies.enabled)
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

                    undoCurrency(currency)
                    currency.quantity = newCoins
                    currency.update()
                }
                .setNegativeButton(resources.getString(R.string.action_remove)) { _, _ ->
                    // Remove Coins

                    val currentCoins = currency.quantity
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0
                    val newCoins = max(currentCoins - inputCoins, 0)

                    Log.d(TAG, "New coins: $currentCoins + $inputCoins = $newCoins")

                    undoCurrency(currency)
                    currency.quantity = newCoins
                    currency.update()
                }
                .setNeutralButton(resources.getString(R.string.action_overwrite)) { _, _ ->
                    // Overwrite Coins

                    val currentCoins = currency.quantity
                    val inputCoins: Int = textInputEditText?.text.toString().toIntOrNull() ?: 0

                    Log.d(TAG, "New coins: $currentCoins => $inputCoins")

                    undoCurrency(currency)
                    currency.quantity = inputCoins
                    currency.update()
                }
                .create()

            textInputLayout?.requestFocus()


            dialog.setOnShowListener {
                textInputLayout?.editText?.postDelayed({
                    val imm: InputMethodManager? =
                        mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.showSoftInput(textInputEditText, InputMethodManager.SHOW_IMPLICIT)
                }, 50)
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

    override fun onCurrencyUpdate() {
        Log.d(TAG, "Currencies updated")
        refreshPouch()
    }

    // region Ad Banner
    private fun initBanner() {
        adView.adUnitId = BuildConfig.AD_UNIT_ID

        adView.adSize = adSize

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "onAdLoaded")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Code to be executed when an ad request fails.
                Log.d(TAG, "onAdFailedToLoad")
                Log.e(TAG, "${adError.code} - ${adError.cause} || ${adError.message}")
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.d(TAG, "onAdOpened")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.d(TAG, "onAdClicked")
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.d(TAG, "onAdLeftApplication")
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
                Log.d(TAG, "onAdClosed")
            }
        }
    }

    private fun loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this device."
        val adRequest = AdRequest
            .Builder()
            .build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }
    // endregion
}