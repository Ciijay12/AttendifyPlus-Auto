package com.attendifyplus.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.attendifyplus.BuildConfig
import com.attendifyplus.data.model.AppUpdateConfig
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.io.File

class UpdateManager(private val context: Context) {

    private val dbRef = FirebaseDatabase.getInstance().getReference("config/update")

    fun getUpdateConfig(): Flow<AppUpdateConfig?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val config = snapshot.getValue(AppUpdateConfig::class.java)
                    trySend(config)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse update config")
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException())
                trySend(null)
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    fun isUpdateAvailable(config: AppUpdateConfig): Boolean {
        // Only return true if Firebase version is strictly greater than local version
        return config.versionCode > BuildConfig.VERSION_CODE
    }

    fun downloadAndInstall(url: String) {
        try {
            val destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "AttendifyPlus_Update.apk"
            val file = File(destination, fileName)
            
            if (file.exists()) file.delete()

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Downloading Update")
                .setDescription("AttendifyPlus is updating...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file))
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId) {
                        installApk(file)
                        try {
                            context.unregisterReceiver(this)
                        } catch (e: Exception) { }
                    }
                }
            }

            // Android 14 requirement: Must specify RECEIVER_EXPORTED for system broadcasts
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
            } else {
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initiate download")
        }
    }

    private fun installApk(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to launch installer")
        }
    }
}
