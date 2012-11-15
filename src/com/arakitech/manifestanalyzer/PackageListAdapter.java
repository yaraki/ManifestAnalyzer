
package com.arakitech.manifestanalyzer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.arakitech.manifestanalyzer.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PackageListAdapter extends BaseAdapter {

    private final PackageManager mPackageManager;
    private final List<PackageInfo> mPackages;
    private final LayoutInflater mLayoutInflater;

    public PackageListAdapter(Context context) {
        mPackageManager = context.getPackageManager();
        mPackages = mPackageManager.getInstalledPackages(0);
        Collections.sort(mPackages, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo info1, PackageInfo info2) {
                return info1.packageName.compareTo(info2.packageName);
            }
        });
        mLayoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mPackages.size();
    }

    @Override
    public Object getItem(int position) {
        return mPackages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;
        if (null == v) {
            v = mLayoutInflater.inflate(R.layout.package_row, null);
            holder = new ViewHolder();
            holder.packageName = (TextView) v.findViewById(R.id.package_name);
            holder.applicationIcon = (ImageView) v.findViewById(R.id.application_icon);
            holder.applicationLabel = (TextView) v.findViewById(R.id.application_label);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        final PackageInfo info = (PackageInfo) getItem(position);
        holder.packageName.setText(info.packageName);
        try {
            holder.applicationIcon.setImageDrawable(mPackageManager
                    .getApplicationIcon(info.packageName));
        } catch (NameNotFoundException e) {
            holder.applicationIcon.setImageResource(R.drawable.icon);
        }
        final ApplicationInfo applicationInfo = info.applicationInfo;
        if (null != applicationInfo) {
            holder.applicationLabel.setText(mPackageManager.getApplicationLabel(applicationInfo));
        }
        return v;
    }

    private class ViewHolder {
        public TextView packageName;
        public ImageView applicationIcon;
        public TextView applicationLabel;
    }
}
