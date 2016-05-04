package name.caiyao.fakegps.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 蔡小木 on 2016/5/4 0004.
 */
public class DbHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "applist.db";
    public final static String APP_TABLE_NAME = "app";
    private final static int DB_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_APP_TABLE = "CREATE TABLE IF NOT EXISTS" + APP_TABLE_NAME + "package_name TEXT PRIMARY KEY," + "latitude DOUBLE,longitude DOUBLE";
        db.execSQL(CREATE_APP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
