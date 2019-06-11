package com.brainheap.android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainheap.android.R
import com.brainheap.android.login.AuthProviderManager
import com.brainheap.android.preferences.Constants
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.ui.login.LoginActivity
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        val ssh = getSHA()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val isLoggedIn = isLoggedIn()
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
                logOut()
                true
            }
            R.id.action_log_in -> {
                logIn()
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

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun getSHA(): String? {
        var result: String? = null
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                result = Base64.encodeToString(md.digest(), Base64.DEFAULT)
            }
        } catch (e: PackageManager.NameNotFoundException) {
        } catch (e: NoSuchAlgorithmException) {
        }
        return result
    }

    private fun isLoggedIn(): Boolean = CredentialsHolder.userId.value?.isNotEmpty() ?: false

    private fun logIn() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun logOut() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.ID_PROP, "").apply()
        AuthProviderManager.logout()
        CredentialsHolder.clean()
    }
}
