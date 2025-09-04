package com.solicare.monitor.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.solicare.monitor.R
import com.solicare.monitor.data.prefs.DevicePrefs
import com.solicare.monitor.data.prefs.FcmPrefs
import com.solicare.monitor.data.prefs.UserPrefs
import com.solicare.monitor.data.repository.DeviceRepositoryImpl
import com.solicare.monitor.presentation.dialog.OneButtonDialog
import com.solicare.monitor.presentation.dialog.TwoButtonDialog
import com.solicare.monitor.presentation.notification.AlertChannel
import com.solicare.monitor.presentation.notification.InfoChannel
import com.solicare.monitor.presentation.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val baseUrl = "https://www.solicare.kro.kr/"

    private lateinit var webView: WebView
    private lateinit var fcmPrefs: FcmPrefs
    private lateinit var userPrefs: UserPrefs
    private lateinit var devicePrefs: DevicePrefs
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var settingsLauncher: ActivityResultLauncher<Intent>

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
        InfoChannel.register(this)
        AlertChannel.register(this)
        settingsLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (!areAllPermissionsGranted()) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", packageName, null)
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
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun initializeApp() {
        if (::webView.isInitialized) return
        fcmPrefs = FcmPrefs(this)
        userPrefs = UserPrefs(this)
        devicePrefs = DevicePrefs(this)

        // FCM 토큰 서버 등록 로직
        val fcmToken = fcmPrefs.getFcmToken()
        val lastRegisteredToken = fcmPrefs.getLastRegisteredToken()
        Log.d("MainActivity", "FCM Token: $fcmToken")
        Log.d("MainActivity", "Last Registered Token: $lastRegisteredToken")
        if (!fcmToken.isNullOrEmpty() && fcmToken != lastRegisteredToken) {
            Log.d("MainActivity", "새로운 FCM 토큰 감지, InfoChannel 알림 및 서버 등록 시도")
            InfoChannel.send(
                this,
                getString(R.string.fcm_token_changed_message),
                getString(R.string.fcm_token_changed_title)
            )
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("MainActivity", "서버에 FCM 토큰 등록 시도: $fcmToken")
                val fcmRepository = DeviceRepositoryImpl(this@MainActivity)
                val result = fcmRepository.registerFcmToken(fcmToken)
                Log.d("MainActivity", "서버 등록 결과: $result")
                if (result) {
                    //TODO: 기존 토큰이 있다면 서버에서 삭제하는 로직 추가 고려
                    //TODO: 응답 DTO로부터 Device UUID 추출, FcmPrefs에 저장 고려
                    fcmPrefs.saveLastRegisteredToken(fcmToken)
                }
            }
        }

        webView = createConfiguredWebView()
        webView.setBackgroundColor(Color.TRANSPARENT)

        val container = FrameLayout(this).apply {
            fitsSystemWindows = true
            addView(
                webView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
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
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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
            ) != PackageManager.PERMISSION_GRANTED &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permission
                    )
        }
        if (permanentlyDenied) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
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