package socketprg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class WorkerThread extends Thread {

	Socket clientSocket;

	public WorkerThread(Socket cs) {
		this.clientSocket = cs;
	}

	@Override
	public void run() {
		System.out.println("WORKER " + Thread.currentThread().getId()
				+ ": Worker thread starting");
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			PrintStream pw = new PrintStream(clientSocket.getOutputStream());
			
			String line;
			while ((line = r.readLine()) != null) {
				System.out.println("WORKER " + Thread.currentThread().getId()
						+ ": Received: " + line);
				
				String line2 = line.toUpperCase();
				System.out.println("WORKER " + Thread.currentThread().getId()
						+ " " + line2);
				pw.println(line2);
			}

			
			System.out.println("WORKER " + Thread.currentThread().getId()
					+ ": Client disconnected");
			pw.close();
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("WORKER " + Thread.currentThread().getId()
				+ ": Worker thread finished");
	}

}