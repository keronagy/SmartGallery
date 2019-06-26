// ------------------------------------ DBADapter.java ---------------------------------------------

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Search for "TO_DO", and make the appropriate changes.
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++

// [TO_DO_A1]
// Change the package to match your project package name
package com.example.SmartGallery.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;




public class DBAdapter {

	private static final String TAG = "DBAdapter";



	public static final String KEY_PATH = "path";
	public static final String KEY_CAPTION = "caption";
	public static final String KEY_TAGS = "tags";
	public static final String KEY_DATE = "date";
	public static final String KEY_ALBUM = "album";

	public static final int COL_PATH = 0;
	public static final int COL_CAPTION = 1;
	public static final int COL_TAGS = 2;
	public static final int COL_DATE = 3;
	public static final int COL_ALBUM = 4;


	public static final String[] ALL_KEYS = new String[] {KEY_PATH, KEY_CAPTION, KEY_TAGS,KEY_DATE,KEY_ALBUM};
	

	public static final String DATABASE_NAME = "PhotosDB";
	public static final String DATABASE_TABLE = "PhotosTable";

	public static final int DATABASE_VERSION = 1;	
	
	
	// [TO_DO_A7]
	// DATABASE_CREATE SQL command 
	private static final String DATABASE_CREATE_SQL = 
			"create table " + DATABASE_TABLE 
			+ " (" + KEY_PATH + " string primary key not null, "
			+ KEY_CAPTION + " string, "
			+ KEY_TAGS + " string,"
			+ KEY_DATE + " string,"
			+ KEY_ALBUM + " string"
			+ ");";
	
	private final Context context;
	
	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	public DBAdapter(Context ctx) {
		this.context = ctx;
		myDBHelper = new DatabaseHelper(context);
	}
	
	// Open the database connection.
	public DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}
	
	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}
	
	// Add a new set of values to the database.
	public long insertRow(String path, String caption, String tags,String date,String album) {
		// [TO_DO_A8]
		// Update data in the row with new fields.
		// Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_PATH, path);
		initialValues.put(KEY_CAPTION, caption);
		initialValues.put(KEY_TAGS, tags);
		initialValues.put(KEY_DATE, date);
		initialValues.put(KEY_ALBUM, album);

		// Insert it into the database.
		return db.insertWithOnConflict(DATABASE_TABLE, null, initialValues,SQLiteDatabase.CONFLICT_IGNORE);
	}
	
	// Delete a row from the database, by rowId (primary key)
	public boolean deleteRow(String path) {
		String where = KEY_PATH + "=" + path;
		return db.delete(DATABASE_TABLE, where, null) != 0;
	}
	
