package name.caiyao.fakegps;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 蔡小木 on 2016/5/11 0011.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "fake.db";
    public static final String TABLE_NAME = "fake";
    public DbHelper(Context context) {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME + "(key TEXT PRIMARY KEY,value TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
