package com.brainheap.android.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brainheap.android.config.BrainheapProperties
import com.brainheap.android.config.BrainheapProperties.redirectUri
import java.util.*
import android.webkit.CookieSyncManager
import android.os.Build



class WebLoginActivity : AppCompatActivity() {

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"
        const val JSESSIONID = "JSESSIONID"
    }

    private var jSessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createWebView() {
        val activity: Activity = this

        val webview = WebView(activity)
        webview.settings.userAgentString = USER_AGENT
        webview.settings.javaScriptEnabled = true
        clearWebView(webview)

        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                storeJSessionId()
                url
                    .takeIf { !url.startsWith(redirectUri) }
                    ?.let{ view.loadUrl(it) }
                    ?: let{
                        val intent = Intent()
                        intent.data = Uri.parse(redirectUri)
                        intent.putExtra(JSESSIONID, jSessionId)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                return true
            }

            override fun onReceivedError(view: WebView, req: WebResourceRequest, rerr: WebResourceError) {
                Toast.makeText(activity, rerr.description.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onPageFinished(view: WebView, url: String) {
                storeJSessionId()
            }

            private fun storeJSessionId(){
                getCookie(BrainheapProperties.baseUrl,
                    JSESSIONID
                )?.let{ jSessionId = it }
            }

            private fun getCookie(site: String, cookieName: String): String? {
                val cookieString = CookieManager.getInstance().getCookie(site)
                val cookieList = (cookieString?.split(";".toRegex())?.dropLastWhile { it.isEmpty() }
                    ?: Collections.emptyList()).toTypedArray()
                for (cookie in cookieList) {
                    if (cookie.contains(cookieName)) {
                        return cookie.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                    }
                }
                return null
            }
        }
        setContentView(webview)
        webview.loadUrl(BrainheapProperties.loginUrl)
    }

    private fun clearWebView(webView: WebView) {
        webView.clearCache(true)
        webView.clearHistory()
        clearCookies()
    }

    @SuppressLint("ObsoleteSdkInt")
    fun clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        } else {
            val cookieSyncMngr = CookieSyncManager.createInstance(this)
            cookieSyncMngr.startSync()
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            cookieManager.removeSessionCookie()
            cookieSyncMngr.stopSync()
            cookieSyncMngr.sync()
        }
    }
}
