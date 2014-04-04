package com.example.tictactoeclient;

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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MyBoundService extends Service{

	private IBinder myBinder = new MyBinder();
	Messenger myMessenger;
	int clientID;

	String serverAddress = "54.186.61.49";
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
		MyBoundService myService;
		private DatagramPacket rxPacket;
		private DatagramSocket socket;
		int ack;
		
		public WorkerThread(DatagramPacket packet, DatagramSocket socket) {
			// myService = s;
			this.rxPacket = packet;
			this.socket = socket;
		}
		
		@Override
		public void run(){
			// convert the rxPacket's payload to a string
			String payload = new String(rxPacket.getData(), 0, rxPacket.getLength())
							.trim();
			Log.d("MyBoundService", "run: Got Payload: " + payload); 
			
			String[] parts = payload.split(",");
			
			if (parts[0].startsWith("ack_id=")) {
				ack = Integer.parseInt(parts[0].split("=")[1]);
				Log.d("MyBoundService", "Ack received: " + ack); 
				
				if (parts[1].startsWith("id=")) {
					
					onConnect(ack, parts[1]);
					return;
				} else if (parts[1].startsWith("opponent_found")){
					// found an opponent
					Log.d("MyBoundService", parts[1]); 
					
					onOpponentFound(ack);
					return;
				} else if (parts[1].startsWith("random_num")){
					
					// got a message of the opponent's random num
					Log.d("MyBoundService", "Random # Received: " + parts[1]); 

					onRandomNum(ack, parts[1]);
					return;
				} else {
					
					// got a message from the opponent
					Log.d("MyBoundService", "Message Received: " + parts[1]); 
					
					onMsg(ack, parts[1], parts[2]);
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
			Log.d("MyBoundService", "onConnect"); 

			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			
			String temp= payload.split("=")[1]; // get client ID
			clientID = Integer.parseInt(temp);
			
			Log.d("MyBoundService", "onConnect: ClientID received: " + clientID); 
			
			// tell the main activity that it's connected
			Message m = Message.obtain();
			m.what = MainActivity.CONNECTED;
			try {
				myMessenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		private void onOpponentFound(int ack) {
			Log.d("MyBoundService", "onOpponentFound"); 
			
			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			
			// tell the main activity that an opponent is found
			Message m = Message.obtain();
			m.what = MainActivity.OPP_FOUND;
			
			int randomNum = (int) (Math.random() * MyBoundService.RANDOM_NUM_RANGE );
			m.arg1 = randomNum;
			try {
				myMessenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			
			// send the random number message to the server
			try {
				sendMsg("random_num=" + randomNum, this.rxPacket.getAddress(),
						this.rxPacket.getPort());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void onRandomNum(int ack, String msg) {
			Log.d("MyBoundService", "onRandomNum"); 

			
			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			
			// parse the strings to get the random number from the opponent
			String temp = msg.split("=")[1];
			int randomNum = Integer.parseInt(temp);

			
			// send the message to the main activity
			Message m = Message.obtain();
			m.what = MainActivity.GOT_RAN_NUM;
			m.arg1 = randomNum;
			try {
				myMessenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		private void onMsg(int ack, String msg1, String msg2) {
			Log.d("MyBoundService", "On Msg"); 
			
			send("received="+ ack +"\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			
			// parse the strings to get row and col number
			String temp = msg1.split("=")[1];
			int row = Integer.parseInt(temp);
			temp = msg2.split("=")[1];
			int col = Integer.parseInt(temp);
			
			// send the message to the main activity
			Message m = Message.obtain();
			m.what = MainActivity.GOT_ROW_COL;
			m.arg1 = row;
			m.arg2 = col;
			try {
				myMessenger.send(m);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
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
	
	public void start() {
		
		Thread t = new Thread() { 
	        public void run() {
	            try {		
					Log.d("MyBoundService", "start"); 
					socket = new DatagramSocket();
					connect(socket);
					
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
	
	private void connect(final DatagramSocket socket) {
		Log.d("MyBoundService", "connect"); 

		Thread t = new Thread() { 
	        public void run() {
	            try {
	            	String command = "connect";
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
	
	
	public void move(int row, int col) {
		// send the message to the server
		String payload = "client_id=" + clientID + ",row=" + row + ",col=" + col;
		Log.d("MyBoundService", "Send move: " + payload); 
		
		try {
			sendMsg(payload, serverSocketAddress.getAddress(), serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	// wrap a string in a UDP packt and send it
	public void sendMsg(String str1, InetAddress address1, int port1)
			throws IOException {
		final String str = str1;
		//final InetAddress address = address1;
		//final int port = port1;
		
		Thread t = new Thread() { 
	        public void run() {
	            int ackNum = (int) (Math.random() * MyBoundService.RANDOM_NUM_RANGE );
				String ack = "" + ackNum;
				final String payload = "ack_id=" + ack + "," + str;
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
