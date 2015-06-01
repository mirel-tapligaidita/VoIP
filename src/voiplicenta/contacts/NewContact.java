package voiplicenta.contacts;

import voiplicenta.database.DBAdapter;
import voiplicenta.main.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NewContact extends Activity{

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
		setContentView(R.layout.new_contact_layout);
		openDB();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		openDB();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDB();
	}
	
	public void addContact(View v){
		EditText name = (EditText) findViewById(R.id.add_contact_name_editText);
		EditText domain = (EditText) findViewById(R.id.add_contact_domain_editText);
		
		if (myDB.insertRow(name.getText().toString(), domain.getText().toString())!=0){
			Context context = getApplicationContext();
			CharSequence text = "Inserted row"+name.getText().toString()+" "+domain.getText().toString();
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
			
	}
}
