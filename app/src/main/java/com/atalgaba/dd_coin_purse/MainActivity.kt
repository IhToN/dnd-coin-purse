package com.atalgaba.dd_coin_purse

import android.app.ActionBar
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.abubusoft.kripton.android.KriptonLibrary
import com.atalgaba.dd_coin_purse.customs.objects.Currencies
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {
    private var settingsDrawer: DrawerLayout? = null
    private var settingsManageCurrencies: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)


        // Initialize Kripton Persistence Library
        KriptonLibrary.init(this)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        configureSettingsNavigation()

        Log.d("Currencies", Currencies.available.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.topbar_menu, menu)
        return true
    }

    private fun toggleSettingsDrawer() {
        if (settingsDrawer != null && settingsManageCurrencies != null) {
            if (settingsDrawer?.isDrawerOpen(settingsManageCurrencies!!) == true) {
                settingsDrawer?.closeDrawer(settingsManageCurrencies!!)
            } else {
                settingsDrawer?.openDrawer(settingsManageCurrencies!!)
            }
        }
    }

    private fun configureSettingsNavigation() {
        settingsDrawer = findViewById(R.id.settings_drawer)
        settingsManageCurrencies = settingsDrawer?.findViewById(R.id.settings_manage_currencies)

        settingsDrawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        if (settingsManageCurrencies != null) {
            Currencies.available.forEach { currency ->
                val currencySwitch = SwitchMaterial(this)

                val lp = ActionBar.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                currencySwitch.layoutParams = lp
                currencySwitch.text = currency.getName(this)
                currencySwitch.isChecked = currency.enabled
                currencySwitch.setOnCheckedChangeListener { _, isChecked ->
                    currency.enabled = isChecked
                    currency.update()
                }

                settingsManageCurrencies!!.addView(currencySwitch)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                Toast.makeText(this, "Display Settings Menu", Toast.LENGTH_SHORT).show()
                toggleSettingsDrawer()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}