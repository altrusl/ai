/**
 * Altai-Info
 * <p/>
 * Copyright (c) 2015-2016 Arnaud Renaud-Goud
 * Copyright (c) 2012-2015 Frederic Julian
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ga.ai.mobile;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

import ga.ai.mobile.utils.PrefUtils;
import ga.ai.mobile.provider.FeedDataContentProvider;

public class MainApplication extends Application {

    private static Context mContext;
    public static boolean RELEASE_BUILD;


    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        if (PrefUtils.getInt(PrefUtils.CURRENT_VERSION, 0) < 16) {
            clearApplicationData();
            PrefUtils.putInt(PrefUtils.CURRENT_VERSION, 16);
        }

        RELEASE_BUILD = BuildConfig.BUILD_TYPE.equalsIgnoreCase("release");


//        if (!RELEASE_BUILD) {
//            Locale locale = null;
//            android.content.res.Configuration config;
//            config = getBaseContext().getResources().getConfiguration();
//            locale = new Locale("ru", "RU");
//            Locale.setDefault(locale);
//            config.setLocale(locale);

//            PrefUtils.putBoolean(PrefUtils.PROXY_ENABLED, true);
//            PrefUtils.putString(PrefUtils.PROXY_TYPE, "0");
//            PrefUtils.putString(PrefUtils.PROXY_HOST, "192.168.221.2");
//            PrefUtils.putString(PrefUtils.PROXY_PORT, "8080");
//        }

        //BuildConfig.BUILD_TYPE

        if (!RELEASE_BUILD && false) {
//            FeedDataContentProvider.addFeed(this, "http://l.ru/index.php?format=feed&type=rss", "l.ru", false);
//                FeedDataContentProvider.addFeed(this, "http://feeds.feedburner.com/news-alt", "НГА", false);
//            FeedDataContentProvider.addFeed(this, "http://feeds.altai-info.ga/mobile-app/feeds/1.html", "Тестовая лента", false);
//            FeedDataContentProvider.addFeed(this, "http://193s2.168.122.2/2.xml", "Local 2", false);
//            FeedDataContentProvider.addFeed(this, "http://st3satic.feed.rbc.ru/rbc/internal/rss.rbc.ru/rbc.ru/mainnews.rss", "РБК", false);

        } else {
//            FeedDataContentProvider.addFeed(this, "http://feeds.feedburner.com/news-alt", "НГА", false);
        }

        PrefUtils.putBoolean(PrefUtils.IS_REFRESHING, false); // init
        PrefUtils.putBoolean(PrefUtils.REFRESH_ENABLED, false);
        PrefUtils.putBoolean(PrefUtils.LEFT_PANEL, true);
        PrefUtils.putBoolean(PrefUtils.SHOW_READ, true);
    }


    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if(appDir.exists()){
            String[] children = appDir.list();
            for(String s : children){
                if(!s.equals("lib")){
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s +" DELETED");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static void setContext(Context context) {
        mContext = context;
    }



}
