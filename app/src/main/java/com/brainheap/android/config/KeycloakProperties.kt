package com.brainheap.android.config

import com.brainheap.android.BrainheapApp
import com.brainheap.android.R

object KeycloakProperties {
    val clientId: String = BrainheapApp.applicationContext().getString(R.string.keycloak_client_id)
    val clientSecret: String = BrainheapApp.applicationContext().getString(R.string.keycloak_client_Secret)
    val baseUrl: String = BrainheapApp.applicationContext().getString(R.string.keycloak_base_url)
    val authCodeUrl: String =
        "$baseUrl/${BrainheapApp.applicationContext().getString(R.string.keycloak_auth_code_endpoint)}"
    val redirectUri: String = BrainheapApp.applicationContext().getString(R.string.keycloak_redirect_url)
}