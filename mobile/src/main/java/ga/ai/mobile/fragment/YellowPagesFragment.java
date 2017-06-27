package ga.ai.mobile.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import ga.ai.mobile.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class YellowPagesFragment extends BaseWebViewFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contView = inflater.inflate(R.layout.fragment_yellow_pages, container, false);
        mWebView = (WebView) contView.findViewById(R.id.ypWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setBuiltInZoomControls(true);

//        url = "file:///android_asset/yp.html";
        url = "http://altai-info.ga/mobile-app/yellow-pages/";
        setCustomErrorPage(true);
        mWebView.loadUrl(url);
//        webViewAsyncLoad(url);
        return contView;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_classified, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_classified_refresh: {
                mWebView.loadUrl(url);
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}