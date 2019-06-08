package com.brainheap.android.config

import com.brainheap.android.BrainheapApp
import com.brainheap.android.R

object FacebookProperties {
    val baseUrl: String = BrainheapApp.applicationContext().getString(R.string.facebook_base_url)
}