package com.solicare.monitor

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.solicare.monitor.data.prefs.DevicePrefs
import com.solicare.monitor.data.prefs.FcmPrefs
import com.solicare.monitor.data.prefs.UserPrefs
import com.solicare.monitor.permission.PermissionHelper


class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    private val baseUrl = "https://www.solicare.kro.kr/"

    private lateinit var fcmPrefs: FcmPrefs
    private lateinit var userPrefs: UserPrefs
    private lateinit var devicePrefs: DevicePrefs
    private lateinit var permissionHelper: PermissionHelper

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionHelper.handleResult(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fcmPrefs = FcmPrefs(this)
        userPrefs = UserPrefs(this)
        devicePrefs = DevicePrefs(this)

        permissionHelper = buildPermissionHelper()

        webView = createConfiguredWebView()
        setContentView(webView)

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
    }

    private fun buildPermissionHelper(): PermissionHelper {
        return PermissionHelper(
            context = this,
            launcher = requestPermissionLauncher,
            permissions = arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS
            ),
            onGranted = {
                // TODO: after permissions are granted, register FCM token
            },
            onDenied = {
                // TODO: handle permission denial
                Log.e("Permission", "Required permissions were denied.")
            }
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createConfiguredWebView(): WebView {
        return WebView(this).apply {
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
    }

}
