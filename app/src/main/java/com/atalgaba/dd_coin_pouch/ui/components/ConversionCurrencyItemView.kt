package com.atalgaba.dd_coin_pouch.ui.components

import android.content.Context
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import com.atalgaba.dd_coin_pouch.R
import com.atalgaba.dd_coin_pouch.customs.objects.Currencies
import com.atalgaba.dd_coin_pouch.customs.persistence.models.Currency
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.lang.Exception
import java.text.DecimalFormatSymbols

class ConversionCurrencyItemView : LinearLayout {
    private var _currency: Currency? = null

    private var _quantity: Double = 0.0

    var currencyTextView: TextView? = null
    var quantityTextView: TextInputLayout? = null

    private val currencyName: String
        get() = _currency?.let { "${it.getName(context)} (${it.getCode(context)})" } ?: ""

    var currency: Currency?
        get() = _currency
        set(value) {
            _currency = value
            currencyTextView?.text = currencyName
        }

    var quantity: Double
        get() = _quantity
        set(value) {
            _quantity = value
            quantityTextView?.editText?.setText(value.toString())
            quantityTextView?.editText?.setSelection(quantityTextView?.editText?.text?.length ?: 0)
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ConversionCurrencyItemView, defStyle, 0
        )

        if (a.hasValue(R.styleable.ConversionCurrencyItemView_currency)) {
            val currencyId = a.getInt(R.styleable.ConversionCurrencyItemView_currency, 0).toLong()
            _currency = Currencies.available.firstOrNull { it.id == currencyId }
        }

        if (a.hasValue(R.styleable.ConversionCurrencyItemView_quantity)) {
            _quantity = a.getFloat(R.styleable.ConversionCurrencyItemView_quantity, 0f).toDouble()
        }

        a.recycle()

        // Inflate layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.component_conversion_currency_item_view, this, true)

        orientation = HORIZONTAL
        weightSum = 1F

        currencyTextView = findViewById(R.id.currency_name)
        quantityTextView = findViewById(R.id.currency_value)

        currencyTextView?.text = currencyName
        quantityTextView?.editText?.setText(_quantity.toString())
        quantityTextView?.editText?.setSelection(quantityTextView?.editText?.text?.length ?: 0)

        quantityTextView?.editText?.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_UNSPECIFIED -> {
                    val imm: InputMethodManager = v.context
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    true
                }
                else -> false
            }
        }
    }

    fun addTextChangedListener(textWatcher: TextWatcher) {
        quantityTextView?.editText?.addTextChangedListener(textWatcher)
    }

    fun removeTextChangedListener(textWatcher: TextWatcher) {
        quantityTextView?.editText?.removeTextChangedListener(textWatcher)
    }

    fun setText(text: String) {
        quantityTextView?.editText?.setText(text)
    }

    fun setCursor(position: Int) {
        val maxPosition: Int = quantityTextView?.editText?.text?.length ?: 0
        val cursorPosition = maxOf(position, maxPosition)

        val editText = quantityTextView?.editText
        if (editText is TextInputEditText) {
            try {
                editText.setSelection(cursorPosition)
            } catch (ex: Exception) {
                editText.setSelection(0)
            }
        }
    }

    fun trimQuantity() {
        val cursorPosition: Int =
            quantityTextView?.editText?.selectionEnd ?: 0

        val value = quantityTextView?.editText?.text.toString()

        val vals = String.format("%.3f", value.toDouble())
            .split(DecimalFormatSymbols.getInstance().decimalSeparator).toMutableList()

        if (vals.isNotEmpty()) {
            vals[0] = vals[0].trimStart('0').padStart(1, '0')
        }
        if (vals.size > 1) {
            vals[1] = vals[1].trimEnd('0')
        }
        val parsedValue =
            vals.joinToString(DecimalFormatSymbols.getInstance().decimalSeparator.toString())
                .trimEnd(DecimalFormatSymbols.getInstance().decimalSeparator)
        setText(parsedValue)

        val diff = parsedValue.length - value.length
        setCursor(cursorPosition + diff)
    }
}