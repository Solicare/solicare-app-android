package com.solicare.monitor

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private val baseUrl = "https://www.solicare.kro.kr/"
    private val CHANNEL_ID = "fcm_register_channel"
    private val PERMISSION_REQUEST_CODE = 123

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            getPhoneNumberAndRegister()
        } else {
            showRegistrationResultNotification(false, "전화번호를 가져올 수 없습니다. 권한을 허용해주세요.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        showFCMRegistrationDialog()

        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadsImagesAutomatically = true
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
            }
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    // 에러 발생시 로그 출력
                    error?.let {
                        Log.e("WebView Error", "Error: ${it.description}")
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    request?.url?.let { view?.loadUrl(it.toString()) }
                    return true
                }
            }
            loadUrl(baseUrl)
        }

        setContentView(webView)

        // 뒤로가기 동작 처리
        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
    }

    private fun showFCMRegistrationDialog() {
        AlertDialog.Builder(this)
            .setTitle("알림 동의")
            .setMessage("솔리케어 알림을 받으시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                checkAndRequestPermissions()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
        )

        if (permissions.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            getPhoneNumberAndRegister()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun getPhoneNumberAndRegister() {
        try {
            val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_NUMBERS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val phoneNumber = telephonyManager.line1Number
                if (!phoneNumber.isNullOrEmpty()) {
                    registerFCMToken(phoneNumber)
                } else {
                    val deviceId =
                        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                    showRegistrationResultNotification(false, "전화번호를 가져올 수 없습니다. 기기 ID를 사용합니다.")
                    registerFCMToken(deviceId)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "전화번호 가져오기 실패", e)
            showRegistrationResultNotification(false, "전화번호를 가져오는데 실패했습니다: ${e.message}")
        }
    }

    private fun registerFCMToken(phoneNumber: String) {
        // 전화번호 저장
        getSharedPreferences("FCMPrefs", MODE_PRIVATE).edit().apply {
            putString("phoneNumber", phoneNumber)
            apply()
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    FCMTokenManager.sendTokenToServer(
                        token,
                        phoneNumber,
                        object : FCMTokenManager.TokenRegistrationCallback {
                            override fun onSuccess(message: String) {
                                runOnUiThread {
                                    showRegistrationResultNotification(true, message)
                                }
                            }

                            override fun onFailure(error: String) {
                                runOnUiThread {
                                    showRegistrationResultNotification(false, error)
                                }
                            }
                        })
                } else {
                    showRegistrationResultNotification(false, "FCM 토큰 가져오기 실패")
                }
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "FCM 등록 알림"
            val descriptionText = "FCM 토큰 등록 결과를 알려주는 채널입니다"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showRegistrationResultNotification(success: Boolean, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(if (success) android.R.drawable.ic_dialog_info else android.R.drawable.ic_dialog_alert)
            .setContentTitle(if (success) "알림 등록 성공" else "알림 등록 실패")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
