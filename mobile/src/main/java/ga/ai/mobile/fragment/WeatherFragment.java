package ga.ai.mobile.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.UnsupportedEncodingException;

import ga.ai.mobile.R;
import ga.ai.mobile.utils.NetworkUtils;
import ga.ai.mobile.utils.PrefUtils;


public class WeatherFragment extends BaseWebViewFragment {


    private Menu menu;
    private View prefsView;
    private RadioGroup radioGroup;
    private AlertDialog prefsDialog;
    private AlertDialog.Builder builder;
    private String[] weatherSpot;
    private String[] gmId;
    private String selUrl;

    public WeatherFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        contView = inflater.inflate(R.layout.fragment_weather, container, true);
        prefsView = inflater.inflate(R.layout.fragment_weather_prefs, container, true);
        radioGroup = (RadioGroup)prefsView.findViewById(R.id.radioGroup);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 10, 10, 10);
        radioGroup.setLayoutParams(params);

        RadioButton spotRadioButton;

        for(int i = 0; i < weatherSpot.length; i++) {
            spotRadioButton = new RadioButton(getActivity());

            // порядковый номер в списке
            int ind = weatherSpot[i].indexOf(':');
            // номер в Гисметео
            gmId[i] = weatherSpot[i].substring(ind + 1);

            spotRadioButton.setText(weatherSpot[i].substring(0, ind));
            spotRadioButton.setId(i+1);

            radioGroup.addView(spotRadioButton);
        }

        radioGroup.check(PrefUtils.getInt(PrefUtils.WEATHER_REGION, 0));

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_custom_feed)
                .setMessage(R.string.choose_weather_spot)
                .setView(prefsView)
                .setPositiveButton(R.string.set_weather_spot, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int selItem = radioGroup.getCheckedRadioButtonId();
                        if ( selItem < 1) {
                            selItem = 1;
                        }
                        PrefUtils.putInt(PrefUtils.WEATHER_REGION, selItem);
                        mWebView.loadUrl(url + "?gmId=" + gmId[selItem-1]);
                        dialog.cancel();
                    }
                });
        prefsDialog = builder.create();

        mWebView = (WebView) contView.findViewById(R.id.weatherWebView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");

        int checked = radioGroup.getCheckedRadioButtonId() > 0 ? radioGroup.getCheckedRadioButtonId()-1 : 0;

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError (WebView view, int errorCode, String description, String failingUrl) {
                mWebView.loadUrl("file:///android_asset/connection_error.html");
            }
        });

        selUrl = url + "?gmId=" + gmId[checked];
//        mWebView.loadUrl(url + "?gmId=" + gmId[checked]);
        setCustomErrorPage(true);
        webViewAsyncLoad(selUrl);

        return contView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        url = "http://altai-info.ga/mobile-app/weather/retrieve_forecast.php";

        weatherSpot = getResources().getStringArray(R.array.weatherSpot);
        gmId = new String[weatherSpot.length];

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.menu_weather, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_weather_refresh: {
                mWebView.loadUrl(url + "?gmId=" + gmId[PrefUtils.getInt(PrefUtils.WEATHER_REGION, 1)-1]);
                break;
            }
            case R.id.menu_weather_settings: {
                prefsDialog.show();
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
