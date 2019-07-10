package com.brainheap.android.config

import com.brainheap.android.BrainheapApp
import com.brainheap.android.R

object BrainheapProperties {
    val baseUrl: String = BrainheapApp.applicationContext().getString(R.string.brainheap_base_url)
    val redirectUri: String = BrainheapApp.applicationContext().getString(R.string.brainheap_redirect_url)
    val loginUrl: String = "${baseUrl}/redirect?redirect_uri=$redirectUri"
}