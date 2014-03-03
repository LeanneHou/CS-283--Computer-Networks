package bananabank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ShutdownWorkerThread extends Thread {

	@Override
	public void run() {
		try {
			Socket socket = new Socket(BananaBankBenchmark.SERVER_ADDRESS, BananaBankBenchmark.PORT);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			ps.println("SHUTDOWN");
			String line = br.readLine();
			int total = Integer.parseInt(line);
			System.out.println("Total amount of money in bank:" + total);
			br.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
