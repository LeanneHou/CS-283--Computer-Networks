package bananabank.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//import bananabank.client.BananaBankBenchmark;

public class MTServer {
	
	private static final int PORT = 4444;
//	public Socket shutDownSocket;

	public static void main(String[] args){
		BananaBank bank = null;
		List<WorkerThread> threadList = new ArrayList<WorkerThread>();

		try {
			ServerSocket ss = new ServerSocket(PORT);
			System.out.println("MAIN: ServerSocket created");
			bank = new BananaBank("accounts.txt");
			
			for(;;) {
				System.out.println("MAIN: Waiting for client connection on port " + PORT);				
				Socket cs = ss.accept();
				System.out.println("MAIN: Client connected");
				WorkerThread wt = new WorkerThread(ss, cs, bank);
				wt.start();
				threadList.add(wt);
			}
			
		} catch (IOException e) {
			try {
				WorkerThread threadGotRequest = null;
				for (WorkerThread s: threadList) {
					if (!s.ifShut())
					{
						s.join();
					} else {
						threadGotRequest = s;
						System.out.println("found the thread that got SHUTDOWN request");
					}
				}
				
				if (bank != null && threadGotRequest != null) {
					System.out.println("starting to save to bank");
					bank.save("accounts.txt");
				
					int sum = 0;
					Collection<Account> allAccounts = bank.getAllAccounts();
					Iterator<Account> itr = allAccounts.iterator();
					while (itr.hasNext()) {
						sum += itr.next().getBalance();
					}
					System.out.println("SERVER: the sum is " + sum);
					
					threadGotRequest.writeBackTotal(sum);
					
					threadGotRequest.join();
				}
				
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			} catch (IOException e3) {
				e3.printStackTrace();
			}
				
			e.printStackTrace();
		}
	}
}