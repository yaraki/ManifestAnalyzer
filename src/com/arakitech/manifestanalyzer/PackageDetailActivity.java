
package com.arakitech.manifestanalyzer;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;

public class PackageDetailActivity extends Activity {

    public static final String EXTRA_PACKAGE_NAME = "package_name";

    private String mPackageName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();
        mPackageName = extras.getString(EXTRA_PACKAGE_NAME);
        final PackageManager manager = getPackageManager();
        setContentView(R.layout.package_detail);
        final ImageView applicationIcon = (ImageView) findViewById(R.id.application_icon);
        final TextView applicationLabel = (TextView) findViewById(R.id.application_label);
        final TextView packageName = (TextView) findViewById(R.id.package_name);
        final ListView manifestList = (ListView) findViewById(R.id.manifest_list);
        packageName.setText(mPackageName); // パッケージ名
        // アプリケーション名、UID
        try {
            final ApplicationInfo info = manager.getApplicationInfo(mPackageName, 0); // アイコン
            applicationIcon.setImageDrawable(manager.getApplicationIcon(mPackageName));
            applicationLabel.setText(manager.getApplicationLabel(info)); // アプリケーション名
            manifestList.setAdapter(new ManifestAdapter(this, info));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
