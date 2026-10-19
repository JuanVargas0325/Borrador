package com.equipo2.vinilos.data.network

import retrofit2.http.Body
import retrofit2.http.POST

interface VinilosApiService {

    @POST("albums")
    suspend fun createAlbum(@Body request: CreateAlbumRequest): AlbumDto
}
