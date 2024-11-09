package com.example.slideflow.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://test.onsignage.com/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface ApiService {
    @GET("PlayerBackend/screen/playlistItems/{screenKey}")
    suspend fun getPlaylists(@Path("screenKey") screenKey: String): PlaylistResponse

    @GET("PlayerBackend/creative/get/{fileKey}")
    suspend fun getMediaFile(@Path("fileKey") fileKey: String): String
}

object SlideFlowApi {
    val retrofitService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}