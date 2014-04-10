package com.example.beepandroid;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.app.Activity;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	final int TIMER_TICKS = 1;
	Spinner fileListSpinner;
	TextView output;
	String outputMessage = "No monitor has been started yet.";
	Timer timer;
	ArrayList<String> files = new ArrayList<String>();
	BeepBeepThread monitorThread;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //gain access to our ressources
        fileListSpinner = (Spinner) findViewById(R.id.FileList);
        output = (TextView) findViewById(R.id.textView1);
        
        initializeFileList();
        
        //timer to update the TextView at each TIMER_TICKS time
	    timer = new Timer();
	    timer.schedule(new TimerTask() {
	      @Override
	      public void run() {
	      	try{
	      		runOnUiThread(new Runnable(){
	      			public void run(){
	      				output.setText(outputMessage);
	    	       		output.invalidate();
	      			}
	      		});
	       	}catch(Exception e){
	       		e.printStackTrace();
	       	}
	      }
	    }, 0, TIMER_TICKS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void initializeFileList(){
    	/**
    	 * Check for ltlfo directory and show the list of available files (ending with ltlfo extension)
    	 */
    	File ltlfoDir = new File(Environment.getExternalStorageDirectory().getPath() + "/Documents/ltlfo");
    	if(!ltlfoDir.exists()){
    		System.out.println("The ltlfo directory doesn't exist!!!");
    		return;
    	}
    	File fileList[] = ltlfoDir.listFiles();
    	for(File file: fileList){
    		String fileName = file.getName();
    		String extension = fileName.substring(fileName.length() - 5);
    		
    		if(extension.equals("ltlfo"))
    			files.add(fileName);
    	}
    	
    	ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, files);
    	fileListSpinner.setAdapter(aa);
    }
    
    public void buttonClicked(View v){
    	/**
    	 * Switch between activated state and deactivated state
    	 * Deactivated -> activated = create a monitor thread with spec selected
    	 * 		and switch button state
    	 * Activated -> Deactivated = stop listening on port, interrupt thread
    	 * 		and switch button state
    	 */
    	Button bouton = (Button) v;
    	
    	if(bouton.getText().equals("Start")){
    		bouton.setText("Stop");
            monitorThread = new BeepBeepThread(this);
            monitorThread.changeSpec((String)fileListSpinner.getSelectedItem());
            Thread t = new Thread(monitorThread);
            t.start();
    		fileListSpinner.setEnabled(false);
    		Toast.makeText(this, "Monitor started", Toast.LENGTH_LONG).show();
    	}
    	else{
    		bouton.setText("Start");
    		monitorThread.stopBeepBeep();
    		monitorThread.interrupt();
    		fileListSpinner.setEnabled(true);
    		Toast.makeText(this, "Monitor stopped", Toast.LENGTH_LONG).show();
    	}
    }
    
    public void changeOutput(String out){
    	/**
    	 * Change the TextView string output. We change only this variable,
    	 * 		which will be used to update the TextView when a timer
    	 * 		is ready.
    	 */
    	outputMessage = out;
    }
    
    public void notify(String caption, String notification){
    	/**
    	 * Notify system with the caption and notification message.
    	 * Used primarily when monitor state change (from inconclusive
    	 * 		to true or false)
    	 */
    	long[] vibration = {0, 1000};
    	NotificationCompat.Builder mBuilder =
    		    new NotificationCompat.Builder(this)
    		    .setSmallIcon(R.drawable.ic_launcher)
    		    .setContentTitle("Verdict for " + caption + ":")
    		    .setContentText(notification)
    		    .setVibrate(vibration);
    	// Sets an ID for the notification
    	int mNotificationId = 001;
    	// Gets an instance of the NotificationManager service
    	NotificationManager mNotifyMgr = 
    	        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	// Builds the notification and issues it.
    	mNotifyMgr.notify(mNotificationId, mBuilder.build());
    	
    }
    
}
