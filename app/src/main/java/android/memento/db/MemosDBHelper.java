package android.memento.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/*
 *   Helper class to open the database
*/
public class MemosDBHelper extends SQLiteOpenHelper {

    public MemosDBHelper(Context context) {
        super(context, MemosContract.DB_NAME, null, MemosContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDB) {
        String sqlQuery =
                String.format(
                        "CREATE TABLE %s (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + "%s TEXT, " + "%s TEXT)",
                        MemosContract.TABLE,
                        MemosContract.Columns.MEMO,
                        MemosContract.Columns.STATUS
                );

        Log.d("MemosDBHelper", "Query to form table: " + sqlQuery);
        sqlDB.execSQL(sqlQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDB, int i, int i2) {
        sqlDB.execSQL("DROP TABLE IF EXISTS " + MemosContract.TABLE);
        onCreate(sqlDB);
    }

}
