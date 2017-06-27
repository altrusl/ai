package ga.ai.mobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import ga.ai.mobile.MainApplication;
import ga.ai.mobile.R;
import ga.ai.mobile.provider.FeedData;
import ga.ai.mobile.provider.FeedDataContentProvider;
import ga.ai.mobile.utils.PrefUtils;
import ga.ai.mobile.utils.StringUtils;
import ga.ai.mobile.utils.UiUtils;


public class ChannelsAdapter extends BaseExpandableListAdapter {


    private static final int POS_ID = 0;
    private static final int POS_URL = 1;
    private static final int POS_NAME = 2;
    private static final int POS_IS_GROUP = 3;
    public static final int POS_ICON = 4;
    private static final int POS_LAST_UPDATE = 5;
    private static final int POS_ERROR = 6;
    private static final int POS_UNREAD = 7;

    private static final String COLON = MainApplication.getContext().getString(R.string.colon);

    private static final int CACHE_MAX_ENTRIES = 100;
    private final Map<Long, String> mFormattedDateCache = new LinkedHashMap<Long, String>(CACHE_MAX_ENTRIES + 1, .75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
            return size() > CACHE_MAX_ENTRIES;
        }
    };

    private Context mContext;
    private int mSelectedItem;
    private Channel mCurrentChannel;
    private Cursor mFeedsCursor;
    private int mAllUnreadNumber, mFavoritesNumber;

    private LinkedHashMap<String, GroupInfo> subjects = new LinkedHashMap<String, GroupInfo>();
    private ArrayList<GroupInfo> groupList = new ArrayList<GroupInfo>();

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    private ArrayList<Channel> channels = new ArrayList<>();

    private int childCounter = 0;


    public ChannelsAdapter(Context context) {
        mContext = context;
        loadData();
        updateNumbers();
        notifyDataSetChanged();
    }

    //load initial data into out list
    private void loadData(){

//        addChildToGroup("Top", "", "All");
//        addChildToGroup("Top", "",  "Favorites");

        addChannel("Top", "",  "All", "", "", "ic_statusbar_rss", Channel.ALL_FEEDS, false, "entries_list_fragment");
        addChannel("Top", "",  "Favorites", "", "", "ic_star", Channel.STARRED, false, "entries_list_fragment");
        addChannel("Top", "",  "Листок", "", "http://altai-info.ga/mobile-app/feeds/feed-listock.php", "ic_listock", Channel.FEED, false, "entries_list_fragment");
        addChannel("Top", "",  "НГА", "", "http://altai-info.ga/mobile-app/feeds/feed-nga.php", "ic_nga", Channel.FEED, false, "entries_list_fragment");
        addChannel("Top", "",  "Новости Алтая", "", "http://altai-info.ga/mobile-app/feeds/feed-na.php", "ic_na", Channel.FEED, false, "entries_list_fragment");
        addChannel("Top", "",  "Холодный белóк", "", "http://altai-info.ga/mobile-app/feeds/feed-coldbelok.php", "ic_coldbelok", Channel.FEED, false, "entries_list_fragment");
        addChannel("Край, Россия, Мир",  "События за пределами Горного Алтая", "Банкфакс", "", "http://altai-info.ga/mobile-app/feeds/feed-bankfax.php", "ic_bankfax", Channel.FEED, false, "entries_list_fragment");
        addChannel("Край, Россия, Мир",  "События за пределами Горного Алтая", "Алтапресс", "", "http://altai-info.ga/mobile-app/feeds/feed-altapress.php", "ic_altapress", Channel.FEED, false, "entries_list_fragment");
        addChannel("Край, Россия, Мир",  "События за пределами Горного Алтая", "Россия и Мир", "", "http://altai-info.ga/mobile-app/feeds/feed-r_i_m.php", "ic_world", Channel.FEED, false, "entries_list_fragment");
        addChannel("Разное", "Полезная информация", "Попутчики РА", "", "http://altai-info.ga/mobile-app/feeds/feed-poputchiki.php", "ic_poputchiki", Channel.FEED, false, "poputchiki_fragment");
        addChannel("Разное", "Полезная информация", "Анонсы", "", "http://altai-info.ga/mobile-app/feeds/feed-anonsy.php", "ic_events", Channel.FEED, false, "entries_list_fragment");
        addChannel("Разное", "Полезная информация", "Желтые страницы", "Справочник по Республике Алтай и соседним регионам", "yellow-pages", "ic_yellow_pages", Channel.WEB_FRAGMENT, false, "yellow_pages_fragment");
        addChannel("Разное", "Полезная информация", "Юмор", "Ежедневная подборка анекдотов", "http://altai-info.ga/mobile-app/feeds/feed-humor.php", "ic_humor", Channel.FEED, false, "entries_list_fragment");
        addChannel("Разное", "Полезная информация", "Горно-Алтайск онлайн", "Вебкамера на площади им. Ленина", "webcam", "ic_webcam", Channel.WEB_FRAGMENT, false, "webcam_fragment");
        addChannel("Footer", "", "Погода", "Прогноз погоды по Республике Алтай", "weather", "ic_weather", Channel.WEB_FRAGMENT, false, "weather_fragment");
        addChannel("Footer", "", "Объявления", "Объявления газеты \"Листок\"", "classified", "ic_classified", Channel.WEB_FRAGMENT, false, "classified_fragment");

    }

    private void addChannel(String groupName, String groupDesc, String title, String channelDesc, String url, String icon, int type, boolean retrieveFullText, String fragmentLayout) {

        Channel ch = new Channel(title, channelDesc, groupName, url, icon, null, type, retrieveFullText, fragmentLayout);
        channels.add(ch);

        final int resId = mContext.getResources().getIdentifier(fragmentLayout, "id", mContext.getPackageName());
        ch.setFragment(((Activity)mContext).getFragmentManager().findFragmentById(resId));

        if (type == Channel.ALL_FEEDS) {
            ch.setUri(FeedData.EntryColumns.ALL_ENTRIES_CONTENT_URI);
            ch.setName(mContext.getResources().getString(R.string.all));
        } else if (type == Channel.STARRED) {
            ch.setUri(FeedData.EntryColumns.FAVORITES_CONTENT_URI);
            ch.setName(mContext.getResources().getString(R.string.favorites));
        } else {
            FeedDataContentProvider.addFeed(mContext, ch, url, title, icon, retrieveFullText);
            ch.getFragment().setMenuVisibility(false);
        }

        int iconID = mContext.getResources().getIdentifier(icon , "mipmap", mContext.getPackageName());
        BitmapDrawable bd = (BitmapDrawable) mContext.getResources().getDrawable(iconID);
        ch.setIcon(bd);

        ch.setGroupId(addChildToGroup(groupName, groupDesc, title));
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<ChildInfo> productList = groupList.get(groupPosition).getChildList();
        return productList.get(childPosition);
    }

    //here we maintain our childs in various groups
    private int addChildToGroup(String groupName, String groupDesc, String childName){

        int groupPosition = 0;

        //check the hash map if the group already exists
        GroupInfo group = subjects.get(groupName);
        //add the group if doesn't exists
        if(group == null){
            group = new GroupInfo();
            group.setName(groupName);
            group.setDesc(groupDesc);
            subjects.put(groupName, group);
            groupList.add(group);
        }

        //get the children for the group
        ArrayList<ChildInfo> childList = group.getChildList();
        //size of the children list
        int listSize = childList.size();
        //add to the counter
        listSize++;

        //create a new child and add that to the group
        ChildInfo child = new ChildInfo();
        child.setSequence(String.valueOf(listSize));
        child.setName(childName);
        child.setNum(childCounter++);
        childList.add(child);
        group.setChildList(childList);

        //find the group position inside the list
        groupPosition = groupList.indexOf(group);
        return groupPosition;
    }



    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {


        ChildInfo child = (ChildInfo) getChild(groupPosition, childPosition);
        int position = child.getNum();
        Channel channel = channels.get(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_drawer_list, parent, false);

            holder = new ViewHolder();
            holder.iconView = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.titleTxt = (TextView) convertView.findViewById(android.R.id.text1);
            holder.stateTxt = (TextView) convertView.findViewById(android.R.id.text2);
            holder.unreadTxt = (TextView) convertView.findViewById(R.id.unread_count);
            holder.separator = convertView.findViewById(R.id.separator);
            holder.separator.setBackgroundColor(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_dividers : R.color.dark_dividers));
            holder.separator.setVisibility(View.GONE);
            convertView.setTag(R.id.holder, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.id.holder);
        }


        if (holder != null) {
            if (position == mSelectedItem) {
                holder.titleTxt.setTextColor(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_primary_color : R.color.dark_primary_color));
            } else {
                holder.titleTxt.setTextColor(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_base_text : R.color.dark_base_text));
            }


            holder.titleTxt.setText("");
            holder.titleTxt.setAllCaps(false);
            holder.stateTxt.setVisibility(View.GONE);
            holder.unreadTxt.setText("");
            convertView.setPadding(0, 0, 0, 0);
            holder.separator.setVisibility(View.GONE);

            if (position == 0 || position == 1) {
                holder.titleTxt.setText(position == 0 ? R.string.all : R.string.favorites);
                holder.iconView.setImageResource(position == 0 ? R.drawable.rss : R.drawable.star);
                if (position == mSelectedItem) {
//                    holder.iconView.setColorFilter(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_primary_color : R.color.dark_primary_color));
                } else {
//                    holder.iconView.setColorFilter(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_base_text : R.color.dark_base_text));
                }

                int unread = position == 0 ? mAllUnreadNumber : mFavoritesNumber;
                if (unread != 0) {
                    holder.unreadTxt.setText(String.valueOf(unread));
                }
                return convertView;
            }

