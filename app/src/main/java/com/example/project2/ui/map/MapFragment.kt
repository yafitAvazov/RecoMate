package com.example.project2.ui.map

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.project2.R
import com.example.project2.databinding.MapviewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : Fragment() {
    private var _binding: MapviewBinding? = null
    private val binding get() = _binding!!
    private var address: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString("address")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MapviewBinding.inflate(inflater, container, false)

        setupWebView()

        return binding.root
    }

    private fun focusOnAddress(address: String) {
        val jsCode = "focusOnLocation('${address.replace("'", "\\'")}');"
        Log.d("MapFragment", "Focusing on address: $address")
        binding.mapWebView.evaluateJavascript(jsCode, null)
    }


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
                if (event.action == MotionEvent.ACTION_UP) {
                    v.performClick()
                }
                v.onTouchEvent(event)
                true
            }

            WebView.setWebContentsDebuggingEnabled(true)

            // ✅ Use WebAppInterface instead of passing Fragment
            addJavascriptInterface(this@MapFragment, "Android")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("MapFragment", "WebView loaded: $url")

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
                    Toast.makeText(requireContext(),
                        context.getString(R.string.error_loading_map), Toast.LENGTH_SHORT).show()
                }
            }
        }


        binding.mapWebView.loadUrl("file:///android_asset/map.html")
    }

    @JavascriptInterface
    fun onSearchError(message: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
