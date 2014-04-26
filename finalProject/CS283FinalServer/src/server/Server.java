package server;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	
	// Mapping client ID to client object
	protected static final Map<Integer, ClientEndPoint> clientIDtoEndpoint = Collections
			.synchronizedMap(new HashMap<Integer, ClientEndPoint>());
	
	// Integer: clientID
	protected static final List<Integer> maleIntoFemale = Collections.synchronizedList(new LinkedList<Integer>());
	protected static final List<Integer> maleIntoMale = Collections.synchronizedList(new LinkedList<Integer>());
	protected static final List<Integer> femaleIntoFemale = Collections.synchronizedList(new LinkedList<Integer>());
	protected static final List<Integer> femaleIntoMale = Collections.synchronizedList(new LinkedList<Integer>());

//	protected static final BlockingQueue<Integer> maleIntoFemale = new LinkedBlockingQueue<Integer>();
//	protected static final BlockingQueue<Integer> femaleIntoMale = new LinkedBlockingQueue<Integer>();
//	protected static final BlockingQueue<Integer> maleIntoMale = new LinkedBlockingQueue<Integer>();
//	protected static final BlockingQueue<Integer> femaleIntoFemale = new LinkedBlockingQueue<Integer>();
	
	// pairs in chat
	protected static final Map<Integer, Integer> clientIDtoClientID = Collections
			.synchronizedMap(new HashMap<Integer, Integer>());
	protected static final Map<Integer, Integer> clientIDtoClientID2 = Collections
			.synchronizedMap(new HashMap<Integer, Integer>());
	
	// to keep track of the timer, linking each acknowledgment to 
	// the corresponding timer
	protected static final Map<Integer, Timer> ackToTimer = Collections
			.synchronizedMap(new HashMap<Integer, Timer>());
	
	
	
	
	
	// constructor
	Server(int port) {
		this.port = port;
	}

	// start up the server
	public void start() {
		try {
			socket = new DatagramSocket(port);

			// receive packets in an infinite loop
			while (true) {
				// create an empty UDP packet
				byte[] buf = new byte[Server.MAX_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				// call receive (this will populate the packet with the received
				// data, and the other endpoint's info)
				socket.receive(packet);
				// start up a worker thread to process the packet (and pass it
				// the socket, too, in case the worker thread wants to respond)
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
