package name.caiyao.fakegps.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import name.caiyao.fakegps.BuildConfig;
import name.caiyao.fakegps.R;
import name.caiyao.fakegps.data.AppInfo;

public class MainActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;
    private AppAdapter mAppAdapter;
    private ArrayList<AppInfo> mAppInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_app);
        assert recyclerView != null;
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        mAppAdapter = new AppAdapter(mAppInfos);
        recyclerView.setAdapter(mAppAdapter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMax(100);
        mProgressDialog.setMessage("正在扫描应用程序");
        mProgressDialog.show();

        GetAppInfoTask getAppInfoTask = new GetAppInfoTask();
        getAppInfoTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setting:

                break;
            case R.id.donate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://qr.alipay.com/apoy1zw1o2xpc7915d")));
                break;
            case R.id.about:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://caiyao.name/releases")));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GetAppInfoTask extends AsyncTask<Integer, Integer, ArrayList<AppInfo>> {

        @Override
        protected ArrayList<AppInfo> doInBackground(Integer[] params) {
            ArrayList<AppInfo> appList = new ArrayList<>();
            List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                AppInfo tmpInfo = new AppInfo();
                tmpInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                tmpInfo.packageName = packageInfo.packageName;
                tmpInfo.versionName = packageInfo.versionName;
                tmpInfo.versionCode = packageInfo.versionCode;
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());
                if (!packageInfo.packageName.equals(BuildConfig.APPLICATION_ID))
                    appList.add(tmpInfo);
                publishProgress(i / packages.size() * 100);
            }
            return appList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo> o) {
            mProgressDialog.dismiss();
            mAppInfos.addAll(o);
            mAppAdapter.notifyDataSetChanged();
        }
    }

    class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {

        public ArrayList<AppInfo> mAppInfos;

        public AppAdapter(ArrayList<AppInfo> appInfos) {
            this.mAppInfos = appInfos;
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AppViewHolder(getLayoutInflater().inflate(R.layout.app_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final AppViewHolder holder, int position) {
            holder.ivIcon.setImageDrawable(mAppInfos.get(position).getAppIcon());
            holder.tvName.setText(mAppInfos.get(position).getAppName());
            holder.tvPackageName.setText(mAppInfos.get(position).getPackageName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, AMapActivity.class).putExtra("package_name", mAppInfos.get(holder.getAdapterPosition()).getPackageName()));
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAppInfos.size();
        }

        public class AppViewHolder extends RecyclerView.ViewHolder {

            public ImageView ivIcon;
            public TextView tvName;
            public TextView tvPackageName;

            public AppViewHolder(View itemView) {
                super(itemView);
                ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvPackageName = (TextView) itemView.findViewById(R.id.tv_package_name);
            }
        }
    }
}
