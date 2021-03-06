package com.atalgaba.dd_coin_pouch.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.atalgaba.dd_coin_pouch.R
import com.atalgaba.dd_coin_pouch.customs.persistence.models.Currency
import com.atalgaba.dd_coin_pouch.customs.objects.Currencies
import com.atalgaba.dd_coin_pouch.helpers.ResourcesHelper

class CurrencyView : LinearLayout {
    private var _currency: Currency? = null

    private var _quantity: Int = 0

    private var currencyTextView: TextView? = null
    private var quantityTextView: TextView? = null

    private val currencyName: String
        get() = _currency?.let { "${it.getName(context)} (${it.getCode(context)})" } ?: ""

    var currency: Currency?
        get() = _currency
        set(value) {
            _currency = value
            currencyTextView?.text = currencyName
        }

    var quantity: Int
        get() = _quantity
        set(value) {
            _quantity = value
            quantityTextView?.text = value.toString()
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
            attrs, R.styleable.CurrencyView, defStyle, 0
        )

        if (a.hasValue(R.styleable.CurrencyView_currency)) {
            val currencyId = a.getInt(R.styleable.CurrencyView_currency, 0).toLong()
            _currency = Currencies.available.firstOrNull { it.id == currencyId }
        }

        if (a.hasValue(R.styleable.CurrencyView_quantity)) {
            _quantity = a.getInt(R.styleable.CurrencyView_quantity, 0)
        }

        a.recycle()

        // Inflate layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.component_currency_view, this, true)

        orientation = HORIZONTAL
        weightSum = 1F

        currencyTextView = findViewById(R.id.currency_name)
        quantityTextView = findViewById(R.id.currency_value)

        currencyTextView?.text = currencyName
        quantityTextView?.text = _quantity.toString()
    }
}