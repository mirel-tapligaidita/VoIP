/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package voiplicenta.main;

import java.text.ParseException;

import voiplicenta.calls.IncomingCallReceiver;
import voiplicenta.database.DBAdapter;
import voiplicenta.settings.SipSettings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipAudioCall.Listener;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Handles all calling, receiving calls, and UI interaction in the WalkieTalkie app.
 */
public class VoIPLicentaMainActivity extends Activity implements View.OnTouchListener {
	
	
	DBAdapter myDB;
	private void openDB(){
		myDB = new DBAdapter(this);
		myDB.open();
	}
	private void closeDB(){
		myDB.close();
	}
    public String sipAddress = "";
    
    public SipManager manager = null;
    public SipProfile me = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;

    private static final int CALL_ADDRESS = 1;
    private static final int SET_AUTH_INFO = 2;
    private static final int UPDATE_SETTINGS_DIALOG = 3;
    private static final int HANG_UP = 4;
	private static final int CALLS_HISTORY = 5;
	private static final int CONTACTS = 6;
	private static final int ADD_CONTACT = 7;

	private static final int INCOMING_CALL_DIALOG = 8;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.voiplicenta_layout);
        openDB();
        

        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        // "Push to talk" can be a serious pain when the screen keeps turning off.
        // Let's prevent that.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeManager();
    }

    @Override
    public void onStart() {
        super.onStart();
        // When we get back from the preference setting Activity, assume
        // settings have changed, and re-login with new auth info.
        openDB();
        initializeManager();
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.close();
        }

        closeLocalProfile();

        if (callReceiver != null) {
            this.unregisterReceiver(callReceiver);
        }
        closeDB();
    }

    public void initializeManager() {
        if(manager == null) {
          manager = SipManager.newInstance(this);
        }

        Context context = getApplicationContext();
		CharSequence text = "Urmeaza sa se initializeze profilul local";
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, text, duration);
		toast.show();
        
        initializeLocalProfile();
    }

    /**
     * Logs you into your SIP provider, registering this device as the location to
     * send SIP calls to for your SIP address.
     */
    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }

        if (me != null) {
            closeLocalProfile();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String username = prefs.getString("namePref", "");
        String domain = prefs.getString("domainPref", "");
        String password = prefs.getString("passPref", "");
        
//        Context context = getApplicationContext();
//		CharSequence text = username+" "+domain;
//		int duration = Toast.LENGTH_SHORT;
//
//		Toast toast = Toast.makeText(context, text, duration);
//		toast.show();
        
        if (username.length() == 0 || domain.length() == 0 || password.length() == 0) {
            showDialog(UPDATE_SETTINGS_DIALOG);
            return;
        }

        try {        	       	        	
            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setPassword(password);
            me = builder.build();

            Intent i = new Intent();
            i.setAction("android.SipDemo.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
            manager.open(me, pi, null);
           
            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.
            SipRegistrationListener listener = new SipRegistrationListener() {
				
				@Override
				public void onRegistrationFailed(String localProfileUri, int errorCode,
						String errorMessage) {
					// TODO Auto-generated method stub
						
                        updateStatus("Registration failed.  Please check settings.");
						
				}
				
				@Override
				public void onRegistrationDone(String localProfileUri, long expiryTime) {
					// TODO Auto-generated method stub
					updateStatus("Ready");
				}
				
				@Override
				public void onRegistering(String localProfileUri) {
					// TODO Auto-generated method stub
					
                    updateStatus("Registering with SIP Server...");
					
				}
			};
			 manager.register(me, 20,listener);
			 manager.setRegistrationListener(me.getUriString(), listener);

        } catch (ParseException pe) {
            updateStatus("Connection Error.");
        } catch (SipException se) {
            updateStatus(se.getMessage());
        }
        
        
    }

    /**
     * Closes out your local profile, freeing associated objects into memory
     * and unregistering your device from the server.
     */
    public void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (me != null) {
                manager.close(me.getUriString());
            }
        } catch (Exception ee) {
            Log.d("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
        }
    }

    /**
     * Make an outgoing call.
     */
    public void initiateCall() {

        updateStatus(sipAddress);

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    call.toggleMute();
                    updateStatus(call);
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    updateStatus("Ready.");
                }
            };

            call = manager.makeAudioCall(me.getUriString(), sipAddress, listener, 30);
            myDB.insertRowIntoCalls("Apel realizat", "cu succes", "OutgoingCall");
        }
        catch (Exception e) {
            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
            if (me != null) {
                try {
                    manager.close(me.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }

    /**
     * Updates the status box at the top of the UI with a messege of your choice.
     * @param status The String to display in the status box.
     */
    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) findViewById(R.id.sipLabel);
                labelView.setText(status);
            }
        });
    }

    /**
     * Updates the status box with the SIP address of the current call.
     * @param call The current, active call.
     */
    public void updateStatus(SipAudioCall call) {
        String useName = call.getPeerProfile().getDisplayName();
        if(useName == null) {
          useName = call.getPeerProfile().getUserName();
        }
        updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
    }

    /**
     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
     * @param v The View where the touch event is being fired.
     * @param event The motion to act on.
     * @return boolean Returns false to indicate that the parent view should handle the touch event
     * as it normally would.
     */
    public boolean onTouch(View v, MotionEvent event) {
     
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,	CALL_ADDRESS,	0,	"Call someone");
        menu.add(0, SET_AUTH_INFO,	0,	"Edit your SIP Info.");
        menu.add(0, HANG_UP,		0,	"End Current Call.");
        menu.add(0, CALLS_HISTORY,	0,	"Call Log");
        menu.add(0, CONTACTS,		0,	"Contacts");
        menu.add(0,	ADD_CONTACT,	0,	"Add Contact");

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CALL_ADDRESS:
                showDialog(CALL_ADDRESS);
                break;
            case SET_AUTH_INFO:
                updatePreferences();
                break;
            case HANG_UP:
                if(call != null) {
                    try {
                      call.endCall();
                    } catch (SipException se) {
                        Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                }
                break;
            case CALLS_HISTORY:
            	showCallsLog();
            	break;
            case CONTACTS:
            	showContacts();
            	break;
            case ADD_CONTACT:
            	addContact();
            	break;
        }
        return true;
    }

    private void addContact() {
		// TODO Auto-generated method stub
		Intent addContactActivity = new Intent(getBaseContext(),voiplicenta.contacts.NewContact.class);
		startActivity(addContactActivity);
	}

	private void showContacts() {
		// TODO Auto-generated method stub
		Intent contactsActivity = new Intent(getBaseContext(),voiplicenta.contacts.Contacts.class);
		startActivity(contactsActivity);
	}

	private void showCallsLog() {
		// TODO Auto-generated method stub
		Intent callsLogActivity = new Intent(getBaseContext(),voiplicenta.calls.CallsLog.class);
		startActivity(callsLogActivity);
	}

	@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CALL_ADDRESS:

                LayoutInflater factory = LayoutInflater.from(this);
                final View textBoxView = factory.inflate(R.layout.call_address_dialog, null);
                return new AlertDialog.Builder(this)
                        .setTitle("Call Someone.")
                        .setView(textBoxView)
                        .setPositiveButton(
                                android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        EditText textField = (EditText)
                                                (textBoxView.findViewById(R.id.calladdress_edit));
                                        sipAddress = textField.getText().toString();
                                        initiateCall();

                                    }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Noop.
                                    }
                        })
                        .create();

            case UPDATE_SETTINGS_DIALOG:
                return new AlertDialog.Builder(this)
                        .setMessage("Please update your SIP Account Settings.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                updatePreferences();
                            }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Noop.
                                    }
                        })
                        .create();
            case INCOMING_CALL_DIALOG:
            	String useName = call.getPeerProfile().getDisplayName();
                if(useName == null) {
                  useName = call.getPeerProfile().getUserName();
                }
                String incomingCallProfile = useName + "@" + call.getPeerProfile().getSipDomain();
                myDB.insertRowIntoCalls(useName, call.getPeerProfile().getSipDomain(), "IncomingCall");
                
                return new AlertDialog.Builder(this)
                        .setTitle(incomingCallProfile)
                        .setPositiveButton(
                                android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        try {
											call.answerCall(30);
										} catch (SipException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
                                        call.startAudio();
                                        call.setSpeakerMode(true);
                                        if(call.isMuted()) {
                                        	call.toggleMute();
                                    }
                                    }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        try {
											call.endCall();
										} catch (SipException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
                                    }
                        })
                        
                        .create();
        }
        return null;
    }

    public void updatePreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),
                SipSettings.class);
        startActivity(settingsActivity);
    }
    
    public void onIncomingCallDialog(SipAudioCall incomingCall) {
		// Be a good citizen.  Make sure UI changes fire on the UI thread.
        this.runOnUiThread(new Runnable() {
            public void run() {
            	showDialog(INCOMING_CALL_DIALOG);
            }
        });
    }
}
