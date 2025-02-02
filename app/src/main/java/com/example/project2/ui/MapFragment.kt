package com.example.project2.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.project2.databinding.MapviewBinding

class MapFragment : Fragment() {
    private var _binding: MapviewBinding? = null
    private val binding get() = _binding!!
    private var address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString("address")
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapviewBinding.inflate(inflater, container, false)

        setupWebView()

        return binding.root
    }

    @SuppressLint("JavascriptInterface", "ClickableViewAccessibility")
    private fun setupWebView() {
        binding.mapWebView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                setSupportZoom(true)
                builtInZoomControls = false
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                v.onTouchEvent(event)
            }

            WebView.setWebContentsDebuggingEnabled(true)
            addJavascriptInterface(this@MapFragment, "Android")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("MapFragment", "WebView loaded: $url")

                    // כאשר המפה מוכנה, אנו מנסים להתמקד בכתובת
                    address?.let {
                        focusOnAddress(it)
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e("MapFragment", "WebView error: ${error?.description}")
                    Toast.makeText(requireContext(), "Error loading map", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // טעינת הקובץ רק פעם אחת
        binding.mapWebView.loadUrl("file:///android_asset/map.html")
    }

    private fun focusOnAddress(address: String) {
        val jsCode = "focusOnLocation('${address.replace("'", "\\'")}');"
        Log.d("MapFragment", "Focusing on address: $address")
        binding.mapWebView.evaluateJavascript(jsCode, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
