
package com.arakitech.manifestanalyzer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.arakitech.manifestanalyzer.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class ManifestAdapter extends BaseAdapter implements BinaryXmlParser.EventHandler {

    private ApplicationInfo mApplicationInfo;
    private LayoutInflater mLayoutInflater;
    private List<Item> mItems = new ArrayList<Item>();

    private static String sStringActivities = null;
    private static String sStringIntentFilters = null;
    private static String sStringUsesPermission = null;
    private static String sStringApplication = null;
    private static String sStringUsesSdk = null;
    private static String sStringReceiver = null;
    private static String sStringService = null;
    private static String sStringProvider = null;
    private static String sStringMetaData = null;

    public ManifestAdapter(Context context, ApplicationInfo info) throws IOException {
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mApplicationInfo = info;
        if (null == sStringActivities) {
            sStringActivities = context.getString(R.string.activity);
        }
        if (null == sStringIntentFilters) {
            sStringIntentFilters = context.getString(R.string.intent_filter);
        }
        if (null == sStringUsesPermission) {
            sStringUsesPermission = context.getString(R.string.uses_permission);
        }
        if (null == sStringApplication) {
            sStringApplication = context.getString(R.string.application);
        }
        if (null == sStringUsesSdk) {
            sStringUsesSdk = context.getString(R.string.uses_sdk);
        }
        if (null == sStringReceiver) {
            sStringReceiver = context.getString(R.string.receiver);
        }
        if (null == sStringService) {
            sStringService = context.getString(R.string.service);
        }
        if (null == sStringProvider) {
            sStringProvider = context.getString(R.string.provider);
        }
        if (null == sStringMetaData) {
            sStringMetaData = context.getString(R.string.meta_data);
        }
        ZipFile zipFile = new ZipFile(info.publicSourceDir);
        InputStream in = zipFile.getInputStream(zipFile.getEntry("AndroidManifest.xml"));
        new BinaryXmlParser(in).parse(this);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (null == v) {
            v = mLayoutInflater.inflate(R.layout.manifest_row, null);
            ViewHolder holder = new ViewHolder();
            holder.title = (TextView) v.findViewById(R.id.item_title);
            holder.name = (TextView) v.findViewById(R.id.item_name);
            holder.description = (TextView) v.findViewById(R.id.item_description);
            v.setTag(holder);
        }
        Item item = (Item) getItem(position);
        item.bindView(v);
        return v;
    }

    private int mCurrentIndent = -2;
    private IntentFilterItem mCurrentIntentFilter = null;

    @Override
    public void onStartTag(String tag, Map<String, String> attrs) {
        Log.v(this.getClass().getSimpleName(), "<" + tag + " " + constructAttr(attrs) + ">");
        ++mCurrentIndent;
        if (tag.equals("activity")) {
            ActivityItem item = new ActivityItem(mCurrentIndent);
            item.name = attrs.get("name");
            mItems.add(item);
        } else if (tag.equals("intent-filter")) {
            mCurrentIntentFilter = new IntentFilterItem(mCurrentIndent);
        } else if (tag.equals("action")) {
            if (null != mCurrentIntentFilter) {
                mCurrentIntentFilter.action = attrs.get("name");
            }
        } else if (tag.equals("category")) {
            if (null != mCurrentIntentFilter) {
                mCurrentIntentFilter.category = attrs.get("name");
            }
        } else if (tag.equals("data")) {
            if (null != mCurrentIntentFilter) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> attr : attrs.entrySet()) {
                    sb.append(attr.getKey());
                    sb.append(" = ");
                    sb.append(attr.getValue());
                }
                mCurrentIntentFilter.data.add(sb.toString());
            }
        } else if (tag.equals("uses-permission")) {
            UsesPermissionItem item = new UsesPermissionItem(mCurrentIndent);
            item.name = attrs.get("name");
            mItems.add(item);
        } else if (tag.equals("application")) {
            ApplicationItem item = new ApplicationItem(mCurrentIndent);
            mItems.add(item);
        } else if (tag.equals("receiver")) {
            ReceiverItem item = new ReceiverItem(mCurrentIndent);
            item.name = attrs.get("name");
            mItems.add(item);
        } else if (tag.equals("uses-sdk")) {
            UsesSdkItem item = new UsesSdkItem(mCurrentIndent);
            item.minSdkVersion = getInteger(attrs.get("minSdkVersion"));
            item.targetSdkVersion = getInteger(attrs.get("targetSdkVersion"));
            mItems.add(item);
        } else if (tag.equals("service")) {
            ServiceItem item = new ServiceItem(mCurrentIndent);
            item.name = attrs.get("name");
            mItems.add(item);
        } else if (tag.equals("provider")) {
            ProviderItem item = new ProviderItem(mCurrentIndent);
            item.name = attrs.get("name");
            item.authorities = attrs.get("authorities");
            mItems.add(item);
        } else if (tag.equals("meta-data")) {
            MetaDataItem item = new MetaDataItem(mCurrentIndent);
            item.name = attrs.get("name");
            if (attrs.containsKey("value")) {
                item.value = attrs.get("value");
            } else {
                item.value = attrs.get("resource");
            }
            mItems.add(item);
        }
    }

    private static int getInteger(String resourceId) {
        if (null == resourceId) {
            return 0;
        }
        try {
            return Integer.parseInt(resourceId.substring(resourceId.lastIndexOf("x") + 1), 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onEndTag(String tag) {
        Log.v(this.getClass().getSimpleName(), "</" + tag + ">");
        --mCurrentIndent;
        if (tag.equals("intent-filter")) {
            mItems.add(mCurrentIntentFilter);
            mCurrentIntentFilter = null;
        }
    }

    private static String constructAttr(Map<String, String> attrs) {
        if (attrs == null || attrs.size() == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> attr : attrs.entrySet()) {
            sb.append(attr.getKey() + "=\"" + attr.getValue() + "\" ");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private static class ViewHolder {
        public TextView title;
        public TextView name;
        public TextView description;
    }

    public static abstract class Item {
        private int mIndent;

        public Item(int indent) {
            mIndent = indent;
        }

        public abstract void bindView(View view);

        public void bindView(View view, String title, String name, String description) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.title.setText(title);
            if (null == name) {
                holder.name.setVisibility(View.GONE);
            } else {
                holder.name.setText(name);
                holder.name.setVisibility(View.VISIBLE);
            }
            if (null == description) {
                holder.description.setVisibility(View.GONE);
            } else {
                holder.description.setText(description);
                holder.description.setVisibility(View.VISIBLE);
            }
            view.setPadding(mIndent * 20, 0, 0, 0);
        }
    }

    public class ActivityItem extends Item {

        public String name;

        public ActivityItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            String description = name;
            if (!name.startsWith(mApplicationInfo.packageName)) {
                if (name.charAt(0) == '.') {
                    description = mApplicationInfo.packageName + description;
                } else {
                    description = mApplicationInfo.packageName + "." + description;
                }
            }
            bindView(view, sStringActivities, name, description);
        }
    }

    public class IntentFilterItem extends Item {

        public String action;
        public String category;
        public List<String> data = new ArrayList<String>();

        public IntentFilterItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            String description = category;
            if (0 < data.size()) {
                StringBuilder sb = new StringBuilder();
                for (String d : data) {
                    sb.append(d);
                    sb.append(", ");
                }
                description += " (" + sb.substring(0, sb.length() - 2) + ")";
            }
            bindView(view, sStringIntentFilters, action, description);
        }
    }

    public class UsesPermissionItem extends Item {

        public String name;

        public UsesPermissionItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            bindView(view, sStringUsesPermission, name, null);
        }
    }

    public class ApplicationItem extends Item {

        public ApplicationItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            bindView(view, sStringApplication, null, null);
        }
    }

    public class ReceiverItem extends Item {

        public String name;

        public ReceiverItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            String description = name;
            if (!name.startsWith(mApplicationInfo.packageName)) {
                if (name.charAt(0) == '.') {
                    description = mApplicationInfo.packageName + description;
                } else {
                    description = mApplicationInfo.packageName + "." + description;
                }
            }
            bindView(view, sStringReceiver, name, description);
        }
    }

    public class UsesSdkItem extends Item {

        public int minSdkVersion;
        public int targetSdkVersion;

        public UsesSdkItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            bindView(view, sStringUsesSdk, "min: " + minSdkVersion, "target: " + targetSdkVersion);
        }
    }

    public class ServiceItem extends Item {

        public String name;

        public ServiceItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            String description = name;
            if (!name.startsWith(mApplicationInfo.packageName)) {
                if (name.charAt(0) == '.') {
                    description = mApplicationInfo.packageName + description;
                } else {
                    description = mApplicationInfo.packageName + "." + description;
                }
            }
            bindView(view, sStringService, name, description);
        }
    }

    public class ProviderItem extends Item {

        public String name;
        public String authorities;

        public ProviderItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            bindView(view, sStringProvider, name, authorities);
        }
    }

    public class MetaDataItem extends Item {

        public String name;
        public String value;

        public MetaDataItem(int indent) {
            super(indent);
        }

        @Override
        public void bindView(View view) {
            bindView(view, sStringMetaData, name, value);
        }
    }

}
