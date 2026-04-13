package com.dimasarya.senserbot.data.remote.api

import com.dimasarya.senserbot.data.remote.dto.UserDto
import retrofit2.http.GET

interface UserApi {

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(id: Int): UserDto
}
