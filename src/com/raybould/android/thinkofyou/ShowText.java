package com.raybould.android.thinkofyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;	
import android.telephony.SmsManager;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Random;

/**
 * 
 * @author Kevin
 *
 * This ties to the display text activity.  Here, we will display
 * the randomly chosen text and then show two buttons, one to send
 * one to choose another bit of text. 
 */
public class ShowText extends Activity {
	private static final int CONTENT_PICKER_RESULT = 1001;
	private String number, msg;
	
	// gettors and settors
	public void setNumber(String value) {
		this.number = value;
	}
	
	public String getNumber() {
		return(this.number);
	}
	
	public void setMsg(String value) {
		this.msg = value;
	}
	
	public String getMsg() {
		return(this.msg);
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choosetext);
        
        // go get the text
        final TextView chosenText = (TextView)findViewById(R.id.chosenText);
        setMsg(pickText());
        chosenText.setText(getMsg());
        
        //set up the buttons
        // rechoose button
        Button chooseTextButton = (Button)findViewById(R.id.rechooseButton);
        // setup listener so that the button actually does something ..
        chooseTextButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick (View view){
        		setMsg(pickText());
        		chosenText.setText(getMsg());
        	}
        });
        
        // text button
        Button sendButton = (Button)findViewById(R.id.sendButton);
        // setup listener so that the button actually does something ..
        sendButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick (View view){
        		// pick the contact and send the text ...
        		sendText();
        	}
        });
    }
    
    /**
     * This function picks the quote and returns it.
     * @return: a string with a random quote
     */
    private String pickText() {
    	String text = "", line;
    	InputStream stream = null;
    	InputStreamReader streamReader = null;
    	
    	//let us use setup the dialogs we are going to need in the event of errors ...
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	// listen for the on click event ...
    	alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// just going to close, which android handles for us
				// good bye alert, parting is such sweet sorrow ...
			}
		});
    	
    	int i = 0;
    	
    	// so maybe not the best way to do this, but this is more about creating an android app
    	// rather than load the whole file in, get a random number and then read the lines until we 
    	// get to the line.  Since we control the file at this stage, we can ensure the number of lines
    	// in the file match.  
    	int iLines = getResources().getInteger(R.integer.number_of_lines);
    	
    	// figure out the line to stop at
    	Random gen = new Random();
    	int iStop = gen.nextInt(iLines);
    	
    	// Only including the try...catch here because java requires it
		// I made the decision to wrap the file reader creation separately in order to better
		// calibrate the error responses and keep the error handling to a minimum in deference
		// to the class rules.  When I say calibrate the error response, I mean doing things like
		// allowing the user to renter the data, etc.
		try {
			stream = getResources().openRawResource(R.raw.quotes);
			streamReader = new InputStreamReader(stream);
		} catch (NotFoundException ex) {
			// out to the console, because the console is the output for the rest of the
			// program
			Log.e("File could not be opened: %s\n", ex.getMessage());
			alert.setMessage("File could not be opened: " + ex.getMessage());
			alert.show();
		}
    	// now, open the file and read until we get to the chosen line ..
    	// now, let us use a BufferedReader to get the individual lines ...
		BufferedReader buffReader = new BufferedReader(streamReader);
		
		// go read the file, line by line ..
		// once again, Java requires handling an exception, so we handle the exception.
		// again, it is conscious decision to keep the try block at the level of the buffered
		// reader: in a normal program, this would give us more flexibility and help keep
		// the code clearer for maintenance.
		try {
			line = buffReader.readLine();
			while (line != null) {
				line = buffReader.readLine();
				if (i == iStop) {
					text = line;
					break;
				}
				i++;
			} 
		} catch (IOException ex) {
			Log.e("File could not be read: %s\n", ex.getMessage());
			alert.setMessage("File could not be read: " +  ex.getMessage());
			alert.show();
		} finally {
			// lets close the file here, since we cannot read from it
			try {
				stream.close();
			} catch (IOException cex) {
				// out to the console, because the console is the output for the rest of the
				// program
				Log.e("File could not be closed: %s\n", cex.getMessage());
				alert.setMessage("File could not be closed: " + cex.getMessage());
				alert.show();
			}
		}
    	return(text);
    }
    
    /**
     * This function calls the pick contact activity and the uses the mobile number or, failing that,
     * the primary number to send the text ..
     */
    private void sendText() {
    	// let's show the contact picker
    	Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    	startActivityForResult(contactPickerIntent, CONTENT_PICKER_RESULT);
    }
    
    /**
     * Callback function for the contact picker Intent
     * We will make sure we have a good result, then pull either the mobile number
     * or the primary number.  Then call the function that sends the text.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
    	// let the user know
    	//let us use setup the dialogs we are going to need in the event of errors ...
    	AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	// listen for the on click event ...
    	alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// just going to close, which android handles for us
				// good bye alert, parting is such sweet sorrow ...
			}
		});
    	
        if (resultCode == RESULT_OK) {  
            switch (requestCode) {  
            case CONTENT_PICKER_RESULT:  
            	// let us find the number ...
            	Cursor cursor;
            	String phonePrimNumber = null;
            	String phoneMobNumber = null;
            	String phoneType;
            	try {
            		Uri result = data.getData();
            		// see if we have a mobile number ..
            		cursor = managedQuery(result, null,null,null,null);
            		if(cursor.moveToFirst()) {
            			// lets see if any number exists ..
            			String id = result.getLastPathSegment();
 
        				// yeah, we have a number, so lets see which one it is ..
        				
        				Cursor phoneCursor = getContentResolver().query(Phone.CONTENT_URI , null,
        																Phone.CONTACT_ID + "=?", new String[] { id },
        																null);
        				
        				//walk through the numbers and see if the mobile exists ...
        				for(phoneCursor.moveToFirst(); !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
        					phoneType = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.TYPE));
        					if (phoneType.equals("2")) {
        						phoneMobNumber = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
        						break;
        					} else if (phoneType.equals("1")) {
        						phonePrimNumber = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER));
        					}
        				}			
            		}
            	} catch (Exception ex) {
            		alert.setMessage("No phone number was found! " + ex.getMessage());
    				alert.show();
    				return;
            	}
            	if (phoneMobNumber == null) {
            		if(phonePrimNumber == null) {
            			alert.setMessage("No phone number was found!");
        				alert.show();
            		} else {
            			setNumber(phonePrimNumber);
            		}
            	} else {
            		setNumber(phoneMobNumber);
            	}
            }  
        } else {  
            // gracefully handle failure  
            Log.e("error", "Could not retrieve contact information"); 
            alert.setMessage("No phone number was found!");
			alert.show();
			return;
        } 
        
        // now, go send the text ..
        sendMsg();
    }  
    
    /**
     * This function sends the SMS message, after pre-pending an introduction
     */
    private void sendMsg() {
    	StringBuilder sbMsg = new StringBuilder("Someone was thinking of you and thought you should know this:\n");
    	
    	// set up the alert that we will use to notify user when the text does or does not go through 
    	final AlertDialog.Builder alert = new AlertDialog.Builder(this);
    	
    	// listen for the on click event ...
    	alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// just going to close, which android handles for us
				// good bye alert, parting is such sweet sorrow ...
			}
		});
    	
    	// now, set the progress bar up
    	final ProgressDialog prog = new ProgressDialog(this);
    	
    	// add the message ..
    	sbMsg.append(getMsg());
    	
    	// send the message
    	SmsManager sms = SmsManager.getDefault();
    	
    	// now, let us wait and see if the message gets through ..
    	Intent msgSent = new Intent("ACTION_MSG_SENT");
    	
    	PendingIntent pendingMsgSent = PendingIntent.getBroadcast(this, 0, msgSent, 0);
    	
    	registerReceiver(new BroadcastReceiver() {
    		public void onReceive (Context context, Intent intent) {
    			int result = getResultCode();
    			if (result != Activity.RESULT_OK) {
    				prog.dismiss();
    				alert.setMessage("The message failed with code " + result);
    				alert.show();
    			} else {
    				prog.dismiss();
    				alert.setMessage("Your message has been sent!");
    				alert.show();
    			}
    		}
    	}, new IntentFilter("ACTION_MSG_SENT"));
    	
    	sms.sendTextMessage(getNumber(), null, getMsg(), pendingMsgSent, null );
    	// start the progress bar ...
    	prog.setTitle("Sending Text");
    	prog.setMessage("Your thought is on its way ...");
    	prog.show();
    }
}   