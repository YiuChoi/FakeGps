package name.caiyao.fakegps;

import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private AppAdapter mAppAdapter;
    private ArrayList<AppInfo> mAppInfos = new ArrayList<>();
    private ArrayList<AppInfo> mAllAppInfos = new ArrayList<>();
    public ArrayList<String> selectApps = new ArrayList<>();

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

        mProgressBar = new ProgressBar(this);
        mProgressBar.setMax(100);
        mProgressBar.setVisibility(View.INVISIBLE);

        EditText etSearch = (EditText) findViewById(R.id.et_search);
        assert etSearch != null;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String key = s.toString();
                if (TextUtils.isEmpty(key)) {
                    mAppInfos.clear();
                    mAppInfos.addAll(mAllAppInfos);
                    mAppAdapter.notifyDataSetChanged();
                    return;
                }
                mAppInfos.clear();
                mAppAdapter.notifyDataSetChanged();
                for (int i = 0; i < mAllAppInfos.size(); i++) {
                    AppInfo appInfo = mAllAppInfos.get(i);
                    if (appInfo.getAppName().contains(key)||appInfo.getPackageName().contains(key)) {
                        mAppInfos.add(appInfo);
                        mAppAdapter.notifyItemInserted(mAllAppInfos.size()-1);
                    }
                }
            }
        });

        CheckBox cbAll = (CheckBox) findViewById(R.id.cb_all);
        assert cbAll != null;
        cbAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (AppInfo appInfo : mAppInfos) {
                    appInfo.setChecked(isChecked);
                    selectApps.add(appInfo.getPackageName());
                }
                mAppAdapter.notifyDataSetChanged();
            }
        });

        GetAppInfoTask getAppInfoTask = new GetAppInfoTask();
        getAppInfoTask.execute();
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
            if (!mProgressBar.isShown()) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<AppInfo> o) {
            mAllAppInfos = o;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start:
                Log.i("FakeGps", Arrays.toString(selectApps.toArray()));
                break;
        }
        return super.onOptionsItemSelected(item);
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
            holder.cbApp.setChecked(mAppInfos.get(position).isChecked());
            holder.cbApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String packageName = mAppInfos.get(holder.getAdapterPosition()).getPackageName();
                    mAppInfos.get(holder.getAdapterPosition()).setChecked(isChecked);
                    if (isChecked && !selectApps.contains(packageName)) {
                        selectApps.add(packageName);
                    } else if (!isChecked && selectApps.contains(packageName)) {
                        selectApps.remove(packageName);
                    }
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
            public CheckBox cbApp;

            public AppViewHolder(View itemView) {
                super(itemView);
                ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvPackageName = (TextView) itemView.findViewById(R.id.tv_package_name);
                cbApp = (CheckBox) itemView.findViewById(R.id.cb_app);
            }
        }
    }
}
