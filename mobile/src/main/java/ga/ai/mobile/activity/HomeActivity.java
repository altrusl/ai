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

package ga.ai.mobile.activity;

import android.Manifest;
//import android.app.AlertDialog;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import ga.ai.mobile.Constants;
import ga.ai.mobile.R;
import ga.ai.mobile.adapter.Channel;
import ga.ai.mobile.adapter.ChannelsAdapter;
import ga.ai.mobile.adapter.ChildInfo;
import ga.ai.mobile.adapter.GroupInfo;
import ga.ai.mobile.fragment.EntriesListFragment;
import ga.ai.mobile.parser.OPML;
import ga.ai.mobile.provider.FeedData;
import ga.ai.mobile.provider.FeedData.EntryColumns;
import ga.ai.mobile.provider.FeedData.FeedColumns;
import ga.ai.mobile.service.FetcherService;
import ga.ai.mobile.service.RefreshService;
import ga.ai.mobile.utils.PrefUtils;
import ga.ai.mobile.utils.UiUtils;
import ga.ai.mobile.utils.VersionChecker;


public class HomeActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String STATE_CURRENT_DRAWER_POS = "STATE_CURRENT_DRAWER_POS";

    private static final String FEED_UNREAD_NUMBER = "(SELECT " + Constants.DB_COUNT + " FROM " + EntryColumns.TABLE_NAME + " WHERE " +
            EntryColumns.IS_READ + " IS NULL AND " + EntryColumns.FEED_ID + '=' + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID + ')';

    private static final String WHERE_UNREAD_ONLY = "(SELECT " + Constants.DB_COUNT + " FROM " + EntryColumns.TABLE_NAME + " WHERE " +
            EntryColumns.IS_READ + " IS NULL AND " + EntryColumns.FEED_ID + "=" + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID + ") > 0" +
            " OR (" + FeedColumns.IS_GROUP + "=1 AND (SELECT " + Constants.DB_COUNT + " FROM " + FeedData.ENTRIES_TABLE_WITH_FEED_INFO +
            " WHERE " + EntryColumns.IS_READ + " IS NULL AND " + FeedColumns.GROUP_ID + '=' + FeedColumns.TABLE_NAME + '.' + FeedColumns._ID +
            ") > 0)";

    private static final int LOADER_ID = 0;
    private static final int SEARCH_DRAWER_POSITION = -1;
    private static final int PERMISSIONS_REQUEST_IMPORT_FROM_OPML = 1;

//    private EntriesListFragment mEntriesFragment;
//    private WeatherFragment mWeatherFragment;
//    private ClassifiedFragment mClassifiedFragment;


    private DrawerLayout mDrawerLayout;
    private View mLeftDrawer;
//    private ListView mDrawerList;
//    private DrawerAdapter mDrawerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private FloatingActionButton fab;
    private final SharedPreferences.OnSharedPreferenceChangeListener mShowReadListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PrefUtils.SHOW_READ.equals(key)) {
                getLoaderManager().restartLoader(LOADER_ID, null, HomeActivity.this);

                if (fab != null) {
                    UiUtils.updateHideReadButton(HomeActivity.this, fab);
                }
            }
        }
    };
    private CharSequence mTitle;
    private BitmapDrawable mIcon;
    private int mCurrentDrawerPos;

    private boolean mCanQuit = false;

    private LinkedHashMap<String, GroupInfo> subjects = new LinkedHashMap<String, GroupInfo>();
    private ArrayList<GroupInfo> groupList = new ArrayList<GroupInfo>();

    private ChannelsAdapter channelsAdapter;
    private ExpandableListView simpleExpandableListView;

    private VersionChecker versionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

