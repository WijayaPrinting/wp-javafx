package com.hendraanggrian.openpss.api

import com.google.gson.GsonBuilder
import com.hendraanggrian.openpss.BuildConfig.ARTIFACT
import com.hendraanggrian.openpss.BuildConfig.USER
import com.hendraanggrian.openpss.time.PATTERN_DATETIME_EXTENDED
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApi {

    @GET(PATH)
    fun getReleases()

    @GET("$PATH/{id}")
    fun getRelease(@Path("id") id: Int)

    @GET("$PATH/latest")
    fun getLatestReleases()

    companion object {
        private const val END_POINT = "https://api.github.com"
        private const val PATH = "repos/$USER/$ARTIFACT/releases"

        fun create(): GitHubApi = Retrofit.Builder()
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                val newRequest = chain.request()
                    .newBuilder()
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(newRequest)
            }.build())
            .baseUrl(END_POINT)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder()
                .setDateFormat(PATTERN_DATETIME_EXTENDED)
                .create()))
            .build().create(GitHubApi::class.java)
    }
}