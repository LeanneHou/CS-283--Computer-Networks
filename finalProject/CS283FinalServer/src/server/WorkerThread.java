package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import server.ClientEndPoint;
import server.Server;

public class WorkerThread extends Thread {

	private DatagramPacket rxPacket;
	private DatagramSocket socket;
	
	public WorkerThread(DatagramPacket packet, DatagramSocket socket) {
		this.rxPacket = packet;
		this.socket = socket;
	}

	@Override
	public void run() {
		// convert the rxPacket's payload to a string
		String payload = new String(rxPacket.getData(), 0, rxPacket.getLength())
				.trim();
		int ack;
		int clientID;

		// dispatch request handler functions based on the payload's prefix

		
		System.out.println("Server WorkerThread -- "+"run: Got Payload: " + payload); 
		
		String[] parts = payload.split(",");
		
		if (parts[0].startsWith("connect")){
			onConnectRequested(parts[1],parts[2]);
		} else if (parts[0].startsWith("ack_id=")) {
			ack = Integer.parseInt(parts[0].split("=")[1]);
			System.out.println("Server WorkerThread -- "+"Ack received: " + ack); 
			
			clientID = Integer.parseInt(parts[1].split("=")[1]);
			
			// message or shutdown
			if (parts[2].startsWith("quit")) {
				onQuitRequested(ack, clientID);

				return;
			} else {
				onMessageRequested(ack, clientID, parts[2]);
			}
		} else if (parts[0].startsWith("received=")){
			// got ack of server receiving a message sent from the client
			// cancel the Timer
			ack = Integer.parseInt(parts[0].split("=")[1]);			
			onAckReceived(ack);
			
			return;	
		}	
	}

