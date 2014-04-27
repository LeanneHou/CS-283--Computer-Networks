package com.example.cs283final;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class MyBoundService extends Service{

	private IBinder myBinder = new MyBinder();
	Messenger myMessenger;
	int clientID;

	//String serverAddress = "127.0.0.1";
	String serverAddress = "54.187.122.58";
	
	int serverPort = 20000;
	public static final int MAX_PACKET_SIZE = 512;
	public static final int RANDOM_NUM_RANGE = 1000;
	
	static DatagramSocket socket = null;
	InetSocketAddress serverSocketAddress = new InetSocketAddress(
			serverAddress, serverPort);

	static final Map<String, Timer> ackToTimer = Collections
			.synchronizedMap(new HashMap<String, Timer>());	
	static List<WorkerThread> threadList = new ArrayList<WorkerThread>();
	
	public class WorkerThread extends Thread {
		private DatagramPacket rxPacket;
		private DatagramSocket socket;
		int ack;
		
		public WorkerThread(DatagramPacket packet, DatagramSocket socket) {
			this.rxPacket = packet;
			this.socket = socket;
		}
		
		@Override
		public void run(){
			// convert the rxPacket's payload to a string
			String payload = new String(rxPacket.getData(), 0, rxPacket.getLength())
							.trim();
			Log.e("MyBoundService", "run: Got Payload: " + payload); 
			
			String[] parts = payload.split(",");
			
			if (parts[0].startsWith("ack_id=")) {
				ack = Integer.parseInt(parts[0].split("=")[1]);
				Log.e("MyBoundService", "Ack received: " + ack); 
				
				if (parts[1].startsWith("id=")) {
					onConnect(ack, parts[1]);
					return;
				} else if (parts[1].startsWith("opponent_found")){
					// found an opponent
					Log.d("MyBoundService", parts[1]); 
					
					onOpponentFound(ack);
					return;
				} else if (parts[1].startsWith("quit")){
					
					// got a message of the opponent's random num
					Log.d("MyBoundService", "Random # Received: " + parts[1]); 

					onQuit(ack);
					return;
				} else {
					// got a message from the opponent
					Log.d("MyBoundService", "Message Received: " + parts[1]); 
					
					onMsg(ack, parts[1]);
					return;
				}
			} else if (parts[0].startsWith("received=")){
				// got ack of server receiving a message sent from the client
				// cancel the Timer
				String ack = parts[0].split("=")[1];
				
				if (ackToTimer.containsKey(ack)){
					ackToTimer.get(ack).cancel();
					ackToTimer.remove(ack);
				}
				
			}	
		}
		
		public void send(String payload, InetAddress address, int port){
			Log.d("MyBoundService", "send: "+payload); 
			
			DatagramPacket txPacket = new DatagramPacket(payload.getBytes(),
					payload.length(), address, port);
			try {
				socket.send(txPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void onConnect(int ack, String payload) {	
			Log.e("MyBoundService", "onConnect"); 

			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			
			String temp= payload.split("=")[1]; // get client ID
			clientID = Integer.parseInt(temp);
			
			Log.e("MyBoundService", "onConnect: ClientID received: " + clientID); 
		}
		
		private void onOpponentFound(int ack) {
			Log.e("MyBoundService", "onOpponentFound"); 
			
			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			
			// send notification
			showNotification("Found Match", "We found you a match!");
		}
		
		private void onQuit(int ack) {
			Log.d("MyBoundService", "onRandomNum"); 
			
			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			
			// send notification
			showNotification("Quit Conversation", "The converstaion stopped");
		}
		
		private void onMsg(int ack, String message) {
			Log.d("MyBoundService", "On Msg"); 
			
			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
						
			// send the message to the main activity
			Message m = Message.obtain();
			m.what = MainActivity.GOT_MSG;
			m.obj = message;
			try {
				myMessenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		private void showNotification(String title, String text) {
			NotificationCompat.Builder mBuilder =
				    new NotificationCompat.Builder(MyBoundService.this)
				    .setSmallIcon(R.drawable.ic_launcher)
				    .setContentTitle(title)
				    .setContentText(text);
			
			Intent resultIntent = new Intent(MyBoundService.this, MainActivity.class);
			// Because clicking the notification opens a new ("special") activity, there's
			// no need to create an artificial back stack.
			PendingIntent resultPendingIntent =
			    PendingIntent.getActivity(
			    MyBoundService.this,
			    0,
			    resultIntent,
			    PendingIntent.FLAG_UPDATE_CURRENT
			);
			
			mBuilder.setContentIntent(resultPendingIntent);
			
			
			// Sets an ID for the notification
			int mNotificationId = 001;
			// Gets an instance of the NotificationManager service
			NotificationManager mNotifyMgr = 
			        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			// Builds the notification and issues it.
			mNotifyMgr.notify(mNotificationId, mBuilder.build());
		}
	}

	
	public class MyBinder extends Binder {
		public MyBoundService getService (){
			Log.i("MyBoundService", "getService"); 
			return MyBoundService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("MyBoundService", "onBind"); 
		return myBinder;
	}
	
	public void start(String str1, String str2) {
		
		final String myGender = str1;
		final String preferredGender = str2;
		
		Thread t = new Thread() { 
	        public void run() {
	            try {		
					Log.e("MyBoundService", "start"); 
					socket = new DatagramSocket();
					connect(myGender, preferredGender, socket);
					
					while (true) {
						byte[] buf = new byte[MAX_PACKET_SIZE];
						DatagramPacket packet = new DatagramPacket(buf, buf.length);
						
						socket.receive(packet);
						
						WorkerThread t = new WorkerThread(packet, socket);
						t.start();
						
						threadList.add(t);
					}
				} catch (IOException e) {
					// we jump out here if there's an error, or if the worker thread (or
					// someone else) closed the socket
					for (WorkerThread s: threadList) {
							try {
								s.join();
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
					}
					
					e.printStackTrace();
				} finally {
					if (socket != null && !socket.isClosed())
						socket.close();
				}
	        }
	    };
	    t.start();
	    
	}
	
	public void connect(final String str1, final String str2, final DatagramSocket socket) {
		Log.e("MyBoundService", "connect"); 
//
//		final String myGender = str1;
//		final String preferredGender = str2;
//		Log.e("MyBoundService", "gender: "+myGender+" prefer: "+ preferredGender);
		
		Thread t = new Thread() { 
	        public void run() {
	            try {
	            	String command = "connect," + str1 +
	            			"," + str2;
	            	Log.e("MyBoundService","command: "+command);
	    			DatagramPacket txPacket = new DatagramPacket(command.getBytes(),
	    					command.length(), serverSocketAddress);
	    			socket.send(txPacket);
	    			
	            } catch (IOException e) {
					e.printStackTrace();
				}
	        }
	    };
	    t.start();
	}
	
	public void sendMessage(String msg){
		try {
			sendMsg(msg, serverSocketAddress.getAddress(), serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void quit(){
		try {
			sendMsg("quit", serverSocketAddress.getAddress(), serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	// wrap a string in a UDP packet and send it
	public void sendMsg(String str1, InetAddress address1, int port1)
			throws IOException {
		final String str = str1;
		//final InetAddress address = address1;
		//final int port = port1;
		
		Thread t = new Thread() { 
	        public void run() {
	            int ackNum = (int) (Math.random() * MyBoundService.RANDOM_NUM_RANGE );
				String ack = "" + ackNum;
				final String payload = "ack_id=" + ack + "," 
							+ "client_id=" + clientID + ","+str;
				Log.d("MyBoundService", "Send Message: " + payload); 
				
				Timer timer = new Timer();
				MyBoundService.ackToTimer.put(ack, timer);

				Log.d("MyBoundService", "created new timer");
				timer.scheduleAtFixedRate(new TimerTask() {
					  @Override
					  public void run() {
							try {
								DatagramPacket txPacket = new DatagramPacket(payload.getBytes(),
										payload.length(), serverSocketAddress);
								socket.send(txPacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
					  }
					}, 0, (long) ((0.2)*60*1000)); ///TEST 3*60*1000);
	        }
	    };
	    t.start();	
	}
	
	public void passMessenger(Messenger temp){
		myMessenger = temp;
	}
}
