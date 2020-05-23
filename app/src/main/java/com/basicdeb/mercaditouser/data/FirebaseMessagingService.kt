package com.basicdeb.mercaditouser.data

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class FirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.i("token",p0)
    }


}