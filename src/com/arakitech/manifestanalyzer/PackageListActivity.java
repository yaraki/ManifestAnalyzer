
package com.arakitech.manifestanalyzer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.arakitech.manifestanalyzer.R;

public class PackageListActivity extends Activity implements OnItemClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.package_list);
        ListView packageList = (ListView) findViewById(R.id.package_list);
        packageList.setAdapter(new PackageListAdapter(this));
        packageList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final PackageInfo info = (PackageInfo) parent.getItemAtPosition(position);
        final Intent intent = new Intent(this, PackageDetailActivity.class);
        intent.putExtra(PackageDetailActivity.EXTRA_PACKAGE_NAME, info.packageName);
        startActivity(intent);
    }
}
