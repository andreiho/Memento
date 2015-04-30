package android.memento.db;


import android.provider.BaseColumns;

import java.util.Date;


/*
 *   Class defining the final variables we will use
 *   to access the data in the database
*/
public class MemosContract {

    public static final String DB_NAME = "android.memento.db.memos";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "memos";

    public class Columns {
        public static final String MEMO = "memo";
        public static final String _ID = BaseColumns._ID;
        public static final String STATUS = "status";
    }

}
