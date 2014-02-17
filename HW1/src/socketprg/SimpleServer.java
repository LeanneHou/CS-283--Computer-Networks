package socketprg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {

	private static final int PORT = 3333;

	public static void main(String[] args) {
		Socket cs = null;
		try {
			ServerSocket ss = new ServerSocket(PORT);
			System.out.println("ServerSocket created");
			while (true) {
				System.out.println("Waiting for client connection on port " + PORT);
				cs = ss.accept();
				System.out.println("Client connected");
	
				BufferedReader r = new BufferedReader(new InputStreamReader(
						cs.getInputStream()));
				// String line = r.readLine();
				// System.out.println("Received: " + line);
				
				String line;
				while ((line = r.readLine()) != null) {
					System.out.println("Received: " + line);
					
					String line2 = line.toUpperCase();
					PrintStream pw = new PrintStream(cs.getOutputStream());
					pw.println(line2);
				}
				
				System.out.println("Client disconnected");
				//pw.close();
				r.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				cs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}