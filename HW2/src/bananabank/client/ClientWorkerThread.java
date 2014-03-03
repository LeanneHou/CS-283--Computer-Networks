package bananabank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class ClientWorkerThread extends Thread {

	private static boolean VERBOSE = false;
	
	@Override
	public void run() {
		System.out.println("Client worker thread (thread id="+Thread.currentThread().getId()+") started");
		try {
			Random rand = new Random();
			
			// connect to the server
			Socket socket = new Socket(BananaBankBenchmark.SERVER_ADDRESS, BananaBankBenchmark.PORT);
			System.out.println("Client worker thread (thread id="+Thread.currentThread().getId()+") connected to server");
			
			// set up input and output streams
			PrintStream ps = new PrintStream(socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			System.out.println("Client worker thread (thread id="+Thread.currentThread().getId()+") requesting " + BananaBankBenchmark.TRANSACTIONS_NUM + " transactions");
			
			// request TRANSACTIONS_NUM transactions from the server
			for (int i = 0; i < BananaBankBenchmark.TRANSACTIONS_NUM; i++) {
				// generate random source and destination account numbers
				int srcAccountNumber = BananaBankBenchmark.ACCOUNT_NUMBERS[rand.nextInt(BananaBankBenchmark.ACCOUNT_NUMBERS.length)];
				int dstAccountNumber = BananaBankBenchmark.ACCOUNT_NUMBERS[rand.nextInt(BananaBankBenchmark.ACCOUNT_NUMBERS.length)];
				while (dstAccountNumber == srcAccountNumber)
					dstAccountNumber = BananaBankBenchmark.ACCOUNT_NUMBERS[rand.nextInt(BananaBankBenchmark.ACCOUNT_NUMBERS.length)];
				
				// ask the server to transfer $1 from source to destination account
				String line = "1 " + srcAccountNumber + " " + dstAccountNumber;
				ps.println(line);
				if(VERBOSE)
					System.out.println("SENT: "+line);
				
				// read back the server's response and print it
				line = br.readLine();
				if(VERBOSE)
					System.out.println("RECEIVED: "+line);
			}
			
			// close the print stream (and the socket, implicitly)
			ps.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Client worker thread (thread id="+Thread.currentThread().getId()+") finished");
		
	}
}
