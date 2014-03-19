package udpgroupchat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

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

		// dispatch request handler functions based on the payload's prefix

		if (payload.startsWith("REGISTER")) {
			onRegisterRequested(payload);
			return;
		}

		if (payload.startsWith("UNREGISTER")) {
			onUnregisterRequested(payload);
			return;
		}

		//
		// implement other request handlers here...
		//
		
		if (payload.startsWith("NAME")) {
			onNameRequested(payload);
			return;
		}
		
		if (payload.startsWith("JOIN")) {
			onJoinRequested(payload);
			return;
		}
		
		if (payload.startsWith("MSG")) {
			onMsgRequested(payload);
			return;
		}
		
		if (payload.startsWith("POLL")) {
			onPollRequested(payload);
			return;
		}

		if (payload.startsWith("SHUTDOWN")) {
			if (this.socket.getInetAddress().getHostAddress().equals("127.0.0.1")){
				Server.socket.close();
			}
		}
		
		if (payload.startsWith("ACK")) {
			onAckRequested(payload);
			return;
		}
		
		// if we got here, it must have been a bad request, so we tell the
		// client about it
		onBadRequest(payload);
	}

	// send a string, wrapped in a UDP packet, to the specified remote endpoint
	public void send(String payload, InetAddress address, int port)
			throws IOException {

		int ackNum = (int) (Math.random() * Server.RANDOM_NUM_RANGE );
		final String ack = "ACK" + ackNum;
		payload = ack + " " + payload;
		//System.out.println("send: "+payload); //TEST!!!!
		
		final DatagramPacket txPacket = new DatagramPacket(payload.getBytes(),
				payload.length(), address, port);
		//this.socket.send(txPacket); // TEST!!!!!!!
		
		final Timer timer = new Timer();
		Server.ackToTimer.put(ack, timer);

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
			}, 0, (long) ((0.2)*60*1000)); ///TEST 3*60*1000);
	}

	private void onAckRequested(String payload) {
		if (Server.ackToTimer.containsKey(payload)) {
			Server.ackToTimer.get(payload).cancel();
		}
	}

	
	private void onRegisterRequested(String payload) {
		// get the address of the sender from the rxPacket
		InetAddress address = this.rxPacket.getAddress();
		// get the port of the sender from the rxPacket
		int port = this.rxPacket.getPort();

		// create a client object, and put it in the map that assigns names
		// to client objects
		ClientEndPoint tempClientEndPoint = new ClientEndPoint(address, port);
		Server.clientEndPoints.add(tempClientEndPoint);
		
		// create an id that does not already exist
		int id;
		do {
			id = (int) (Math.random() * Server.RANDOM_NUM_RANGE );
		} while (Server.clientIDtoEndpoint.containsKey(id));
		
		// map the id to the client object 
		Server.clientIDtoEndpoint.put(id, tempClientEndPoint);
		
		// note that calling clientEndPoints.add() with the same endpoint info
		// (address and port)
		// multiple times will not add multiple instances of ClientEndPoint to
		// the set, because ClientEndPoint.hashCode() is overridden. See
		// http://docs.oracle.com/javase/7/docs/api/java/util/Set.html for
		// details.

		// tell client we're OK, and send back to the client its assigned ID
		try {
			send("REGISTERED "+id+"\n", address, port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onUnregisterRequested(String payload) {
		ClientEndPoint clientEndPoint = new ClientEndPoint(
				this.rxPacket.getAddress(), this.rxPacket.getPort());

		// get the ID of the client
	    String rest = payload.substring("UNREGISTER".length() + 1,
					payload.length()).trim();
	    StringTokenizer st = new StringTokenizer(rest);
		int clientID = Integer.parseInt(st.nextToken());
		
		// check if client is in the set of registered clientEndPoints
		if (Server.clientIDtoEndpoint.containsKey(clientID)) {
			// yes, remove it
			Server.clientEndPoints.remove(clientEndPoint);
			
			// remove all association of the client ID
			
			Server.clientIDtoEndpoint.remove(clientID);
			Server.clientIDtoName.remove(clientID);
			Server.clientIDtoMsgQ.remove(clientID);
			for (List<Integer> list: Server.groupNameToList.values()){
				if (list.contains(clientID)){
					list.remove((Integer)clientID);
				}
			}
			
			try {
				send("UNREGISTERED\n", this.rxPacket.getAddress(),
						this.rxPacket.getPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// no, send back a message
			try {
				send("CLIENT NOT REGISTERED\n", this.rxPacket.getAddress(),
						this.rxPacket.getPort());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void onNameRequested(String payload) {
		// get the ID of the client
	    String rest = payload.substring("NAME".length() + 1,
					payload.length()).trim();
	    StringTokenizer st = new StringTokenizer(rest);
	    String temp = st.nextToken();
		int clientID = Integer.parseInt(temp);
		
		// get the name
		String name = rest.substring(temp.length() + 1,
				rest.length()).trim();
		
		// map the ID to the name
		Server.clientIDtoName.put(clientID, name);

		// tell client we're OK
		try {
			send("+SUCCESS: Hi " + name + "!\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void onJoinRequested(String payload) {
		// get the ID of the client
	    String rest = payload.substring("JOIN".length() + 1,
					payload.length()).trim();
	    StringTokenizer st = new StringTokenizer(rest);
	    String temp = st.nextToken();
		int clientID = Integer.parseInt(temp);
		
		// get the group name
		String group = rest.substring(temp.length()+1, rest.length()).trim();
		
		// map the group to a list of client IDs in that group
		if (Server.groupNameToList.containsKey(group)) {
			Server.groupNameToList.get(group).add(clientID);
		} else {
			List<Integer> list = Collections.synchronizedList(new ArrayList<Integer>());
			list.add(clientID);
			Server.groupNameToList.put(group, list);
		}

		// tell client we're OK
		try {
			send("+SUCCESS: Joined group " + group + "!\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onMsgRequested(String payload) {
		// get the ID of the client
	    String rest = payload.substring("MSG".length() + 1,
					payload.length()).trim();
	    StringTokenizer st = new StringTokenizer(rest);
	    String temp = st.nextToken();
		int clientID = Integer.parseInt(temp);
		
		
		String group = st.nextToken(); // get the group name
		
		// get the message
		String message = rest.substring(temp.length()+group.length()+2, rest.length()).trim();
		
		// iterate the clients in the group
		for (int tempID: Server.groupNameToList.get(group)){
			// the message to send
			String wholeMsg = "+SUCCESS: FROM "+ Server.clientIDtoName.get(clientID) + 
							  " TO " + group + ": "+ message + "\n";
			
			if (Server.clientIDtoMsgQ.containsKey(tempID)) {
				Server.clientIDtoMsgQ.get(tempID).add(wholeMsg);
			} else {
				List<String> tempQ =  Collections
						.synchronizedList(new ArrayList<String>());
				tempQ.add(wholeMsg);
				Server.clientIDtoMsgQ.put(tempID, tempQ);
			}	  
		}	
	}
	
	private void onPollRequested(String payload) {
		// get the ID of the client
	    String rest = payload.substring("POLL".length() + 1,
					payload.length()).trim();
	    StringTokenizer st = new StringTokenizer(rest);
	    String temp = st.nextToken();
		int clientID = Integer.parseInt(temp);
	
		try {
			
		List<String> tempList = Server.clientIDtoMsgQ.get(clientID);
		if ( tempList != null) {
			if (tempList.isEmpty()) {
		    		send("+SUCCESS: NO MESSAGE\n", this.rxPacket.getAddress(), this.rxPacket.getPort());
			} else {
				// iterator of the client's message queue, List<String>
			    for (String msg: tempList){
			    	send(msg, this.rxPacket.getAddress(), this.rxPacket.getPort());
			    }
			    tempList.clear();
			}
		}
    	
		} catch (IOException e) {
    		e.printStackTrace();
    	}
	}
	

	private void onBadRequest(String payload) {
		try {
			send("BAD REQUEST\n", this.rxPacket.getAddress(),
					this.rxPacket.getPort());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