//	// Delete all records
//	public void deleteAll() {
//		Cursor c = getAllRows();
//		long path = c.getColumnIndexOrThrow(KEY_PATH);
//		if (c.moveToFirst()) {
//			do {
//				deleteRow(c.getString(path));
//			} while (c.moveToNext());
//		}
//		c.close();
//	}
	
	// Return all rows in the database.
	public Cursor getAllRows() {
		String where = null;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, 
							where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Return all rows in the database.
	public Cursor getAllRowsNullCaption() {
		String where = KEY_CAPTION + " is null";
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Return all rows in the database.
	public Cursor getAllRowsNullTag() {
		String where = KEY_TAGS + " is null";
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

    // Return all rows in the database.
    public Cursor getAllRowsNullCaptionAndTags() {
        String where = KEY_CAPTION + " IS null AND " + KEY_TAGS + " IS null";
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getAllRowsSorted() {
        String where = null;
        Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
                where, null, null, null, KEY_DATE+" DESC", null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

	// Get a specific row (by path)
	public Cursor getRow(String path) {
		String where = KEY_PATH + "=\"" + path+"\"";
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS, 
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	// Get a specific row (by Tag)
	public Cursor getRowByTag(String tag) {
//		String where = KEY_TAGS + "=" + tag;
        String where = SearchWhereClause(tag,KEY_TAGS,"AND");
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	// Get a specific row (by Caption)
	public Cursor getRowByCaption(String caption) {
		String where = SearchWhereClause(caption,KEY_CAPTION,"AND");
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	// Get a specific row (By Tag And Album)
	public Cursor getRowByTagAndAlbum(String tag, String album) {
		String where =KEY_ALBUM +" = \""+ album + "\" AND "+ SearchWhereClause(tag,KEY_TAGS,"AND");
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	// Get a specific row (by Caption And Album)
	public Cursor getRowByCaptionAndAlbum(String caption, String album) {
		String where = KEY_ALBUM +" = \""+ album + "\" AND "+SearchWhereClause(caption,KEY_CAPTION,"AND");
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	public Cursor getRowByAlbum(String albums) {
		String where = KEY_ALBUM + " = " + SearchByAlbumsWhereClause(albums) + " AND "+KEY_CAPTION + " IS null AND " + KEY_TAGS + " IS null"; ;
		Cursor c = 	db.query(true, DATABASE_TABLE, ALL_KEYS,
				where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	// Change an existing row to be equal to new data.
//	public boolean updateRow(String path, String caption, String tags,String date,String album) {
//		String where = KEY_PATH + "=" + path;
//
//		ContentValues newValues = new ContentValues();
//		newValues.put(KEY_CAPTION, caption);
//		newValues.put(KEY_TAGS, tags);
//		newValues.put(KEY_DATE, date);
//		newValues.put(KEY_ALBUM, album);
//
//		// Insert it into the database.
//		return db.update(DATABASE_TABLE, newValues, where, null) != 0;
//	}

    //Row Updates
    //1- update Caption Only
    public boolean updateRowCaption(String path, String caption) {
        String where = KEY_PATH + "=" + path;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_CAPTION, caption);
        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }

    //2- update Tags only
    public boolean updateRowTags(String path, String tags) {
        String where = KEY_PATH + "=" + path;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_TAGS, tags);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }

    //3- update Tags and Caption
    public boolean updateRow(String path, String caption, String tags) {
        String where = KEY_PATH + "=" + path;

        ContentValues newValues = new ContentValues();
        newValues.put(KEY_CAPTION, caption);
        newValues.put(KEY_TAGS, tags);

        // Insert it into the database.
        return db.update(DATABASE_TABLE, newValues, where, null) != 0;
    }






    public static String SearchWhereClause(String search, String field, String and)
    {
        String[] splited = search.split("\\s+");
        String like = "";
        for (int i = 0; i < splited.length; i++) {
            if(i==0 && splited.length ==1)
            {
                like += field + " LIKE \"%" + splited[i] + "%\"";
            }
            else if(i == splited.length-1)
            {
                like += field + " LIKE \"%" + splited[i] + "%\"";
            }
            else
            {
                like += field + " LIKE \"%" + splited[i] + "%\" "+and+" ";
            }

        }
        return like;
    }
	public String SearchByAlbumsWhereClause(String albums)
	{

        String[] splited = albums.split("\\s+");
        String s = "\""+splited[0]+"\"";
        for (int i = 1; i < splited.length; i++) {
            s += " OR "+ KEY_ALBUM +" = \"" +splited[i]+"\"";
        }
        return s;
	}
	public Cursor getAllAlbumsNames()
    {
        Cursor c = 	db.query(true, DATABASE_TABLE, new String[]{KEY_ALBUM+" as _id "},
                null, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
//        String[] projection = new String[] { MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.NUMBER_OF_SONGS };
//        String selection = null;
//        String[] selectionArgs = null;
//        String sortOrder = MediaStore.Audio.Media.ALBUM + " ASC";
//        Cursor cursor = getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    public boolean isOpen()
    {
        return db.isOpen();
    }

	
	
    // ==================
	//	Private Helper Classes:
	// ==================
	
	/**
	 * Private class which handles database creation and upgrading.
	 * Used to handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DATABASE_CREATE_SQL);			
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");
			
			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			
			// Recreate new database:
			onCreate(_db);
		}
	}
}
