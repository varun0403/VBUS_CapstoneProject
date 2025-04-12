package com.example.vbus.notification

import android.content.Context
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun sendingNotification(notification: NotificationWrapper, context: Context) {
    val TAG = "Notification"
    val token = "Bearer ${AccessToken.getAccessToken()}"
    Log.d("FCM", "Access Token: $token")
    Log.d(TAG,"Token :$token")

    NotificationApi.sendNotification()
        .notification(accessToken = token, message = notification)
        .enqueue(object : Callback<NotificationWrapper> {
            override fun onResponse(call: Call<NotificationWrapper>, response: Response<NotificationWrapper>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Notification Sent ✅", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context, "Notification Not Sent ❌", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Failure: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<NotificationWrapper>, t: Throwable) {
                Toast.makeText(context, "Failed to send ❌", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Send Notification Failed: ${t.localizedMessage}")
            }
        })
}

