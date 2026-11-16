package se.larsves.portalskarm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/** Tunt skal: visar Portal-skärmens webbsida i helskärm och håller skärmen vaken. */
public class MainActivity extends Activity {

    // Adressen till sidan på GitHub Pages
    private static final String HOME_URL = "https://larsves.github.io/portal-skarm/";
    private static final String SPOTIFY = "com.spotify.music";

    private WebView web;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(7, 11, 31));
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);           // Spotify-inloggning och ✕ på hämtningen sparas här
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        web.addJavascriptInterface(new Bridge(), "PortalApp");
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String url = req.getUrl().toString();
                if (url.startsWith("http://") || url.startsWith("https://")) return false; // stanna i appen
                if (url.startsWith("spotify:") || url.startsWith("intent:")) { openSpotify(); return true; }
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                // Ingen uppkoppling? Försök igen om 30 sekunder.
                if (req.isForMainFrame()) handler.postDelayed(() -> web.loadUrl(freshUrl()), 30_000);
            }
        });

        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(freshUrl());
    }

    /** Lägger till en tidsstämpel så att appen alltid hämtar senaste versionen av sidan. */
    private static String freshUrl() {
        return HOME_URL + "?v=" + System.currentTimeMillis();
    }

    private void openSpotify() {
        Intent i = getPackageManager().getLaunchIntentForPackage(SPOTIFY);
        if (i != null) startActivity(i);
    }

    private class Bridge {
        @JavascriptInterface
        public void openSpotify() { handler.post(MainActivity.this::openSpotify); }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        // Bakåt går tillbaka inne i sidan (t.ex. från Spotify-inloggningen) innan appen stängs
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
}
