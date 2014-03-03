package bananabank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends Thread{

	static final String SERVER_ADDRESS = "localhost";
	public static final int PORT = 4444; // 2000;
	public static final int TRANSACTIONS_NUM = 10; // 100;
	public static final int WORKER_THREAD_NUM = 8;
	public static final int ACCOUNT_NUMBERS[] = new int[] { 11111, 22222,
			33333, 44444, 55555, 66666, 77777, 88888 };

	public static void main(String[] args) throws InterruptedException {
		// Test some edge cases;
		try {
			Socket socket = new Socket(BananaBankBenchmark.SERVER_ADDRESS, BananaBankBenchmark.PORT);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			
			// invalid account number
			ps.println("10 99999 11111");
			String line = br.readLine();
			System.out.println("RECEIVED: "+line);
			
			// attempt to transfer more money than the balance
			ps.println("1000 22222 11111");
			line = br.readLine();
			System.out.println("RECEIVED: "+line);
			
			br.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// start up a thread that sends "SHUTDOWN" to the server
		new ShutdownWorkerThread().start();

	}
}
