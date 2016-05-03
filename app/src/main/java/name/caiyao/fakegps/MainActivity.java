package name.caiyao.fakegps;

import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private AppAdapter mAppAdapter;
    private ArrayList<AppInfo> mAppInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_app);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAppAdapter = new AppAdapter(mAppInfos);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressBar = new ProgressBar(this);
        mProgressBar.setMax(100);
        mProgressBar.setVisibility(View.INVISIBLE);

        GetAppInfoTask getAppInfoTask = new GetAppInfoTask();
        getAppInfoTask.execute();
    }

    private class GetAppInfoTask extends AsyncTask<Integer, Integer, ArrayList<AppInfo>> {

        @Override
        protected ArrayList<AppInfo> doInBackground(Integer[] params) {
            ArrayList<AppInfo> appList = new ArrayList<>(); //用来存储获取的应用信息数据
            List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                AppInfo tmpInfo = new AppInfo();
                tmpInfo.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
                tmpInfo.packageName = packageInfo.packageName;
                tmpInfo.versionName = packageInfo.versionName;
                tmpInfo.versionCode = packageInfo.versionCode;
                tmpInfo.appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());
                appList.add(tmpInfo);
                publishProgress(i / packages.size() * 100);
            }
            return appList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (!mProgressBar.isShown()) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo> o) {
            mProgressBar.setVisibility(View.INVISIBLE);
            mAppInfos.addAll(o);
            mAppAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
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
        public void onBindViewHolder(AppViewHolder holder, int position) {
            holder.ivIcon.setImageDrawable(mAppInfos.get(position).getAppIcon());
            holder.tvName.setText(mAppInfos.get(position).getAppName());
            holder.tvPackageName.setText(mAppInfos.get(position).getPackageName());
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
