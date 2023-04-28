package com.example.miniapp_downloader;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class download_miniapp {

    private Context mContext;
    private WebView mWebView;

    public download_miniapp(Context context, WebView webView) {
        mContext = context;
        mWebView = webView;
        mWebView.getSettings().setAllowContentAccess(true);
        mWebView.getSettings().setAllowFileAccess(true);
    }

    public void downloadMiniApp(String url) {
        // Check if mini app is already downloaded and stored in cache

        File cacheDir = mContext.getCacheDir();
        File miniAppDir = new File(cacheDir, "mini_app");
        File miniAppFile = new File(miniAppDir, "index.html");
        if (miniAppFile.exists()) {
            // Load mini app from cache
            mWebView.loadUrl("file://" + miniAppFile.getAbsolutePath());
            return;
        }

        // Download mini app from server
        new DownloadMiniAppTask().execute(url, miniAppDir.getAbsolutePath(), miniAppFile.getAbsolutePath());
    }

    private class DownloadMiniAppTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String url = params[0];
            String miniAppDirPath = params[1];
            String miniAppFilePath = params[2];

            try {
                URL urlObject = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObject.openConnection();
                conn.connect();
                InputStream input = conn.getInputStream();
                ZipInputStream zipInput = new ZipInputStream(input);

                // Create mini app directory if it doesn't exist
                File miniAppDir = new File(miniAppDirPath);
                if (!miniAppDir.exists()) {
                    miniAppDir.mkdirs();
                }

                // Unpack the zip file
                while (true) {
                    ZipEntry entry = zipInput.getNextEntry();
                    if (entry == null) {
                        break;
                    }
                    String entryName = entry.getName();
                    File entryFile = new File(miniAppDirPath, entryName);
                    if (entry.isDirectory()) {
                        entryFile.mkdirs();
                    } else {
                        FileOutputStream output = new FileOutputStream(entryFile);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zipInput.read(buffer)) > 0) {
                            output.write(buffer, 0, len);
                        }
                        output.close();
                    }
                }

                return true;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // Load mini app in WebView
                mWebView.loadUrl("file://" + mContext.getCacheDir() + "/mini_app/index.html");
            }
        }
    }

}
