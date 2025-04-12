package com.example.vbus.notification

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header

interface NotificationInterface {
    @Headers("Content-Type: application/json")
    @POST("/v1/projects/vbus-160e8/messages:send")
    fun notification(
        @Header("Authorization") accessToken: String,
        @Body message: NotificationWrapper
    ): Call<NotificationWrapper>
}





