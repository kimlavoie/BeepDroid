package com.example.beepandroid;

import java.io.File;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.app.Activity;
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

	Spinner fileListSpinner;
	TextView output;
	ArrayList<String> files = new ArrayList<String>();
	BeepBeepThread monitorThread;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        fileListSpinner = (Spinner) findViewById(R.id.FileList);
        output = (TextView) findViewById(R.id.textView1);
        
        initializeFileList();
        monitorThread = new BeepBeepThread(output);
        Thread t = new Thread(monitorThread);
        t.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void initializeFileList(){
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
    	Button bouton = (Button) v;
    	
    	
    	if(bouton.getText().equals("Start")){
    		bouton.setText("Stop");
    		monitorThread.changeSpec((String)fileListSpinner.getSelectedItem());
    		monitorThread.pause();
    		fileListSpinner.setEnabled(false);
    		Toast.makeText(this, "Monitor started", Toast.LENGTH_LONG).show();
    	}
    	else{
    		bouton.setText("Start");
    		monitorThread.unPause();
    		fileListSpinner.setEnabled(true);
    		Toast.makeText(this, "Monitor stopped", Toast.LENGTH_LONG).show();
    	}
    }
    
    public void changeOutput(String out){
    	output.setText(out);
    }
    
}
