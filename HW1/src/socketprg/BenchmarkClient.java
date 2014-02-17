package socketprg;

import java.io.IOException;
import java.net.Socket;

public class BenchmarkClient {

	
	private static final int N = 100;
	
	public static void main(String[] args){

		try {
			System.out.println("CLIENT MAIN: Client connected to the server");
			
			long time1 = System.currentTimeMillis();
			
			for (int i=0; i<N; i++) {
				Socket s = new Socket("localhost", 4444);
				new ClientThread(s).start();
			}
			long time2 = System.currentTimeMillis();
			
			System.out.println("CLIENT MAIN: Average time to serve a client request is " 
								+ (time2-time1)/(long)N + " milliseconds");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}