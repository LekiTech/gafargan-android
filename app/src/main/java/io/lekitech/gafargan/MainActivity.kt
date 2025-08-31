package io.lekitech.gafargan

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    // Base URL to handle relative paths in your local site
    val baseUrl = "file:///android_asset/";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val webView = WebView(this)

        // Go edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = FrameLayout(this)
        val webView = android.webkit.WebView(this)
        setContentView(root)

        // Force dark icons in system bars (status & navigation)
        val insetsController = WindowInsetsControllerCompat(window, root)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

        root.addView(webView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        // Apply safe-area padding
        ViewCompat.setOnApplyWindowInsetsListener(webView) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottom = maxOf(bars.bottom, ime.bottom)

            root.setPadding(bars.left, bars.top, bars.right, bottom)
            insets//WindowInsetsCompat.CONSUMED
        }

        webView.apply {
            webViewClient = object : android.webkit.WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    Log.e("WebView", "Error: ${error?.description}")
                }
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url.toString()
                    if (url.startsWith(baseUrl)) {
                        return false // Allow loading local files
                    }
                    return true // Block all other URLs
                }
            }
            webChromeClient = android.webkit.WebChromeClient()
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.allowFileAccess = true
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true

            // Load the local index.html file from the assets folder
            loadUrl(baseUrl + "index.html")
        }

        // Handle back button to navigate within the WebView
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    finish() // Exit the app if there's no web history
                }
            }
        })
    }
}
