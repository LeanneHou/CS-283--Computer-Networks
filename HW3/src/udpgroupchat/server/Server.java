package udpgroupchat.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

public class Server {

	// constants
	public static final int DEFAULT_PORT = 20000;
	public static final int MAX_PACKET_SIZE = 512;
	public static final int RANDOM_NUM_RANGE = 100000;

	// static so a worker thread can access it
	static DatagramSocket socket = null;
	
	static List<WorkerThread> threadList = new ArrayList<WorkerThread>();


	// port number to listen on
	protected int port;

	// set of clientEndPoints
	// note that this is synchronized, i.e. safe to be read/written from
	// concurrent threads without additional locking
	protected static final Set<ClientEndPoint> clientEndPoints = Collections
			.synchronizedSet(new HashSet<ClientEndPoint>());
	
	// Mapping client ID to client object
	protected static final Map<Integer, ClientEndPoint> clientIDtoEndpoint = Collections
			.synchronizedMap(new HashMap<Integer, ClientEndPoint>());
	
	protected static final Map<Integer, String> clientIDtoName = Collections
			.synchronizedMap(new HashMap<Integer, String>());
	
	protected static final Map<Integer, List<String>> clientIDtoMsgQ = Collections
			.synchronizedMap(new HashMap<Integer, List<String>>());

	protected static final Map<String, List<Integer>> groupNameToList = Collections
			.synchronizedMap(new HashMap<String, List<Integer>>());

	protected static final Map<String, Timer> ackToTimer = Collections
			.synchronizedMap(new HashMap<String, Timer>());
	
	
	// constructor
	Server(int port) {
		this.port = port;
	}

	// start up the server
	public void start() {
		try {
			// create a datagram socket, bind to port port. See
			// http://docs.oracle.com/javase/tutorial/networking/datagrams/ for
			// details.

			socket = new DatagramSocket(port);

			// receive packets in an infinite loop
			while (true) {
				// create an empty UDP packet
				byte[] buf = new byte[Server.MAX_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				// call receive (this will poulate the packet with the received
				// data, and the other endpoint's info)
				socket.receive(packet);
				// start up a worker thread to process the packet (and pass it
				// the socket, too, in case the
				// worker thread wants to respond)
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

	// main method
	public static void main(String[] args) {
		int port = Server.DEFAULT_PORT;

		// check if port was given as a command line argument
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Invalid port specified: " + args[0]);
				System.out.println("Using default port " + port);
			}
		}

		// instantiate the server
		Server server = new Server(port);

		System.out
				.println("Starting server. Connect with netcat (nc -u localhost "
						+ port
						+ ") or start multiple instances of the client app to test the server's functionality.");

		// start it
		server.start();

	}

}
