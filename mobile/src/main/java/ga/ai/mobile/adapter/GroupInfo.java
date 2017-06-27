package ga.ai.mobile.adapter;

import java.util.ArrayList;

public class GroupInfo {

    private String name;
    private ArrayList<ChildInfo> list = new ArrayList<ChildInfo>();
    private String desc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ChildInfo> getChildList() {
        return list;
    }

    public void setChildList(ArrayList<ChildInfo> childList) {
        this.list = childList;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}