//        mEntriesFragment    = (EntriesListFragment) getFragmentManager().findFragmentById(R.id.entries_list_fragment);
//        mWeatherFragment    = (WeatherFragment) getFragmentManager().findFragmentById(R.id.weather_fragment);
//        mClassifiedFragment = (ClassifiedFragment) getFragmentManager().findFragmentById(R.id.classified_fragment);

        mTitle = getTitle();
        UiUtils.setPreferenceTheme(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLeftDrawer = findViewById(R.id.left_drawer);

        mLeftDrawer.setBackgroundColor((ContextCompat.getColor(getApplicationContext(), PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_primary_color : R.color.dark_primary_color)));
//        mDrawerList.setBackgroundColor((ContextCompat.getColor(getApplicationContext(), PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_background : R.color.dark_primary_color_light)));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, 0);
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);

            if (PrefUtils.getBoolean(PrefUtils.LEFT_PANEL, false)) {
                mDrawerLayout.openDrawer(mLeftDrawer);
            }
        }



        /*
         * start auto refreshing service
        */
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ENABLED, true)) {
            // starts the service independent to this activity
            startService(new Intent(this, RefreshService.class));
        } else {
            stopService(new Intent(this, RefreshService.class));
        }
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ON_OPEN_ENABLED, false)) {
            if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
                startService(new Intent(HomeActivity.this, FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }



        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && new File(OPML.BACKUP_OPML).exists()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setMessage(R.string.storage_request_explanation).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_IMPORT_FROM_OPML);
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_IMPORT_FROM_OPML);
            }
        }

        //get reference of the ExpandableListView
        simpleExpandableListView = (ExpandableListView) findViewById(R.id.simpleExpandableListView);

//        simpleExpandableListView.setIndicatorBoundsRelative(5, 70);
        simpleExpandableListView.setIndicatorBounds(5, 70);
//        simpleExpandableListView.setGroupIndicator(null);
        // create the adapter and load data
        channelsAdapter = new ChannelsAdapter(HomeActivity.this);
        //simpleExpandableListView.setChildIndicator(null);



        // setOnChildClickListener listener for child row click
        simpleExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                int position = ChannelsAdapter.getChildListPosition(channelsAdapter, groupPosition, childPosition);
                ChildInfo child = (ChildInfo) channelsAdapter.getChild(groupPosition, childPosition);
                int position = child.getNum();

                selectDrawerItem(position);

                int num = position + groupPosition + 1;
                for (int i = 0; i < groupPosition; i++) {
                    if (!simpleExpandableListView.isGroupExpanded(i)) {
                        num -= channelsAdapter.getGroupList().get(i).getChildList().size();
                    }
                }
                simpleExpandableListView.setItemChecked(num, true);
                if (mDrawerLayout != null) {
                    mDrawerLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDrawerLayout.closeDrawer(mLeftDrawer);
                        }
                    }, 50);
                }
                return true;
            }
        });

        // setOnGroupClickListener listener for group heading click
//        simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//            @Override
//            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//                //get the group header
//                GroupInfo headerInfo = groupList.get(groupPosition);
                //display it or do something with it

//                return false;
//            }
//        });


        /*
         * Hide Read Button
        */
        fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    UiUtils.displayHideReadButtonAction(HomeActivity.this);
                    return true;
                }
            });
            UiUtils.updateHideReadButton(HomeActivity.this, fab);
            UiUtils.addEmptyFooterView(simpleExpandableListView, 100);
        }

        if (savedInstanceState != null) {
            mCurrentDrawerPos = savedInstanceState.getInt(STATE_CURRENT_DRAWER_POS);
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);
    }



    //method to expand all groups
