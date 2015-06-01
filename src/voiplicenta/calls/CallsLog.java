package voiplicenta.calls;

import voiplicenta.database.DBAdapter;
import voiplicenta.main.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class CallsLog extends Activity{

	private static final int ADD_CONTACT = 1;
	String contactName = "";
	String contactDomain = "";
	DBAdapter myDB;
	private void openDB(){
		myDB = new DBAdapter(getApplicationContext());
		myDB.open();
	}
	private void closeDB(){
		myDB.close();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calls_log_layout);
		openDB();
		populateListFromDB();
		registerListClickCallback();
	}
	

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		openDB();
		populateListFromDB();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDB();
	}
		
	private void populateListFromDB() {
		Cursor cursor = myDB.getAllRowsFromCalls();
		// Allow activity to manage lifetime of the cursor
		startManagingCursor(cursor);
		
		//Set up mapping from cursor to view fields
		String[] fromFieldNames = new String[]
				{DBAdapter.KEY_CALL_NAME,		DBAdapter.KEY_CALL_DOMAIN,	DBAdapter.KEY_CALL_TYPE};
		int[] toViewIDs = new int[]
				{R.id.call_item_name,			R.id.call_item_domain,		R.id.call_item_type};
		//Create an adapter to map elements of the db into the UI
		SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(
				this, //context, 
				R.layout.call_item_layout, //layout,
				cursor, //cursor
				fromFieldNames,
				toViewIDs);
		
		//Set the adapter for the listview
		ListView myList = (ListView) findViewById(R.id.listOfCalls);
		myList.setAdapter(myCursorAdapter);
		
	}
	
	private void registerListClickCallback() {
		// TODO Auto-generated method stub
		ListView myList = (ListView) findViewById(R.id.listOfCalls);
		myList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View viewClicked, int position,
					long idInDB) {
				// TODO Auto-generated method stub
				Cursor cursor = myDB.getRowFromCalls(idInDB);
				if (cursor.moveToFirst()){
					long idDB = cursor.getLong(DBAdapter.COL_CALL_ROWID);
					String name = cursor.getString(DBAdapter.COL_CALL_NAME);
					String domain = cursor.getString(DBAdapter.COL_CALL_DOMAIN);
					contactName=name;
					contactDomain=domain;
					String contact = "ContactName: "+ name + "\n"
									+ "ContactDomain: " + domain;
					
					//Toast.makeText(CallsLog.this, contact, Toast.LENGTH_LONG).show();
					showDialog(ADD_CONTACT);
//					LayoutInflater factory = LayoutInflater.from(CallsLog.this);
//	                final View addContactFromCallsView = factory.inflate(R.layout.add_contact_from_calls, null);
//	                
				}
				cursor.close();
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id){
			case ADD_CONTACT:
				LayoutInflater factory = LayoutInflater.from(CallsLog.this);
                final View addContactFromCallsView = factory.inflate(R.layout.add_contact_from_calls, null);
                final EditText cName = (EditText)(addContactFromCallsView.findViewById(R.id.add_contact_from_calls_nameEditText));
                cName.setText(contactName);
                final EditText cDomain = (EditText)(addContactFromCallsView.findViewById(R.id.add_contact_from_calls_domainEditText));
                cDomain.setText(contactDomain);                
                return new AlertDialog.Builder(this)
                .setTitle("Add Contact")
                .setView(addContactFromCallsView)
                .setPositiveButton(
                        android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                myDB.insertRow(cName.getText().toString(), cDomain.getText().toString());
                                Toast.makeText(CallsLog.this,cName.getText().toString()+"\n"+cDomain.getText().toString(), Toast.LENGTH_LONG).show();
                            }
                })
                .setNegativeButton(
                        android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Noop.
                            	Toast.makeText(CallsLog.this, "Anulare", Toast.LENGTH_LONG).show();
                            }
                })
                .create();
		}
		return null;
	}
}
