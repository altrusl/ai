package ga.ai.mobile.adapter;

import android.app.Fragment;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

/**
 * Created by Алтай on 14.01.2017.
 */

public class Channel {

    private String name;
    private String url;
    private String groupName;
    private String desc;
    private boolean retrieveFullText;
    private String iconName;
    private BitmapDrawable icon;
    private String fragmentLayout;
    private Fragment fragment;
    private int type;


    private Uri uri;
    private int groupId;

    public Channel(String name, String desc, String groupName, String url, String iconName, Uri uri, int type, boolean retrieveFullText, String fragmentLayout) {
        this.name = name;
        this.desc = desc;
        this.url = url;
        this.groupName = groupName;
        this.iconName = iconName;
        this.uri = uri;
        this.type = type;
        this.retrieveFullText = retrieveFullText;
        this.fragmentLayout = fragmentLayout;
    }

    public static int ALL_FEEDS = 0;
    public static int STARRED = 1;
    public static int FEED = 2;
    public static int WEB_FRAGMENT = 3;



    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isRetrieveFullText() {
        return retrieveFullText;
    }

    public void setRetrieveFullText(boolean retrieveFullText) {
        this.retrieveFullText = retrieveFullText;
    }

    public BitmapDrawable getIcon() {
        return icon;
    }

    public void setIcon(BitmapDrawable icon) {
        this.icon = icon;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public Uri getUri() {
        return uri;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }
}
