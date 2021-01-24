package com.atalgaba.dd_coin_purse.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.atalgaba.dd_coin_purse.R
import com.atalgaba.dd_coin_purse.customs.objects.Currencies
import com.atalgaba.dd_coin_purse.customs.persistence.models.Currency
import com.google.android.material.textfield.TextInputLayout

/**
 * TODO: document your custom view class.
 */
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

        if (a.hasValue(R.styleable.CurrencyView_currency)) {
            val currencyId = a.getInt(R.styleable.CurrencyView_currency, 0).toLong()
            _currency = Currencies.available.firstOrNull { it.id == currencyId }
        }

        if (a.hasValue(R.styleable.CurrencyView_quantity)) {
            _quantity = a.getFloat(R.styleable.CurrencyView_quantity, 0f).toDouble()
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
}