//    private void expandAll() {
//        int count = channelsAdapter.getGroupCount();
//        for (int i = 0; i < count; i++){
//            simpleExpandableListView.expandGroup(i);
//        }
//    }
//
//    //method to collapse all groups
//    private void collapseAll() {
//        int count = channelsAdapter.getGroupCount();
//        for (int i = 0; i < count; i++){
//            simpleExpandableListView.collapseGroup(i);
//        }
//    }




    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_DRAWER_POS, mCurrentDrawerPos);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.registerOnPrefChangeListener(mShowReadListener);
    }

    @Override
    protected void onPause() {
        PrefUtils.unregisterOnPrefChangeListener(mShowReadListener);
        super.onPause();
    }

    @Override
    public void finish() {
        if (mDrawerLayout != null) {
            if(mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
                mDrawerLayout.closeDrawer(mLeftDrawer);
                return;
            }
        }

        if (mCanQuit) {
            super.finish();
            return;
        }

        Toast.makeText(this, R.string.back_again_to_quit, Toast.LENGTH_SHORT).show();
        mCanQuit = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCanQuit = false;
            }
        }, 3000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // We reset the current drawer position
        selectDrawerItem(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickHideRead(View view) {
        if (!PrefUtils.getBoolean(PrefUtils.SHOW_READ, true)) {
            PrefUtils.putBoolean(PrefUtils.SHOW_READ, true);
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_pink)));
            fab.setImageResource(R.drawable.ic_visibility);
        } else {
            PrefUtils.putBoolean(PrefUtils.SHOW_READ, false);
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.fab_gray)));
            fab.setImageResource(R.drawable.ic_visibility_off);
        }
    }

    public void onClickEditFeeds(View view) {
        startActivity(new Intent(this, EditFeedsListActivity.class));
    }

    public void onClickSearch(View view) {
        selectDrawerItem(SEARCH_DRAWER_POSITION);
        if (mDrawerLayout != null) {
            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.closeDrawer(mLeftDrawer);
                }
            }, 50);
        }
    }

    public void onClickSendToFriend(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sendToFriendMsg)
                .setPositiveButton(R.string.sendToFriendOkBtn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(Intent.createChooser(
                                new Intent(Intent.ACTION_SEND)
                                        .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.sendToFriendSbj))
                                        .putExtra(Intent.EXTRA_TEXT, getString(R.string.sendToFriendTxt))
                                        .setType(Constants.MIMETYPE_TEXT_PLAIN), getString(R.string.sendToFriendChooseMethod))
                        );
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.sendToFriendCancelBtn, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder.show();
    }

    public void onClickSettings(View view) {
        startActivity(new Intent(this, GeneralPrefsActivity.class));
    }

    public static class customWebView extends WebView {
        public customWebView(Context context)
        {
            super(context);
        }

        // Note this!
        @Override
        public boolean onCheckIsTextEditor()
        {
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!hasFocus())
                        requestFocus();
                    break;
            }

            return super.onTouchEvent(ev);
        }
    }

    public void onClickHelp(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.helpMsgTitle);

        WebView myWebView = new customWebView(this.getBaseContext());
//        myWebView.loadData(getString(R.string.helpMsg), "text/html", "ru_RU");
//        myWebView.loadDataWithBaseURL(null, getString(R.string.helpMsg), "text/html", "ru_RU", null);
        myWebView.loadUrl("file:///android_asset/helpMsg.html");
        myWebView.requestFocus(View.FOCUS_DOWN);
        builder.setView(myWebView)
                .setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }


    public void onClickPostAd(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.postAd);

