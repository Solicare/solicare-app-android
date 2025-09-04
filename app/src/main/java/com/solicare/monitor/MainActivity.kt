package com.solicare.monitor

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.solicare.monitor.data.prefs.DevicePrefs
import com.solicare.monitor.data.prefs.FcmPrefs
import com.solicare.monitor.data.prefs.UserPrefs
import com.solicare.monitor.permission.PermissionHelper
import com.solicare.monitor.presentation.dialog.OneButtonDialog
import com.solicare.monitor.presentation.dialog.TwoButtonDialog

class MainActivity : AppCompatActivity() {
    private val baseUrl = "https://www.solicare.kro.kr/"

    private lateinit var webView: WebView
    private lateinit var fcmPrefs: FcmPrefs
    private lateinit var userPrefs: UserPrefs
    private lateinit var devicePrefs: DevicePrefs
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var settingsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    private val requiredPermissions: Array<String> by lazy {
        val list = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        list.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        settingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (!areAllPermissionsGranted()) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", packageName, null)
                    }
                    TwoButtonDialog(
                        this,
                        getString(R.string.permission_required_title),
                        getString(R.string.permission_required_settings_message),
                        getString(R.string.permission_required_settings_button), // 설정으로 가기
                        getString(R.string.exit_app), // 앱 종료
                        onPositive = { settingsLauncher.launch(intent) },
                        onNegative = { finishAffinity() },
                        isCancelable = false
                    ).show()
                } else {
                    initializeApp()
                }
            }
        permissionHelper = PermissionHelper(
            context = this,
            launcher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                permissionHelper.handleResult(permissions)
            },
            permissions = requiredPermissions,
            onGranted = { initializeApp() },
            onDenied = { handlePermissionDenied() }
        )
        if (!areAllPermissionsGranted()) {
            OneButtonDialog(
                this,
                getString(R.string.permission_required_title),
                getString(R.string.permission_required_message),
                getString(R.string.permission_required_confirm),
                onClick = { permissionHelper.checkAndRequestPermissions() }
            ).show()
        } else {
            initializeApp()
        }
    }

    override fun onResume() {
        super.onResume()
        if (areAllPermissionsGranted()) {
            initializeApp()
        }
    }

    private fun areAllPermissionsGranted(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initializeApp() {
        if (::webView.isInitialized) return
        fcmPrefs = FcmPrefs(this)
        userPrefs = UserPrefs(this)
        devicePrefs = DevicePrefs(this)

        webView = createConfiguredWebView()
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val container = android.widget.FrameLayout(this).apply {
            fitsSystemWindows = true
            addView(
                webView, android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }
        setContentView(container)

        ViewCompat.setOnApplyWindowInsetsListener(webView) { v, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = systemBarsInsets.top,
                bottom = systemBarsInsets.bottom
            )
            insets
        }

        onBackPressedDispatcher.addCallback(this) {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                finish()
            }
        }
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

    private fun handlePermissionDenied() {
        val deniedPermissions = permissionHelper.getLastDeniedPermissions()
        val permanentlyDenied = deniedPermissions.any { permission ->
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permission
                    )
        }
        if (permanentlyDenied) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            TwoButtonDialog(
                this,
                getString(R.string.permission_required_title),
                getString(R.string.permission_required_settings_message),
                getString(R.string.permission_required_settings_button), // 설정으로 가기
                getString(R.string.exit_app), // 앱 종료
                onPositive = { settingsLauncher.launch(intent) },
                onNegative = { finishAffinity() },
                isCancelable = false
            ).show()
        } else {
            OneButtonDialog(
                this,
                getString(R.string.permission_required_title),
                getString(R.string.permission_required_retry_message),
                getString(R.string.permission_required_retry_button),
                onClick = { permissionHelper.checkAndRequestPermissions() }
            ).show()
        }
    }
}
