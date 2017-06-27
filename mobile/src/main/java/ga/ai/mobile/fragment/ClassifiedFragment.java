package ga.ai.mobile.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ga.ai.mobile.R;
import ga.ai.mobile.utils.PrefUtils;

/**
 * A placeholder fragment containing a simple view.
 */
public class ClassifiedFragment extends BaseWebViewFragment {


        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contView = inflater.inflate(R.layout.fragment_classified, container, false);
        mWebView = (WebView) contView.findViewById(R.id.classifiedWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);

        setCustomErrorPage(true);
//        url = "http://altai-info.ga/mobile-app/classified/";
        url = "http://altai-info.ga/mobile-app/classified/";

        webViewAsyncLoad(url);
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


    @Override
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