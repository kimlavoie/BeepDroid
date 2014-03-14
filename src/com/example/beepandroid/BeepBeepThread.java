package com.example.beepandroid;

import java.io.BufferedReader;
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
		this.parent = parent;
	}
	
	private class CommunicationThread extends Thread{
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
			EventNotifier en = new EventNotifier(true);
			PipeReader pr;
			try
			{
			  parent.changeOutput("In CommunicationThread.run");
			  MonitorFactory mf = new MonitorFactory();
			  String formula_contents = FileReadWrite.readFile(Environment.getExternalStorageDirectory().getPath() + "/Documents/ltlfo/" + spec);
			  Operator op = Operator.parseFromString(formula_contents);
			  op.accept(mf);
			  Monitor mon = mf.getMonitor();
			  Map<String,String> metadata = getMetadata(formula_contents);
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

			
			while(true){
				try{
					parent.changeOutput(en.formatVerdicts());
					System.out.println("Verdict: " + en.formatVerdicts());
					Thread.sleep(1000);
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
	
	public void run(){
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
}
