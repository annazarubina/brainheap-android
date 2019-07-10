package com.brainheap.android.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HttpClientFactory {

    companion object {
        private var instance: OkHttpClient? = null
        var jSessionId: String? = null

        fun instance(): OkHttpClient {
            instance?:let {
                instance = OkHttpClient.Builder()
                    .addInterceptor(SessionIdInterceptor())
                    .build()
            }
            return instance!!
        }
    }

    private class SessionIdInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            return processResponse(chain.proceed(processRequest(chain.request())))
        }

        private fun processRequest(request: Request) : Request =
            request.newBuilder()
                .header("User-Agent", "Your-App-Name")
                .header("Cookie", "JSESSIONID=$jSessionId")
                .header("Accept", "application/vnd.yourapi.v1.full+json")
                .method(request.method(), request.body())
                .build()

        private fun processResponse(response: Response) : Response {
            val cookieHeaders = response.headers("cookie")
            for (cookieHeader in cookieHeaders) {
                if (cookieHeader.contains("JSESSIONID=")) {
                    jSessionId = cookieHeader.substring(11, cookieHeader.indexOf(';'))
                }
            }
            return response
        }
    }
}