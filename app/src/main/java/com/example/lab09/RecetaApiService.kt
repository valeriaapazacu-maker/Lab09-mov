package com.example.lab09

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecetaApiService {
    @GET("recipes")
    suspend fun getRecetas(@Query("limit") limit: Int): RecetasRespuesta

    @GET("recipes/search")
    suspend fun buscarRecetas(@Query("q") texto: String): RecetasRespuesta

    @GET("recipes/{id}")
    suspend fun getReceta(@Path("id") id: Int): RecetaModel
}