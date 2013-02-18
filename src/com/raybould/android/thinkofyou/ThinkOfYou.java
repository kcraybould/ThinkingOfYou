package com.raybould.android.thinkofyou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;;

public class ThinkOfYou extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // open with a splash page, then go to the main text selection page
        Thread splashThread = new Thread() {
            @Override
            public void run() {
               try {
                  int waited = 0;
                  while (waited < 5000) {
                     sleep(100);
                     waited += 100;
                  }
               } catch (InterruptedException e) {
                  // do nothing
               } finally {
                  finish();
                  displayText();
               }
            }
         };
         splashThread.start();
      
    }
    
    /**
     * This takes us to the activity that displays the randomly chosen text ...
     */
    public void displayText() {
    	Intent showText = new Intent (this, ShowText.class);
    	startActivity(showText);
    }
}