//            if (true) return convertView;


            holder.titleTxt.setText(channel.getName());
            holder.iconView.setImageDrawable(channel.getIcon());
            holder.stateTxt.setText(channel.getDesc());
            holder.stateTxt.setVisibility(View.VISIBLE);

            if (channel.getGroupId() == 1 || channel.getGroupId() == 2) {
                convertView.setPadding(30, 0, 0, 0);
            }

            if (channel.getType() == Channel.FEED && mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
                if (mFeedsCursor.isNull(POS_ERROR)) {
                    long timestamp = mFeedsCursor.getLong(POS_LAST_UPDATE);
                    // Date formatting is expensive, look at the cache
                    String formattedDate = mFormattedDateCache.get(timestamp);
                    if (formattedDate == null) {
                        formattedDate = mContext.getString(R.string.update) + COLON;
                        if (timestamp == 0) {
                            formattedDate += mContext.getString(R.string.never);
                        } else {
                            formattedDate += StringUtils.getDateTimeString(timestamp);
                        }
                        mFormattedDateCache.put(timestamp, formattedDate);
                    }
                    holder.stateTxt.setText(formattedDate);
                } else {
                    holder.stateTxt.setText(new StringBuilder(mContext.getString(R.string.error)).append(COLON).append(mFeedsCursor.getString(POS_ERROR)));
                }

                int unread = mFeedsCursor.getInt(POS_UNREAD);
                if (unread != 0) {
                    holder.unreadTxt.setText(String.valueOf(unread));
                }
            }