//        AssetManager assetManager = getAssets();
//        // To load text file
//        InputStream input;
//        String postAdHTML = "";
//        try {
//            input = assetManager.open("postAd.html");
//            int size = input.available();
//            byte[] buffer = new byte[size];
//            input.read(buffer);
//            input.close();
//            postAdHTML = new String(buffer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        WebView myWebView = new HomeActivity.customWebView(this);
//        myWebView.loadDataWithBaseURL(null, postAdHTML, "text/html", "ru_RU", null);
        myWebView.loadUrl("file:///android_asset/post_ad_form.html");
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.requestFocus(View.FOCUS_DOWN);
        builder.setView(myWebView)
                .setPositiveButton("Закрыть", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        CursorLoader cursorLoader = new CursorLoader(this, FeedColumns.GROUPED_FEEDS_CONTENT_URI, new String[]{FeedColumns._ID, FeedColumns.URL, FeedColumns.NAME,
                FeedColumns.IS_GROUP, FeedColumns.ICON, FeedColumns.LAST_UPDATE, FeedColumns.ERROR, FEED_UNREAD_NUMBER},
                PrefUtils.getBoolean(PrefUtils.SHOW_READ, true) ? "" : WHERE_UNREAD_ONLY, null, null
        );
        cursorLoader.setUpdateThrottle(Constants.UPDATE_THROTTLE_DELAY);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // attach the adapter to the expandable list view
        simpleExpandableListView.setAdapter(channelsAdapter);
        channelsAdapter.setCursor(cursor);

        simpleExpandableListView.expandGroup(0);
        simpleExpandableListView.expandGroup(3);

        // First open => we open the drawer for you
        if (PrefUtils.getBoolean(PrefUtils.FIRST_OPEN, true)) {
            PrefUtils.putBoolean(PrefUtils.FIRST_OPEN, false);
            if (mDrawerLayout != null) {
                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.openDrawer(mLeftDrawer);
                    }
                }, 500);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.welcome_title).setMessage(R.string.welcome_message).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        }

        try {
            TelephonyManager tMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            if (versionChecker == null) {
                versionChecker = new VersionChecker();
                versionChecker.setContext(this);
                versionChecker.setmPhone(mPhoneNumber);
                versionChecker.setaID(androidID);
            }
            versionChecker.check();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        channelsAdapter.setCurrentChannel(0);

//        Toast.makeText(getBaseContext(), PrefUtils.getString("time", "empty"), Toast.LENGTH_LONG).show();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if (channelsAdapter == null)
            return;

        channelsAdapter.setCursor(null);
    }


    private void selectDrawerItem(int position) {

        mCurrentDrawerPos = position;

        EntriesListFragment elf = (EntriesListFragment)channelsAdapter.getChannels().get(0).getFragment();
        Uri newUri = FeedData.EntryColumns.ALL_ENTRIES_CONTENT_URI;
        boolean showFeedInfo = true;

        if (position == SEARCH_DRAWER_POSITION) {
            newUri = EntryColumns.SEARCH_URI(elf.getCurrentSearch());
            elf.setData(newUri, showFeedInfo);
        } else {
            channelsAdapter.setCurrentChannel(position);
            Channel ch = channelsAdapter.getCurrentChannel();
            if (ch.getType() != Channel.WEB_FRAGMENT) {
                if (ch.getType() == Channel.FEED) {
                    newUri = Uri.parse(ch.getUri().toString() + "/entries");
                    showFeedInfo = false;
                }
                ((EntriesListFragment)ch.getFragment()).setData(newUri, showFeedInfo);
            }
        }



//
//
//            switch (position) {
//                case SEARCH_DRAWER_POSITION:
//                    newUri = EntryColumns.SEARCH_URI(mEntriesFragment.getCurrentSearch());
//                    break;
//                case 0:
//                    newUri = EntryColumns.ALL_ENTRIES_CONTENT_URI;
//                    break;
//                case 1:
//                    newUri = EntryColumns.FAVORITES_CONTENT_URI;
//                    break;
//                default:
//                    long feedOrGroupId = channelsAdapter.getItemId(position);
//                    if (channelsAdapter.isItemAGroup(position)) {
//                        newUri = EntryColumns.ENTRIES_FOR_GROUP_CONTENT_URI(feedOrGroupId);
//                    } else {
//                        byte[] iconBytes = channelsAdapter.getItemIcon(position);
//                        Bitmap bitmap = UiUtils.getScaledBitmap(iconBytes, 24);
//                        if (bitmap != null) {
//                            mIcon = new BitmapDrawable(getResources(), bitmap);
//                        }
//
//                        newUri = EntryColumns.ENTRIES_FOR_FEED_CONTENT_URI(feedOrGroupId);
//                        showFeedInfo = false;
//                    }
//
//                    mTitle = channelsAdapter.getItemName(position);
//                    break;
//            }

//
//        if (!newUri.equals(mEntriesFragment.getUri())) {
//            mEntriesFragment.setData(newUri, showFeedInfo);
//        }



        refreshTitle(0);
    }


    public void refreshTitle(int mNewEntriesNumber) {

        if (channelsAdapter.getCurrentChannel() == null) {
            channelsAdapter.setCurrentChannel(0);
        }

        if (mCurrentDrawerPos == SEARCH_DRAWER_POSITION) {
            getSupportActionBar().setTitle(android.R.string.search_go);
            return;
        }

        getSupportActionBar().setTitle(channelsAdapter.getCurrentChannel().getName());
        //getSupportActionBar().setIcon(channelsAdapter.getCurrentChannel().getIcon());

        if (channelsAdapter.getCurrentChannel().getType() != Channel.WEB_FRAGMENT && mNewEntriesNumber != 0) {
            getSupportActionBar().setTitle(getSupportActionBar().getTitle().toString() + " (" + String.valueOf(mNewEntriesNumber) + ")");
        }


        invalidateOptionsMenu();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_IMPORT_FROM_OPML:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                // Perform automated import of the backup
                                OPML.importFromFile(OPML.BACKUP_OPML);
                            }
                            catch (Exception ig){
                            }
                        }
                    }).start();
                }
        }
    }
}
