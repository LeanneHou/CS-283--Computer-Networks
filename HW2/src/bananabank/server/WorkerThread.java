package bananabank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class WorkerThread extends Thread {

	Socket clientSocket;
	ServerSocket serverSocket;
	BananaBank bank;
	boolean ifShutDown;
	PrintStream ps;

	public WorkerThread(ServerSocket ss, Socket cs, BananaBank b) {
		this.serverSocket = ss;
		this.clientSocket = cs;
		this.bank = b;
		this.ifShutDown = false;
		try {
			this.ps = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		System.out.println("WORKER " + Thread.currentThread().getId()
				+ ": Worker thread starting");
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
			
			String line;
			while ((line = r.readLine()) != null && !ifShutDown) {
				System.out.println("WORKER " + Thread.currentThread().getId()
						+ ": Received: " + line);
				
				if (line.startsWith("SHUTDOWN")) {
					System.out.println("Received SHUTDOWN");
					System.out.println("client address: " + clientSocket.getInetAddress().getHostAddress());
						if (clientSocket.getInetAddress().getHostAddress().equals("127.0.0.1")) {
							System.out.println("Received SHUTDOWN from local");
							ifShutDown = true;	
							serverSocket.close();
						}
				} else {
					
					StringTokenizer st = new StringTokenizer(line);
					int money2transfer = Integer.parseInt(st.nextToken());
					int srcAccNum = Integer.parseInt(st.nextToken());
					int dstAccNum = Integer.parseInt(st.nextToken());

					Account srcAcc = bank.getAccount(srcAccNum);
					Account dstAcc = bank.getAccount(dstAccNum);
					
					if (srcAcc != null && dstAcc != null) {
						Account acc1, acc2; // put the accounts in order, to avoid deadlocks
						if (srcAccNum < dstAccNum){
							acc1 = srcAcc;
							acc2 = dstAcc;
						} else {
							acc1 = dstAcc;
							acc2 = srcAcc;
						}
	
						boolean isEnough = false;
						synchronized(acc1) {
							synchronized(acc2) {
								// only make transfer when there's enough money in the account
								if(srcAcc.getBalance() >= money2transfer){
									isEnough = true;
									srcAcc.transferTo(money2transfer, dstAcc);
								}
							}
						} 
					
						if (isEnough) {
							ps.println(money2transfer + " transferred from account " + srcAccNum + " to account " + dstAccNum);
						} else {
							ps.println("There's not enough money in account " + srcAccNum);
						}
					} else {
						ps.println("Invalid sourse account");
					}
				}

			}

			
			System.out.println("WORKER " + Thread.currentThread().getId()
					+ ": Client disconnected");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("WORKER " + Thread.currentThread().getId()
				+ ": Worker thread finished");
	}
	
	public boolean ifShut () {
		return ifShutDown;
	}
	
	public void writeBackTotal (int total) {
		ps.println(total);
	}

}