//            if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
//                holder.titleTxt.setText((mFeedsCursor.isNull(POS_NAME) ? mFeedsCursor.getString(POS_URL) : mFeedsCursor.getString(POS_NAME)));
//
//                if (mFeedsCursor.getInt(POS_IS_GROUP) == 1) {
//                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                    layoutParams.setMargins(0,80,0,0);
//
//                    holder.iconView.setLayoutParams(layoutParams);
////                    holder.titleTxt.setAllCaps(true);
////                    holder.separator.setVisibility(View.VISIBLE);
////                    //holder.iconView.setImageResource(R.drawable.ic_folder);
////                    if (position == mSelectedItem) {
////                        holder.iconView.setColorFilter(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_primary_color : R.color.dark_primary_color));
////                    } else {
////                        //holder.iconView.setColorFilter(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_base_text : R.color.dark_base_text));
////                    }
//                } else {
//
//                    holder.stateTxt.setVisibility(View.VISIBLE);
//
//                    if (mFeedsCursor.getPosition() == mFeedsCursor.getCount() - 1) {
////                        holder.stateTxt.setText(R.string.classifiedSubText);
//                    } else if (mFeedsCursor.getPosition() == mFeedsCursor.getCount() - 2) {
////                        holder.stateTxt.setText(R.string.weatherSubText);
//                    } else {
//                        if (mFeedsCursor.isNull(POS_ERROR)) {
//                            long timestamp = mFeedsCursor.getLong(POS_LAST_UPDATE);
//
//                            // Date formatting is expensive, look at the cache
//                            String formattedDate = mFormattedDateCache.get(timestamp);
//                            if (formattedDate == null) {
//                                formattedDate = mContext.getString(R.string.update) + COLON;
//                                if (timestamp == 0) {
//                                    formattedDate += mContext.getString(R.string.never);
//                                } else {
//                                    formattedDate += StringUtils.getDateTimeString(timestamp);
//                                }
//                                mFormattedDateCache.put(timestamp, formattedDate);
//                            }
//
//                            holder.stateTxt.setText(formattedDate);
//                        } else {
//                            holder.stateTxt.setText(new StringBuilder(mContext.getString(R.string.error)).append(COLON).append(mFeedsCursor.getString(POS_ERROR)));
//                        }
//                    }
//
//
//                    final long feedId = mFeedsCursor.getLong(POS_ID);
//                    Bitmap bitmap = UiUtils.getFaviconBitmap(feedId, mFeedsCursor, POS_ICON);
//
//                    if (bitmap != null) {
//                        holder.iconView.setImageBitmap(bitmap);
//                    } else {
//                        holder.iconView.setImageResource(R.mipmap.ic_launcher);
//                    }
//
//                    int unread = mFeedsCursor.getInt(POS_UNREAD);
//                    if (unread != 0) {
//                        holder.unreadTxt.setText(String.valueOf(unread));
//                    }
//                }
//                if ((mFeedsCursor.isNull(POS_NAME) ? mFeedsCursor.getString(POS_URL) : mFeedsCursor.getString(POS_NAME)).startsWith("ERROR:")) {
//                    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                    convertView = inflater.inflate(R.layout.item_drawer_null, parent, false);
//                    return convertView;
//                }
//            }
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<ChildInfo> productList = groupList.get(groupPosition).getChildList();
        return productList.size();
    }

    public int getChildCounter() {
        return childCounter;
    }

    public ArrayList<GroupInfo> getGroupList() {
        return groupList;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (groupPosition == 0 || groupPosition == 3) {
            TextView txtV = new TextView(mContext.getApplicationContext());
//            txtV.setText("группа");
            txtV.setHeight(0);
//            txtV.setCompoundDrawablesWithIntrinsicBounds(isExpanded ? 0 : android.R.drawable.arrow_down_float, 0, 0, 0);
            return txtV;
        }

        GroupInfo group = (GroupInfo) getGroup(groupPosition);
        ViewHolder holder;


//        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_drawer_list, parent, false);
//        }

        holder = (ViewHolder)convertView.getTag(R.id.holder);

        if (holder == null) {
            holder = new ViewHolder();
            holder.iconView = (ImageView) convertView.findViewById(android.R.id.icon);
            holder.titleTxt = (TextView) convertView.findViewById(android.R.id.text1);
            holder.stateTxt = (TextView) convertView.findViewById(android.R.id.text2);
            holder.unreadTxt = (TextView) convertView.findViewById(R.id.unread_count);
//            holder.separator = convertView.findViewById(R.id.separator);
//            holder.separator.setBackgroundColor(ContextCompat.getColor(mContext, PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_dividers : R.color.dark_dividers));
            convertView.setTag(R.id.holder, holder);
        }


//        holder.iconView.setImageDrawable(null);
//        holder.iconView.setMaxWidth(0);
//        holder.titleTxt.setAllCaps(false);
        holder.titleTxt.setText(group.getName());
        holder.stateTxt.setText(group.getDesc());
        holder.unreadTxt.setText("");
        convertView.setPadding(0, 0, 0, 0);
//        holder.separator.setVisibility(View.GONE);
        convertView.setBackgroundColor(mContext.getResources().getColor(R.color.light_dividers));

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public int getCount() {
        if (mFeedsCursor != null) {
            return mFeedsCursor.getCount() + 2;
        }
        return 0;
    }


    public long getItemId(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
            return mFeedsCursor.getLong(POS_ID);
        }

        return -1;
    }

    public byte[] getItemIcon(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
            return mFeedsCursor.getBlob(POS_ICON);
        }

        return null;
    }

    public String getItemName(int position) {
        if (mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2)) {
            return mFeedsCursor.isNull(POS_NAME) ? mFeedsCursor.getString(POS_URL) : mFeedsCursor.getString(POS_NAME);
        }

        return null;
    }

    public boolean isItemAGroup(int position) {
        return mFeedsCursor != null && mFeedsCursor.moveToPosition(position - 2) && mFeedsCursor.getInt(POS_IS_GROUP) == 1;

    }

    private void updateNumbers() {
        mAllUnreadNumber = mFavoritesNumber = 0;

        // Gets the numbers of entries (should be in a thread, but it's way easier like this and it shouldn't be so slow)
        Cursor numbers = mContext.getContentResolver().query(FeedData.EntryColumns.CONTENT_URI, new String[]{FeedData.ALL_UNREAD_NUMBER, FeedData.FAVORITES_NUMBER}, null, null, null);
        if (numbers != null) {
            if (numbers.moveToFirst()) {
                mAllUnreadNumber = numbers.getInt(0);
                mFavoritesNumber = numbers.getInt(1);
            }
            numbers.close();
        }
    }

    public void setCursor(Cursor cursor) {
        mFeedsCursor = cursor;
        updateNumbers();
    }

    public Channel getCurrentChannel() {
        return mCurrentChannel;
    }

    public void setCurrentChannel(int pos) {
        mCurrentChannel = channels.get(pos);

        for (Channel ch : channels) {
            ch.getFragment().getView().setVisibility(View.GONE);
            ch.getFragment().setMenuVisibility(false);
        }
        mCurrentChannel.getFragment().getView().setVisibility(View.VISIBLE);
        mCurrentChannel.getFragment().setMenuVisibility(true);
    }


    static class ViewHolder {
        ImageView iconView;
        TextView titleTxt;
        TextView stateTxt;
        TextView unreadTxt;
        View separator;
    }
}