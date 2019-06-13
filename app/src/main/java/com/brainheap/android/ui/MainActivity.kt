package com.brainheap.android.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainheap.android.R
import com.brainheap.android.login.LoginModule
import com.facebook.FacebookSdk


class MainActivity : AppCompatActivity() {
    private val loginModule = LoginModule()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!loginModule.isLoggedIn()) {
            loginModule.logIn(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isLoggedIn = loginModule.isLoggedIn()
        menu.findItem(R.id.action_log_in).isVisible = !isLoggedIn
        menu.findItem(R.id.action_profile).isVisible = isLoggedIn
        menu.findItem(R.id.action_log_out).isVisible = isLoggedIn
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_search -> {
                showMessage("Search")
                true
            }
            R.id.action_profile -> {
                showMessage("Profile")
                true
            }
            R.id.action_log_out -> {
                loginModule.logOut()
                loginModule.logIn(this)
                true
            }
            R.id.action_log_in -> {
                loginModule.logIn(this)
                true
            }
            R.id.action_setting -> {
                showMessage("Setting")
                true
            }
            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
        }

    private fun showMessage(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
