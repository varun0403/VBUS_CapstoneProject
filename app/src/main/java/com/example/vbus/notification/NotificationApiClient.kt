package com.example.vbus.notification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NotificationApi {
    private var retrofit:Retrofit?=null

    fun sendNotification():NotificationInterface{
        if (retrofit==null){
            retrofit = Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(NotificationInterface::class.java)
    }
}