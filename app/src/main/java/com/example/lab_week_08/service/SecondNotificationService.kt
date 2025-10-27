package com.example.lab_week_08.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.example.lab_week_08.R
import kotlinx.coroutines.*

class SecondNotificationService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default)
    companion object {
        val trackingCompletion = MutableLiveData<String>()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra("Id") ?: "002"
        val channelId = "SecondChannel$id"

        createNotificationChannel(channelId)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second Notification Service")
            .setContentText("Countdown starting...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(2, notification)

        scope.launch {
            for (i in 5 downTo 0) {  // ubah jadi 5 detik agar tidak tabrakan dengan service pertama
                val updateNotification = NotificationCompat.Builder(this@SecondNotificationService, channelId)
                    .setContentTitle("Second Notification Service")
                    .setContentText("Counting down: $i")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build()

                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(2, updateNotification)
                delay(1000)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Second Channel id $id process is done!", Toast.LENGTH_SHORT).show()
                trackingCompletion.postValue(id)
                stopForeground(true)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Second Service Channel", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
