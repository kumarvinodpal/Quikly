package com.quikly.in.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.quikly.in.R;

import org.imaginativeworld.oopsnointernet.callbacks.ConnectionCallback;
import org.imaginativeworld.oopsnointernet.dialogs.signal.DialogPropertiesSignal;
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getCanonicalName();
    private WebView webView;
    String ShowOrHideWebViewInitialUse = "show";
    private ProgressBar spinner;
    String myUrl = "https://quikly.in/panel/login/login.php";
    //String myUrl = "https://www.javatpoint.com/java-tutorial";
    String ua = "Chrome/30.0.0.0 Mobile Safari/537.36";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** Hiding Title bar of this activity screen */
        //getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        /** Making this activity, full screen */
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /** Sets a layout for this activity */
        setContentView(R.layout.activity_main);
        initView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {
        noInternetPendulum();
        webView = (WebView) findViewById(R.id.webView);
        //spinner = (ProgressBar) findViewById(R.id.progressBar1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        //webView.getSettings().setAllowFileAccess(true);//No access to files is allowed, access is allowed by default
        //webView.getSettings().setAllowFileAccessFromFileURLs(true);//A file is allowed to access the contents of other files
        //webView.getSettings().setAllowUniversalAccessFromFileURLs(true);//Whether you can access any original starting content
        //webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        //webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        //webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUserAgentString(ua);
        //webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        webView.setWebViewClient(new CustomWebViewClient());
        //webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webView.loadUrl(myUrl);
        webView.setDownloadListener(new DownloadListener() {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            public void onDownloadStart(final String url, final String userAgent, String contentDisposition, String mimetype, long contentLength) {
                progressDialog.show();
                //Checking runtime permission for devices above Marshmallow.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG, "Permission is granted");
                        downloadDialog(url, userAgent, contentDisposition, mimetype);

                    } else {

                        Log.v(TAG, "Permission is revoked");
                        //requesting permissions.
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

                    }
                } else {
                    //Code for devices below API 23 or Marshmallow
                    Log.v(TAG, "Permission is granted");
                    downloadDialog(url, userAgent, contentDisposition, mimetype);

                }
            }
        });

    }

    //downloadDialog Method

    public void downloadDialog(final String url, final String userAgent, String contentDisposition, String mimetype) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        //getting filename from url.
        final String finalFileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
        //String finalFileName = finalName.replace(".bin", "");
        //alertdialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //title of alertdialog
        //builder.setTitle(R.string.download_title);
        //message of alertdialog
        builder.setMessage("Are you sure, for downloading this pdf file:: \n" +
                "" + finalFileName + ".pdf");
        //if Yes button clicks.

        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                //DownloadManager.Request created with url.
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                //cookie
                String cookie = CookieManager.getInstance().getCookie(url);
                //Add cookie and User-Agent to request
                request.addRequestHeader("Cookie", cookie);
                request.addRequestHeader("User-Agent", userAgent);
                //file scanned by MediaScannar
                request.allowScanningByMediaScanner();
                //Download is visible and its progress, after completion too.
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                //DownloadManager created
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                //Saving files in Download folder
                String path = finalFileName + ".pdf";
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path);
                //download enqued
                downloadManager.enqueue(request);
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancel the dialog if Cancel clicks
                dialog.cancel();
                webView.goBack();
            }

        });
        //alertdialog shows.
        builder.show();

    }

    /**
     * This allows for a splash screen
     * Hide elements once the page loads
     * Show custom error page
     * Resolve issue with SSL certificate
     **/
    private class CustomWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //view.loadUrl(url);
            //return true;
//            if (url.endsWith(".pdf")) {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
//                // if want to download pdf manually create AsyncTask here
//                // and download file
//                return true;
//            }

            if (URLUtil.isNetworkUrl(url)) {
                return false;
            }
            if (appInstalledOrNot(url)) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(MainActivity.this, "App is not installed in your phone.", Toast.LENGTH_SHORT).show();
            }

            if (url.startsWith("tel:") || url.startsWith("whatsapp:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            }

            if (url.startsWith("mailto:")) {

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, "message");
                startActivity(Intent.createChooser(share, "Title of the dialog the system will open"));
                view.reload();
                return true;
            }
            return false;
        }

        // Handle SSL issue
        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(R.string.notification_error_ssl_cert_invalid);
            builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.proceed();
                }
            });

            builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    handler.cancel();
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            // only make it invisible the FIRST time the app is run
            if (ShowOrHideWebViewInitialUse.equals("show")) {
                webView.setVisibility(webView.INVISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            ShowOrHideWebViewInitialUse = "hide";
            //spinner.setVisibility(View.GONE);
            view.setVisibility(webView.VISIBLE);
            super.onPageFinished(view, url);

        }

        // Show custom error page
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            myUrl = view.getUrl();
//            setContentView(R.layout.error);
//            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {

                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        builder.setMessage(R.string.exit_app);

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                finish();
                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Do nothing
                            }
                        });

                        final AlertDialog dialog = builder.create();

                        dialog.show();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    /* Retry Loading the page */

    /*public void tryAgain(View v) {

        setContentView(R.layout.activity_main);
        initView();
    }*/

    private void noInternetPendulum() {
        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder builder = new NoInternetDialogSignal.Builder(
                this,
                getLifecycle()
        );

        DialogPropertiesSignal properties = builder.getDialogProperties();

        properties.setConnectionCallback(new ConnectionCallback() { // Optional
            @Override
            public void hasActiveConnection(boolean hasActiveConnection) {
                // ...
            }
        });

        properties.setCancelable(false); // Optional
        properties.setNoInternetConnectionTitle("No Internet"); // Optional
        properties.setNoInternetConnectionMessage("Check your Internet connection and try again"); // Optional
        properties.setShowInternetOnButtons(true); // Optional
        properties.setPleaseTurnOnText("Please turn on"); // Optional
        properties.setWifiOnButtonText("Wifi"); // Optional
        properties.setMobileDataOnButtonText("Mobile data"); // Optional

        properties.setOnAirplaneModeTitle("No Internet"); // Optional
        properties.setOnAirplaneModeMessage("You have turned on the airplane mode."); // Optional
        properties.setPleaseTurnOffText("Please turn off"); // Optional
        properties.setAirplaneModeOffButtonText("Airplane mode"); // Optional
        properties.setShowAirplaneModeOffButtons(true); // Optional

        builder.build();
    }
}