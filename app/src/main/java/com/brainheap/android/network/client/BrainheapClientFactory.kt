package com.brainheap.android.network.client

import com.brainheap.android.config.BrainheapProperties
import com.brainheap.android.network.RefrofitClientFactory

object BrainheapClientFactory : RefrofitClientFactory<BrainheapClient>(BrainheapProperties.baseUrl)