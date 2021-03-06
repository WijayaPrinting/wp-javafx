package com.hendraanggrian.openpss.ui.main

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hendraanggrian.bundler.bindExtras
import com.hendraanggrian.openpss.R
import com.hendraanggrian.openpss.R2
import com.hendraanggrian.openpss.ui.Activity
import com.hendraanggrian.openpss.ui.UnsupportedFragment
import com.hendraanggrian.openpss.ui.customer.CustomerFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    private val navigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        replaceFragment(
            R.id.fragmentLayout, when (item.itemId) {
                R.id.tab_customer -> CustomerFragment()
                R.id.tab_invoice -> UnsupportedFragment()
                R.id.tab_schedule -> UnsupportedFragment()
                else -> UnsupportedFragment()
            }
        )
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindExtras()
        navigationView.run {
            menu.findItem(R.id.tab_customer).title = getString(R2.string.customer)
            menu.findItem(R.id.tab_invoice).title = getString(R2.string.invoice)
            menu.findItem(R.id.tab_schedule).title = getString(R2.string.schedule)
            menu.findItem(R.id.tab_finance).title = getString(R2.string.finance)
            setOnNavigationItemSelectedListener(navigationListener)
        }
        replaceFragment(R.id.fragmentLayout, CustomerFragment())
    }
}
