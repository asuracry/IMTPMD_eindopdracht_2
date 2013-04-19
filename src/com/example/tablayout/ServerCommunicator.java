package com.example.tablayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.ServerSocket;

import android.util.Log;

public class ServerCommunicator implements Runnable
{
	private MainActivity activity;
	private Thread thread;
	
	private String clientMessage, serverMessage;
	private String ip;
	private int port;
	ServerSocket serverSocket;
	Socket clientSocket;
	
	public ServerCommunicator( MainActivity activity, String ip, int port, String clientMessage ){	
		//we gebruiken de activity om de userinterface te updaten
		this.activity = activity;
		
		//gegevens om naar de server te verbinden en een message te sturen
		this.clientMessage = clientMessage;
		this.ip = ip;
		this.port = port;
		
		//de nieuwe thread kan tekst verzenden en ontvangen van en naar een server
		this.thread = new Thread(this);
		this.thread.run();
	}
	

	//dit is een methode die niet op de UI thread wordt aangeroepen, maar door onze eigen nieuwe thread
	//we kunnen dus niet zomaar ontvangen berichten in een userinterface object stoppen m.b.v. view.setText( message )
	//hier gebruiken we de activity voor: activity.runOnUiThread( activity )
	@Override
	public void run(){
		try{
			clientSocket = new Socket();
			clientSocket.connect(new InetSocketAddress(this.ip, this.port), 4000);
			
			//verzend een bericht naar de server
			this.sendMessage(clientMessage, clientSocket);
			
			//this.activity.setReceivedServerMessage( "We hebben het bericht verzonden" );
			//this.activity.runOnUiThread(this.activity);
			
			waitForResponse(serverSocket);
			
		}
		
		catch(UnknownHostException e){
			Log.d("debug", "can't find host");
		}
		
		catch(SocketTimeoutException e){
			Log.d("debug", "time-out");
		}
		
		catch (IOException e){
			e.printStackTrace();
		}
		
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
	}
	

	//ook deze methoden kunnen niet naar de UI direct communiceren, hou hier rekening mee
	private void sendMessage( String message, Socket serverSocket )
	{
		OutputStreamWriter outputStreamWriter = null;
		
		try{
			outputStreamWriter = new OutputStreamWriter(serverSocket.getOutputStream());
		}
		
		catch (IOException e2){
			e2.printStackTrace();
		}
		
		if(outputStreamWriter != null){
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			PrintWriter writer = new PrintWriter( bufferedWriter, true );
			
			//writer.print(clientMessage);
			writer.println(message);
			writer.close();
		}
	}
	
	
	//wacht op server bericht (na versturen)
	private void waitForResponse(ServerSocket serverSocket)
	{
		/*
		//String serverMessage = "";
		while(true){
			//Thread.sleep(100);
			
			try{
				clientSocket = serverSocket.accept();   //accept the client connection
		        InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
		        BufferedReader bufferedReader = new BufferedReader(inputStreamReader); //get the client message
		        serverMessage = bufferedReader.readLine();
		        //System.out.println(serverMessage);
		        inputStreamReader.close();
		        clientSocket.close();
			}
			
			catch(IOException e){
				
			}
		}
		
		//Socket.getInputStream()
		//- new InputStreamReader( inputStream )
		//- new BufferedReader( inputStreamReader )
		//- String messageLine = bufferedReader.readln()
		
		
		/*
		client = new Socket("10.0.2.2", 4444);  //connect to server
	    PrintWriter printwriter = new PrintWriter(clientSocket.getOutputStream(),true);
	    printwriter.write(clientMessage);  //write the message to output stream
	 
	     printwriter.flush();
	     printwriter.close();
	     client.close();   //closing the connection
	 
	    } catch (UnknownHostException e) {
	     e.printStackTrace();
	    } catch (IOException e) {
	     e.printStackTrace();
	    }
		*/
		
		//return serverMessage;
	}

}
