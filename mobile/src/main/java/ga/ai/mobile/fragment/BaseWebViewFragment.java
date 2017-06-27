package ga.ai.mobile.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;

import ga.ai.mobile.utils.NetworkUtils;


public class BaseWebViewFragment extends Fragment {


    protected View contView;
    protected WebView   mWebView;
    protected String url;


    protected void setCustomErrorPage(boolean cust) {
        if (cust) {
            mWebView.setWebViewClient(new WebViewClient() {
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    mWebView.loadUrl("file:///android_asset/connection_error.html");
                }
            });
        }
}



    protected void webViewAsyncLoad(String url) {
        new webViewLoadAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
    }

    protected class webViewLoadAsyncTask extends AsyncTask<String, Integer, String> {

        private String data = "";

        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            mWebView.loadDataWithBaseURL("http://altai-info.ga/mobile-app/classified/", data, "text/html", "UTF-8", null);
        }

        protected String doInBackground(String... url) {
            try {
                byte[] content = NetworkUtils.downloadHtmlAsBytes(url[0]);
                if (content != null) {
                    data = new String(content, "utf-8");
                }
            } catch (UnsupportedEncodingException e) {
                Log.e("NetworkUtils", "downloadHtml: ", e);
            }
            return data;
        }
    }
}
