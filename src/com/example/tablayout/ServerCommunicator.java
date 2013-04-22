package com.example.tablayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class ServerCommunicator implements Runnable
{
	private Activity activity;
	private Thread thread;
	private String naam, vraag, ip;
	private int poort;
	private String serverBericht;
	public ServerCommunicator( Activity activity, String naam, String vraag, String ip, int poort )
	{
		//we gebruiken de activity om de userinterface te updaten
		this.activity = activity;
		//gegevens om naar de server te verbinden en een message te sturen
		this.naam = naam;
		this.vraag = vraag;
		this.ip = ip;
		this.poort = poort;
		//de nieuwe thread kan tekst verzenden en ontvangen van en naar een server
		this.setThread(new Thread(this));
		getThread().start();
	}

	//dit is een methode die niet op de UI thread wordt aangeroepen, maar door onze eigen nieuwe thread
	//we kunnen dus niet zomaar ontvangen berichten in een userinterface object stoppen m.b.v. view.setText( message )
	//hier gebruiken we de activity voor: activity.runOnUiThread( activity )
	@Override
	public void run()
	{
		try
		{
			Socket socket = new Socket();
			socket.connect( new InetSocketAddress( this.ip, this.poort ), 4000 );
			//verzend een bericht naar de server
			this.sendMessage( "Naam: " + naam + " , vraag: " + vraag + "\n", socket );
			//wacht op een antwoord.
			final String respons = waitForResponse(socket);
			Log.d("debug", "RESPONS: " + respons);
			if(!respons.equals("Your message has been received on the server!"))
			{
				activity.runOnUiThread( new Runnable() {
					public void run() {
						JSONObject json = null;
						JSONArray array = null;
						Object opdracht = null;
						try {
							json = new JSONObject( respons );
						} catch (JSONException e) {
							Log.d("debug", e.getMessage());
						}
						try {
							array = (JSONArray) json.get( "opdracht" );
						} catch (JSONException e) {
							Log.d("debug", e.getMessage());
						}
						for( int i = 0; i < array.length(); i++ )
						{
							try {
								opdracht =  array.get( i );
								//activity.setReceivedServerMessage( opdracht.toString() );
								serverBericht = opdracht.toString();
								
							} catch (JSONException e) {
								Log.d("debug", e.getMessage());
							}
						}

					}
				} );
			}
		}
		catch( UnknownHostException e )
		{
			Log.d("debug", "ServerCommunicator, can't find host");
		}
		catch( SocketTimeoutException e )
		{
			Log.d("debug", "ServerCommunicator, time-out");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	//ook deze methoden kunnen niet naar de UI direct communiceren, hou hier rekening mee
	private void sendMessage( String message, Socket socket )
	{
		DataOutputStream dataOutputStream = null;
		DataInputStream dataInputStream = null;
		try {
			dataOutputStream = new DataOutputStream(socket.getOutputStream());
			dataInputStream = new DataInputStream(socket.getInputStream());
			dataOutputStream.writeUTF(message);
			Log.d("debug", "just send to server");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//wacht op server bericht (na versturen)
	private String waitForResponse(Socket socket)
	{
		BufferedReader bufferedReader = null;
		String returnMessage = null;
		try
		{
			InputStream inputStream = socket.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader( inputStream );
			bufferedReader = new BufferedReader( inputStreamReader );
		}
		
		catch (IOException e1){
			e1.printStackTrace();
			InetAddress adress = socket.getInetAddress();
			Log.d("debug", "Can't create inputStreamReader to talk to client " + adress);
			Log.d("debug", e1.getMessage());
		}
		
		if(bufferedReader != null)
		{
			try
			{
				Log.d("debug", "start receiving...");
				String messageLine = bufferedReader.readLine();
				Log.d("debug", "Server op " + socket.getInetAddress() + " zegt: > " + messageLine);
				returnMessage = messageLine;
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		
		return returnMessage;
	}

	public Thread getThread(){
		return thread;
	}

	public void setThread(Thread thread){
		this.thread = thread;
	}

	public String getServerBericht(){
		return serverBericht;
	}

}