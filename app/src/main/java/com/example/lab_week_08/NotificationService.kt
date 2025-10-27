package com.example.lab_week_08

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    // Notification builder untuk membuat dan update notifikasi
    private lateinit var notificationBuilder: NotificationCompat.Builder

    // Handler dan HandlerThread untuk menjalankan proses di background thread
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent): IBinder? = null

    // Dipanggil sekali saat service pertama kali dibuat
    override fun onCreate() {
        super.onCreate()

        // Membuat notification awal (foreground)
        notificationBuilder = startForegroundService()

        // Membuat thread baru bernama "SecondThread" untuk menjalankan tugas background
        val handlerThread = HandlerThread("SecondThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }

    // Dipanggil saat service dijalankan (setelah startForeground)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)

        // Ambil channel ID dari intent
        val Id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        // Jalankan tugas di background thread
        serviceHandler.post {
            // Hitung mundur 10 sampai 0 dan update notifikasi
            countDownFromTenToZero(notificationBuilder)

            // Notifikasi ke MainActivity bahwa proses selesai
            notifyCompletion(Id)

            // Hentikan notifikasi foreground dan matikan service
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return returnValue
    }

    // === Membuat dan menjalankan foreground notification ===
    private fun startForegroundService(): NotificationCompat.Builder {
        val pendingIntent = getPendingIntent()
        val channelId = createNotificationChannel()

        val notificationBuilder = getNotificationBuilder(pendingIntent, channelId)

        // Jalankan service sebagai foreground agar notifikasi langsung tampil
        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        return notificationBuilder
    }

    // === Membuat PendingIntent (aksi saat notifikasi diklik) ===
    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE
        else 0

        return PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            flag
        )
    }

    // === Membuat Notification Channel (dibutuhkan sejak Android O) ===
    private fun createNotificationChannel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "001"
            val channelName = "001 Channel"
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, channelPriority)
            val service = requireNotNull(
                ContextCompat.getSystemService(this, NotificationManager::class.java)
            )

            service.createNotificationChannel(channel)
            channelId
        } else {
            ""
        }

    // === Membuat konfigurasi dasar notifikasi ===
    private fun getNotificationBuilder(
        pendingIntent: PendingIntent,
        channelId: String
    ) = NotificationCompat.Builder(this, channelId)
        .setContentTitle("Second worker process is done")
        .setContentText("Check it out!")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent)
        .setTicker("Second worker process is done, check it out!")
        .setOngoing(true)

    // === Fungsi hitung mundur 10 ke 0 dan update teks notifikasi ===
    private fun countDownFromTenToZero(notificationBuilder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        for (i in 10 downTo 0) {
            Thread.sleep(1000L) // Delay 1 detik
            notificationBuilder
                .setContentText("$i seconds until last warning")
                .setSilent(true)

            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    // === Kirim hasil ke LiveData agar MainActivity bisa mendeteksi selesai ===
    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = Id
        }
    }

    companion object {
        const val NOTIFICATION_ID = 0xCA7
        const val EXTRA_ID = "Id"

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}
