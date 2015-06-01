// ------------------------------------ DBADapter.java ---------------------------------------------

// TODO: Change the package to match your project.
package voiplicenta.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


// TO USE:
// Change the package (at top) to match your project.
// Search for "TODO", and make the appropriate changes.
public class DBAdapter {

	/////////////////////////////////////////////////////////////////////
	//	Constants & Data
	/////////////////////////////////////////////////////////////////////
	// For logging:
	private static final String TAG = "DBAdapter";
	
	// DB Fields
	public static final String KEY_CONTACT_ROWID = "_id";
	public static final String KEY_CALL_ROWID = "_id";
	public static final int COL_ROWID = 0;
	public static final int COL_CALL_ROWID = 0;
	/*
	 * CHANGE 1:
	 */
	// TODO: Setup your fields here:
	public static final String KEY_CONTACT_NAME = "contactname";
	public static final String KEY_CONTACT_DOMAIN = "contactdomain";
	
	public static final String KEY_CALL_NAME = "callinfoname";
	public static final String KEY_CALL_DOMAIN = "callinfodomain";
	public static final String KEY_CALL_TYPE = "callinfotype";
	
	// TODO: Setup your field numbers here (0 = KEY_ROWID, 1=...)
	public static final int COL_NAME = 1;
	public static final int COL_DOMAIN = 2;
	
	public static final int COL_CALL_NAME = 1;
	public static final int COL_CALL_DOMAIN = 2;
	public static final int COL_CALL_TYPE = 3;
	

	
	public static final String[] ALL_KEYS = new String[] {KEY_CONTACT_ROWID, KEY_CONTACT_NAME, KEY_CONTACT_DOMAIN};
	public static final String[] ALL_CALL_KEYS = new String[]{KEY_CALL_ROWID,KEY_CALL_NAME,KEY_CALL_DOMAIN,KEY_CALL_TYPE};
	// DB info: it's name, and the table we are using (just one).
	public static final String DATABASE_NAME = "MyDb";
	public static final String DATABASE_TABLE_CONTACTS = "contactsTable";
	public static final String DATABASE_TABLE_CALLS = "callsTable";
	
	// Track DB version if a new version of your app changes the format.
	public static final int DATABASE_VERSION = 2;	
	
	private static final String DATABASE_CREATE_CONTACTS_SQL = 
			"create table " + DATABASE_TABLE_CONTACTS 
			+ " (" + KEY_CONTACT_ROWID + " integer primary key autoincrement, "
			
			/*
			 * CHANGE 2:
			 */
			// TODO: Place your fields here!
			// + KEY_{...} + " {type} not null"
			//	- Key is the column name you created above.
			//	- {type} is one of: text, integer, real, blob
			//		(http://www.sqlite.org/datatype3.html)
			//  - "not null" means it is a required field (must be given a value).
			// NOTE: All must be comma separated (end of line!) Last one must have NO comma!!
			+ KEY_CONTACT_NAME + " text not null, "
			+ KEY_CONTACT_DOMAIN + " text not null "
			
			
			// Rest  of creation:
			+ ");";
	
