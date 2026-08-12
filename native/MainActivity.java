package com.botto.webview2;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.widget.Toast;
import org.apache.cordova.*;
import java.util.Locale;

public class MainActivity extends CordovaActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;
    private boolean isTtsInitialized = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();

        tts = new TextToSpeech(this, this);

        if (appView != null && appView.getEngine() != null) {
            org.apache.cordova.engine.SystemWebViewEngine engine = 
                (org.apache.cordova.engine.SystemWebViewEngine) appView.getEngine();
            android.webkit.WebView webView = (android.webkit.WebView) engine.getView();

            // Add JavaScript Interface for Native TTS
            webView.addJavascriptInterface(new BottoNativeTTSBridge(this), "BottoNativeTTS");

            // Setup Download Listener for APKs and files
            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                    try {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setMimeType(mimeType);
                        String cookies = CookieManager.getInstance().getCookie(url);
                        request.addRequestHeader("cookie", cookies);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Mengunduh file dari Botto WebView...");
                        String filename = URLUtil.guessFileName(url, contentDisposition, mimeType);
                        request.setTitle(filename);
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

                        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                        Toast.makeText(getApplicationContext(), "Mengunduh: " + filename, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Gagal mengunduh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("id", "ID"));
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.US);
            }
            isTtsInitialized = true;
        }
    }

    public class BottoNativeTTSBridge {
        Context mContext;

        BottoNativeTTSBridge(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void speak(String text) {
            if (isTtsInitialized && tts != null) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "BottoTTSId");
            }
        }

        @JavascriptInterface
        public void stop() {
            if (tts != null) {
                tts.stop();
            }
        }

        @JavascriptInterface
        public boolean isAvailable() {
            return isTtsInitialized;
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
