package voiplicenta.contacts;

import voiplicenta.calls.CallsLog;
import voiplicenta.database.DBAdapter;
import voiplicenta.main.R;
import voiplicenta.main.VoIPLicentaMainActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class Contacts extends Activity {


	protected static final int CALL_CONTACT = 1;
	String contact = "";
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
		setContentView(R.layout.contacts_layout);
		
		Log.d("BaseContext",getBaseContext().toString());
	
		openDB();
		populateContactListFromDB();
		registerListClickCallback();
		
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		openDB();
		populateContactListFromDB();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDB();
	}
	
	private void populateContactListFromDB() {
		Cursor cursor = myDB.getAllRows();
		// Allow activity to manage lifetime of the cursor
		startManagingCursor(cursor);
		
		//Set up mapping from cursor to view fields
		String[] fromFieldNames = new String[]
				{DBAdapter.KEY_CONTACT_NAME,DBAdapter.KEY_CONTACT_DOMAIN,};
		int[] toViewIDs = new int[]
				{R.id.contact_item_name              ,R.id.contact_item_domain};
		//Create an adapter to map elements of the db into the UI
		SimpleCursorAdapter myCursorAdapter = new SimpleCursorAdapter(
				this, //context, 
				R.layout.contact_item_layout, //layout,
				cursor, //cursor
				fromFieldNames,
				toViewIDs);
		
		//Set the adapter for the listview
		ListView myList = new ListView(getBaseContext());
		myList = (ListView) findViewById(R.id.listOfContacts);
		myList.setAdapter(myCursorAdapter);
		
	}
	private void registerListClickCallback() {
		// TODO Auto-generated method stub
				ListView myList = (ListView) findViewById(R.id.listOfContacts);
				myList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View viewClicked, int position,
							long idInDB) {
						// TODO Auto-generated method stub
						Cursor cursor = myDB.getRow(idInDB);
						if (cursor.moveToFirst()){
							long idDB = cursor.getLong(DBAdapter.COL_ROWID);
							String name = cursor.getString(DBAdapter.COL_NAME);
							String domain = cursor.getString(DBAdapter.COL_DOMAIN);
							
							contact = name+"@"+domain;
							
							//Toast.makeText(Contacts.this, contact, Toast.LENGTH_LONG).show();
							showDialog(CALL_CONTACT);
						
						}
						cursor.close();
					}
				});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id){
			case CALL_CONTACT:
				//LayoutInflater factory = LayoutInflater.from(Contacts.this);
                //final View callContactFromContactsView = factory.inflate(R.layout.contact_options_layout, null);
                //final EditText cContact = (EditText)(callContactFromContactsView.findViewById(R.id.contact_option_CallContactLabel));
                //cContact.setText(contact);
                        
                return new AlertDialog.Builder(this)
                .setTitle("Call "+contact+"?")
                //.setView(callContactFromContactsView)
                .setPositiveButton(
                        android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {                             
                               // Toast.makeText(Contacts.this,contact, Toast.LENGTH_LONG).show();
                            	VoIPLicentaMainActivity voipMain = (VoIPLicentaMainActivity) getBaseContext();
                            	voipMain.sipAddress=contact;
                            	voipMain.initiateCall();
                            	
                            }
                })
                .setNegativeButton(
                        android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Noop.
                            	//Toast.makeText(Contacts.this, "Sau nu", Toast.LENGTH_LONG).show();
                            }
                })
                .create();
		}
		return null;
	}
}