	// send a string, wrapped in a UDP packet, to the specified remote endpoint
	public void send(String payload, InetAddress address, int port)
			throws IOException {

		int ackNum = (int) (Math.random() * Server.RANDOM_NUM_RANGE );
		final String ack = "ack_id=" + ackNum;
		payload = ack + "," + payload;
		
		final DatagramPacket txPacket = new DatagramPacket(payload.getBytes(),
				payload.length(), address, port);
		
		final Timer timer = new Timer();
		Server.ackToTimer.put(ackNum, timer);

		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
					try {
						WorkerThread.this.socket.send(txPacket);
						//WorkerThread.this.socket.receive(rPacket);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
			  }
			}, 0, (long) ((0.3)*60*1000)); ///TEST 3*60*1000);
	}
	
	public void sendReceived(String payload, InetAddress address, int port){
		System.out.println("Server WorkerThread -- "+"send: "+payload); 
		
		DatagramPacket txPacket = new DatagramPacket(payload.getBytes(),
				payload.length(), address, port);
		try {
			socket.send(txPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onAckReceived(int ack) {
		if (Server.ackToTimer.containsKey(ack)){
			Server.ackToTimer.get(ack).cancel();
			Server.ackToTimer.remove(ack);
		}
	}

	private void onConnectRequested(String gender, String into) {	
		System.out.println("Server WorkerThread -- "+"onConnect"); 
		
		// get the address of the sender from the rxPacket
		InetAddress address = this.rxPacket.getAddress();
		// get the port of the sender from the rxPacket
		int port = this.rxPacket.getPort();

		
		// create a client object, and put it in the map that assigns names
		// to client objects
		ClientEndPoint tempClientEndPoint = new ClientEndPoint(address, port);
		
		// create an id that does not already exist
		int id;
		do {
			id = (int) (Math.random() * Server.RANDOM_NUM_RANGE );
		} while (Server.clientIDtoEndpoint.containsKey(id));
		
		// map the id to the client object 
		Server.clientIDtoEndpoint.put(id, tempClientEndPoint);
		

		// send the id back to the client
		try {
			send("id="+id+"\n", address, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		// check if there's a match, if so, match them
		// if not add the client to appropriate waiting group
		if (gender.equals("F")){
			if (into.equals("M")){ 	// Female into male
				if (Server.maleIntoFemale.isEmpty()){
					// no match now
					Server.femaleIntoMale.add(id);
				} else {
					// delete one from the matching waiting queues
					int idMatched = Server.maleIntoFemale.get(0);
					Server.maleIntoFemale.remove(0);
					match(id, idMatched);
				}
			} else { 				// Female into female				
				if (Server.femaleIntoFemale.isEmpty()){
					// no match now
					Server.femaleIntoMale.add(id);
				} else {
					// delete one from the matching waiting queues
					int idMatched = Server.femaleIntoFemale.get(0);
					Server.femaleIntoFemale.remove(0);
					match(id, idMatched);
				}
			}
		} else {
			if (into.equals("M")){	// Male into male				
				if (Server.maleIntoMale.isEmpty()){
					// no match now
					Server.maleIntoMale.add(id);
				} else {
					// delete one from the matching waiting queues
					int idMatched = Server.maleIntoMale.get(0);
					Server.maleIntoMale.remove(0);
					match(id, idMatched);
				}
			} else {				// Male into female
				Server.maleIntoFemale.add(id);
				
				if (Server.femaleIntoMale.isEmpty()){
					// no match now
					Server.maleIntoFemale.add(id);
				} else {
					// delete one from the matching waiting queues
					int idMatched = Server.femaleIntoMale.get(0);
					Server.femaleIntoMale.remove(0);
					match(id, idMatched);
				}
			}
		}
	}
	
	private void match(int client1, int client2){
		Server.clientIDtoClientID.put(client1, client2);
		Server.clientIDtoClientID2.put(client2, client1);
		
		try {
			send("opponent_found", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
			send("opponent_found", 
					Server.clientIDtoEndpoint.get(client2).address,
					Server.clientIDtoEndpoint.get(client2).port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void onQuitRequested(int ack, int clientID){
		System.out.println("Server WorkerThread -- "+"onQuitRequested"); 

		// make sure the client is matched
		if (Server.clientIDtoClientID.containsKey(clientID) 
			|| Server.clientIDtoClientID2.containsKey(clientID)){
			
			int temp;
			if (Server.clientIDtoClientID.containsKey(clientID)){
				temp = Server.clientIDtoClientID.get(clientID);
				Server.clientIDtoClientID.remove(clientID);
				Server.clientIDtoClientID2.remove(temp);
			} else { // don't need to check coz we checked it before
				temp = Server.clientIDtoClientID2.get(clientID);
				Server.clientIDtoClientID2.remove(clientID);
				Server.clientIDtoClientID.remove(temp);
			}
			
			// send quit message to both clients
			final int tempID = temp;
			try {
				send("quit", this.rxPacket.getAddress(),
						this.rxPacket.getPort());
				send("quit", 
						Server.clientIDtoEndpoint.get(tempID).address,
						Server.clientIDtoEndpoint.get(tempID).port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Server.clientIDtoEndpoint.remove(clientID);
			Server.clientIDtoEndpoint.remove(temp);
		}
		
		sendReceived("received="+ ack +"\n", this.rxPacket.getAddress(),
				this.rxPacket.getPort());
	}

	private void onMessageRequested(int ack, int clientID, String msg){
		System.out.println("Server WorkerThread -- "+"onMessageRequested"); 

		// in case of client IP change
		if ((Server.clientIDtoEndpoint.get(clientID).address != 
				this.rxPacket.getAddress()) 
			|| (Server.clientIDtoEndpoint.get(clientID).port != 
					this.rxPacket.getPort())) {
			
			// update the IP change;
			ClientEndPoint tempClientEndPoint = new ClientEndPoint
					(this.rxPacket.getAddress(), this.rxPacket.getPort());

			Server.clientIDtoEndpoint.put(clientID, tempClientEndPoint);
		}
		
		// send back ack
		sendReceived("received="+ ack +"\n", this.rxPacket.getAddress(),
				this.rxPacket.getPort());
		
		// make sure the client is matched
		if (Server.clientIDtoClientID.containsKey(clientID) 
			|| Server.clientIDtoClientID2.containsKey(clientID)){
			
			// find the opponent
			int temp;
			if (Server.clientIDtoClientID.containsKey(clientID)){
				temp = Server.clientIDtoClientID.get(clientID);
			} else {
				temp = Server.clientIDtoClientID2.get(clientID);
			}
			
			// send message to the other clients
			final int tempID = temp;
			try {
				send(msg, 
					Server.clientIDtoEndpoint.get(tempID).address,
					Server.clientIDtoEndpoint.get(tempID).port);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}

}
