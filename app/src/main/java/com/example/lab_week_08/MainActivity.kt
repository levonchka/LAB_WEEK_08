package com.example.lab_week_08

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.*

import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker

class MainActivity : AppCompatActivity() {

    // WorkManager digunakan untuk mengatur dan mengeksekusi WorkRequest
    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Atur padding otomatis agar UI tidak tertutup system bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi WorkManager
        workManager = WorkManager.getInstance(this)

        // --- Membuat Constraints ---
        // Worker hanya bisa dijalankan jika ada koneksi internet
        val networkConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val id = "001"

        // --- Membuat Work Request ---
        // OneTimeWorkRequest -> dijalankan hanya sekali
        // PeriodicWorkRequest -> dijalankan berulang dengan interval tertentu

        // Request untuk FirstWorker
        val firstRequest = OneTimeWorkRequest.Builder(FirstWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(FirstWorker.INPUT_DATA_ID, id))
            .build()

        // Request untuk SecondWorker
        val secondRequest = OneTimeWorkRequest.Builder(SecondWorker::class.java)
            .setConstraints(networkConstraints)
            .setInputData(getIdInputData(SecondWorker.INPUT_DATA_ID, id))
            .build()

        // --- Menyusun urutan eksekusi ---
        // Menjalankan FirstWorker terlebih dahulu, lalu SecondWorker setelahnya
        workManager.beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()

        // --- Mengamati hasil eksekusi ---
        // Mengamati LiveData untuk mengetahui kapan setiap worker selesai
        workManager.getWorkInfoByIdLiveData(firstRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showResult("First process is done")
                }
            }

        workManager.getWorkInfoByIdLiveData(secondRequest.id)
            .observe(this) { info ->
                if (info.state.isFinished) {
                    showResult("Second process is done")
                }
            }
    }

    // Fungsi untuk membungkus input ke dalam Data
    private fun getIdInputData(idKey: String, idValue: String): Data =
        Data.Builder()
            .putString(idKey, idValue)
            .build()

    // Fungsi untuk menampilkan hasil menggunakan Toast
    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
