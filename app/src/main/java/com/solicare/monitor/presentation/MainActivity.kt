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
import com.solicare.monitor.BuildConfig
import com.solicare.monitor.R
import com.solicare.monitor.data.prefs.DevicePrefs
import com.solicare.monitor.data.prefs.FcmPrefs
import com.solicare.monitor.data.prefs.UserPrefs
import com.solicare.monitor.data.repository.DeviceRepositoryImpl
import com.solicare.monitor.domain.util.JwtUtils
import com.solicare.monitor.presentation.dialog.OneButtonDialog
import com.solicare.monitor.presentation.dialog.TwoButtonDialog
import com.solicare.monitor.presentation.notification.AlertChannel
import com.solicare.monitor.presentation.notification.InfoChannel
import com.solicare.monitor.presentation.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val loggingTag = "MainActivity"
    private val baseUrl = BuildConfig.BASE_URL + "/"

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

    private var isLinkingDevice = false

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

        registerDevice()

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

    fun registerDevice() {
        val deviceUuid = devicePrefs.getDeviceUuid()
        val currentToken = fcmPrefs.getToken()
        val lastRegisteredToken = fcmPrefs.getLastRegisteredToken()
        Log.d(loggingTag, "Device UUID: $deviceUuid")
        Log.d(loggingTag, "Current FCM Token: $currentToken")
        Log.d(loggingTag, "Last Registered Token: $lastRegisteredToken")

        if (currentToken != lastRegisteredToken && !currentToken.isNullOrEmpty()) {
            if (deviceUuid.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d(loggingTag, "서버에 FCM 토큰 등록 시도: $currentToken")
                    val deviceRepository = DeviceRepositoryImpl(this@MainActivity)
                    val result = currentToken.let { deviceRepository.registerFcmToken(it) }
                    if (!result.isNullOrEmpty()) {
                        devicePrefs.saveDeviceUuid(result)
                        fcmPrefs.saveLastRegisteredToken(currentToken)
                        InfoChannel.send(
                            this@MainActivity,
                            getString(R.string.device_register_title),
                            getString(R.string.device_register_success)
                        )
                    } else {
                        InfoChannel.send(
                            this@MainActivity,
                            getString(R.string.device_register_title),
                            getString(R.string.device_register_fail)
                        )
                    }
                }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d(loggingTag, "서버에 FCM 토큰 갱신 시도: $currentToken")
                    val deviceRepository = DeviceRepositoryImpl(this@MainActivity)
                    val result =
                        deviceRepository.renewFcmToken(lastRegisteredToken ?: "", currentToken)
                    if (result) {
                        fcmPrefs.saveLastRegisteredToken(currentToken)
                        InfoChannel.send(
                            this@MainActivity,
                            getString(R.string.device_renew_title),
                            getString(R.string.device_renew_success)
                        )
                    } else {
                        InfoChannel.send(
                            this@MainActivity,
                            getString(R.string.device_renew_title),
                            getString(R.string.device_renew_fail)
                        )
                    }
                }
            }
        }
    }

    private fun linkDevice() {
        if (devicePrefs.isDeviceLinked()) return
        if (isLinkingDevice) {
            Log.d(loggingTag, "이미 디바이스 연결 시도 중, 중복 요청 방지")
            return
        }
        isLinkingDevice = true
        Log.d(loggingTag, "FCM 토큰이 멤버와 미연결 상태입니다. 디바이스 연결 시도")

        val deviceUuid = devicePrefs.getDeviceUuid()
        Log.d(loggingTag, "Device UUID: $deviceUuid")
        val memberUuid = userPrefs.getMemberUuid()
        Log.d(loggingTag, "Member UUID: $memberUuid")
        val accessToken = userPrefs.getJwtToken()
        Log.d(loggingTag, "Access Token: ${accessToken?.take(10)}...")

        if (deviceUuid.isNullOrEmpty() || memberUuid.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            Log.e(loggingTag, "디바이스 연결에 필요한 정보 부족, 연결 시도 중단")
            isLinkingDevice = false
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val deviceRepository = DeviceRepositoryImpl(this@MainActivity)
            val result = deviceRepository.linkDeviceToMember(accessToken, memberUuid, deviceUuid)
            isLinkingDevice = false
            if (result) {
                devicePrefs.setDeviceLinked(true)
            } else {
                InfoChannel.send(
                    this@MainActivity,
                    getString(R.string.device_link_member_title),
                    getString(R.string.device_link_member_fail)
                )
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createConfiguredWebView(): WebView {
        return WebView(this).apply {
            // useExternalFCMToken 쿠키 항상 설정
            val cookieManager = android.webkit.CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setCookie(baseUrl, "useExternalFCMToken=true; path=/")
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
                        Log.e("WebView", "Error: ${it.description}")
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // accessToken 쿠키 검사 및 UserPrefs 업데이트
                    checkAndUpdateJwtTokenFromCookie(url)
                    // 디바이스-멤버 연결 시도
                    linkDevice()
                }
            }
            loadUrl(baseUrl)
        }
    }

    // accessToken 쿠키를 읽어 UserPrefs와 비교 후 업데이트
    private fun checkAndUpdateJwtTokenFromCookie(url: String?) {
        if (url == null) return
        val cookieManager = android.webkit.CookieManager.getInstance()
        val cookies = cookieManager.getCookie(url) ?: return
        Log.d("WebView", "Cookies for $url: $cookies")
        val accessToken = cookies.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("accessToken=") }
            ?.substringAfter("=")
        if (!accessToken.isNullOrEmpty()) {
            val savedToken = userPrefs.getJwtToken()
            if (savedToken != accessToken) {
                Log.d("WebView", "accessToken(JWT) 변경 감지: '${accessToken}'")
                userPrefs.saveJwtToken(accessToken)
                val memberUuid = JwtUtils.extractUserUuidFromJwt(accessToken)
                if (!memberUuid.isNullOrEmpty()) {
                    userPrefs.saveMemberUuid(memberUuid)
                    Log.d("WebView", "Member UUID 추출 및 저장: $memberUuid")
                } else {
                    Log.e("WebView", "JWT에서 Member UUID 추출 실패")
                }
            }
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