	private static final String DATABASE_CREATE_CALLS_SQL = 
			"create table " + DATABASE_TABLE_CALLS 
			+ " (" + KEY_CALL_ROWID + " integer primary key autoincrement, "
			+ KEY_CALL_NAME + " text not null, "
			+ KEY_CALL_DOMAIN + " text not null, "
			+ KEY_CALL_TYPE + " text not null "
			+ ");";
	
	
	// Context of application who uses us.
	private final Context context;
	
	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	/////////////////////////////////////////////////////////////////////
	//	Public methods:
	/////////////////////////////////////////////////////////////////////
	
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
	public long insertRow(String name, String domain) {
		/*
		 * CHANGE 3:
		 */		
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CONTACT_NAME, name);
		initialValues.put(KEY_CONTACT_DOMAIN, domain);
		
		
		// Insert it into the database.
		return db.insert(DATABASE_TABLE_CONTACTS, null, initialValues);
	}
	
	public long insertRowIntoCalls(String name,String domain,String type){
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CALL_NAME,name);
		initialValues.put(KEY_CALL_DOMAIN, domain);
		initialValues.put(KEY_CALL_TYPE,type);
		
		return db.insert(DATABASE_TABLE_CALLS, null, initialValues);
	}
	
	// Delete a row from the database, by rowId (primary key)
	public boolean deleteRow(long rowId) {
		String where = KEY_CONTACT_ROWID + "=" + rowId;
		return db.delete(DATABASE_TABLE_CONTACTS, where, null) != 0;
	}
	
	public boolean deleteRowFromCalls(long rowId) {
		String where = KEY_CALL_ROWID + "=" + rowId;
		return db.delete(DATABASE_TABLE_CALLS,where,null) != 0;
	}
	
	public void deleteAll() {
		Cursor c = getAllRows();
		long rowId = c.getColumnIndexOrThrow(KEY_CONTACT_ROWID);
		if (c.moveToFirst()) {
			do {
				deleteRow(c.getLong((int) rowId));				
			} while (c.moveToNext());
		}
		c.close();
	}
	
	public void deleteAllFromCalls(){
		Cursor c = getAllRowsFromCalls();
		long rowId = c.getColumnIndexOrThrow(KEY_CALL_ROWID);
		if (c.moveToFirst()){
			do {
				deleteRowFromCalls(c.getLong((int)rowId));
			} while (c.moveToNext());
		}
		c.close();
	}
	// Return all data in the database.
	public Cursor getAllRows() {
		String where = null;
		Cursor c = 	db.query(true, DATABASE_TABLE_CONTACTS, ALL_KEYS, 
							where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	
	public Cursor getAllRowsFromCalls() {
		String where = null;
		Cursor c = db.query(true, DATABASE_TABLE_CALLS, ALL_CALL_KEYS, where, null, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Get a specific row (by rowId)
	public Cursor getRow(long rowId) {
		String where = KEY_CONTACT_ROWID + "=" + rowId;
		Cursor c = 	db.query(true, DATABASE_TABLE_CONTACTS, ALL_KEYS, 
						where, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	
	public Cursor getRowFromCalls(long rowId){
		String where = KEY_CALL_ROWID + "=" + rowId;
		Cursor c = db.query(false, DATABASE_TABLE_CALLS, ALL_CALL_KEYS, where, null, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}
	
	
	// Change an existing row to be equal to new data.
	public boolean updateRow(long rowId, String name, String domain) {
		String where = KEY_CONTACT_ROWID + "=" + rowId;

		/*
		 * CHANGE 4:
		 */
		// TODO: Update data in the row with new fields.
		// TODO: Also change the function's arguments to be what you need!
		// Create row's data:
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_CONTACT_NAME, name);
		newValues.put(KEY_CONTACT_DOMAIN, domain);
		
		
		// Insert it into the database.
		return db.update(DATABASE_TABLE_CONTACTS, newValues, where, null) != 0;
	}
	
	public boolean updateRowInCalls(long rowId,String name,String domain,String type){
		String where = KEY_CALL_ROWID + "=" +rowId;
		ContentValues newValues = new ContentValues();
		newValues.put(KEY_CALL_NAME,name);
		newValues.put(KEY_CALL_DOMAIN, domain);
		newValues.put(KEY_CALL_TYPE,type);
		
		return db.update(DATABASE_TABLE_CALLS, newValues, where, null) != 0;
	}
	
	/////////////////////////////////////////////////////////////////////
	//	Private Helper Classes:
	/////////////////////////////////////////////////////////////////////
	
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
			_db.execSQL(DATABASE_CREATE_CONTACTS_SQL);
			_db.execSQL(DATABASE_CREATE_CALLS_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version " + oldVersion
					+ " to " + newVersion + ", which will destroy all old data!");
			
			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CONTACTS);
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CALLS);
			
			// Recreate new database:
			onCreate(_db);
		}
	}
}
