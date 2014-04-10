package com.example.beepandroid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import ca.uqac.info.ltl.Operator;
import ca.uqac.info.monitor.Monitor;
import ca.uqac.info.monitor.MonitorFactory;
import ca.uqac.info.util.FileReadWrite;
import ca.uqac.info.util.PipeReader;
import ca.uqac.info.monitor.frontend.EventNotifier;

public class BeepBeepThread extends Thread {
	boolean pause = true;
	String spec;
	MainActivity parent;
	ServerSocket serverSocket = null;
	ArrayList<CommunicationThread> clients = new ArrayList<CommunicationThread>();
	
	public BeepBeepThread(MainActivity parent){
		this.parent = parent;	//to update the TextView
	}
	
	public void run(){
		/**
		 * Start listening for socket connections
		 */
		Socket socket = null;
		try{
			serverSocket = new ServerSocket(6000);
		} catch (IOException e){
			e.printStackTrace();
		}
			try{
				socket = serverSocket.accept();
				CommunicationThread ct = new CommunicationThread(socket);
				clients.add(ct);
				ct.start();
				if(pause){
					ct.pause();
				}
			} catch (IOException e){
				e.printStackTrace();
			}
	}
	
	public void changeSpec(String filename){
		this.spec = filename;
	}
	
	public void stopBeepBeep(){
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void pause(){
		pause = true;
		for(CommunicationThread c: clients){
			c.pause();
		}
	}
	public void unPause(){
		pause = false;
		for(CommunicationThread c: clients){
			c.unpause();
		}
	}
	
	public static Map<String,String> getMetadata(String fileContents)
	  /**
	   * From BeepBeep, to parse metadatas
	   * (I don't understand all of it, so I just copied it here)
	   */
	  {
	    Map<String,String> out_map = new HashMap<String,String>();
	    StringBuilder comment_contents = new StringBuilder();
	    Pattern pat = Pattern.compile("^\\s*?#(.*?)$", Pattern.MULTILINE);
	    Matcher mat = pat.matcher(fileContents);
	    while (mat.find())
	    {
	      String line = mat.group(1).trim();
	      comment_contents.append(line).append(" ");
	    }
	    pat = Pattern.compile("@(\\w+?)\\((.*?)\\);");
	    mat = pat.matcher(comment_contents);
	    while (mat.find())
	    {
	      String key = mat.group(1);
	      String val = mat.group(2).trim();
	      if (val.startsWith("\"") && val.endsWith("\""))
	      {
	        // Trim double quotes if any
	        val = val.substring(1, val.length() - 1);
	      }
	      out_map.put(key, val);
	    }
	    return out_map;
	  }
	
	private class CommunicationThread extends Thread{
		/**
		 * Called to establish communication between distance source and the monitor
		 */
		Socket clientSocket;
		BufferedReader input;
		TextView output;
		boolean pause = false;
		
		public CommunicationThread (Socket socket){
			clientSocket = socket;
			try{
				this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public void run(){
			/**
			 * Start listening for events and update things accordingly
			 */
			/* ******************************** From BeepBeep, with some minor modifications ***************************************/
			EventNotifier en = new EventNotifier(true);
			PipeReader pr;
			String caption = "";
			try
			{
			  parent.changeOutput("In CommunicationThread.run");
			  MonitorFactory mf = new MonitorFactory();
			  String formula_contents = FileReadWrite.readFile(Environment.getExternalStorageDirectory().getPath() + "/Documents/ltlfo/" + spec);
			  Operator op = Operator.parseFromString(formula_contents);
			  op.accept(mf);
			  Monitor mon = mf.getMonitor();
			  Map<String,String> metadata = getMetadata(formula_contents);
			  caption = metadata.get("Caption");
			  metadata.put("Filename", spec);
			  en.addMonitor(mon, metadata);
			  pr = new PipeReader(clientSocket.getInputStream(), en, false);
			  pr.setSeparator("message", null);
			  en.reset();
			  Thread th = new Thread(pr);
			  th.start();
			}
			catch (IOException e)
			{
			  e.printStackTrace();
			  System.exit(-32);
			}
			catch (Operator.ParseException e)
			{
			  System.err.println("Error parsing input formula");
			  System.exit(-2);
			}
			/* ********************************************************************************************************************/
			
			//Check for verdict change and update TextView accordingly
			String verdict = en.formatVerdicts();
			parent.changeOutput(verdict);
			EventNotifier.MonitorInfo infos = en.getMonitorInfo(0);
			long nb_mes = infos.m_numEvents;
			while(true){
				try{
					
					if(infos.m_numEvents != nb_mes){
				    	String message = 	en.formatVerdicts() + "\n" +
				    						"Cumulative time: " + infos.m_cumulativeTime + "\n" +
				    						"Nb mess.: " + infos.m_numEvents + "\n" +
				    						"Time by mess.: " + (infos.m_cumulativeTime / infos.m_numEvents);
				    	if(!en.formatVerdicts().equals(verdict)){
							parent.notify(caption,message);
							verdict = en.formatVerdicts();
				    	}
						parent.changeOutput(message);
					}
					if(pause){
						Thread.sleep(1000);
					}
					else{
						
					}
				} catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}
		
		public void pause(){pause = true;}
		public void unpause(){pause = false;}
	}
}
