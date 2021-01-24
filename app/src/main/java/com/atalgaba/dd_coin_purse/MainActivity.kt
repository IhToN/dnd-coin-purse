package com.atalgaba.dd_coin_purse

import android.app.ActionBar
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.abubusoft.kripton.android.KriptonLibrary
import com.atalgaba.dd_coin_purse.customs.objects.Currencies
import com.atalgaba.dd_coin_purse.helpers.AdsHelper
import com.google.android.gms.ads.MobileAds
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "DnD Coin Purse"
    }

    private var settingsDrawer: DrawerLayout? = null
    private var settingsSideDrawer: LinearLayout? = null


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

        MobileAds.initialize(this) {}
        AdsHelper.initialize(this)

        Log.d("Currencies", Currencies.available.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.topbar_menu, menu)
        return true
    }

    private fun toggleSettingsDrawer() {
        if (settingsDrawer != null && settingsSideDrawer != null) {
            if (settingsDrawer?.isDrawerOpen(settingsSideDrawer!!) == true) {
                settingsDrawer?.closeDrawer(settingsSideDrawer!!)
            } else {
                settingsDrawer?.openDrawer(settingsSideDrawer!!)
            }
        }
    }

    private fun configureSettingsNavigation() {
        settingsDrawer = findViewById(R.id.main_drawer_layout)
        settingsSideDrawer = settingsDrawer?.findViewById(R.id.settings_side_drawer)

        settingsDrawer?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


        if (settingsSideDrawer != null) {
            val currencySettings: LinearLayout? =
                settingsSideDrawer?.findViewById(R.id.settings_manage_currencies)
            val otherSettings: LinearLayout? = settingsSideDrawer?.findViewById(R.id.settings_other)

            val switchLayoutParams = ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            if (currencySettings !== null) {
                // currencies switch
                Currencies.available.forEach { currency ->
                    val currencySwitch = SwitchMaterial(this)

                    currencySwitch.layoutParams = switchLayoutParams
                    currencySwitch.text = currency.getName(this)
                    currencySwitch.isChecked = currency.enabled
                    currencySwitch.setOnCheckedChangeListener { _, isChecked ->
                        currency.enabled = isChecked
                        currency.update()
                    }

                    currencySettings.addView(currencySwitch)
                }
            }


            if (otherSettings !== null) {
                // ads switch
                val adsSwitch = SwitchMaterial(this)
                adsSwitch.layoutParams = switchLayoutParams
                adsSwitch.text = getString(R.string.menu_in_app_ads_switch)
                adsSwitch.isChecked = AdsHelper.areAdsEnabled
                adsSwitch.setOnCheckedChangeListener { _, isChecked ->
                    AdsHelper.areAdsEnabled = isChecked
                }

                otherSettings.addView(adsSwitch)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                Log.d(TAG, "Display settings menu")
                toggleSettingsDrawer()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}