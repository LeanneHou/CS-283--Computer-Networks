package bananabank.client;

public class BananaBankBenchmark extends Thread {

	static final String SERVER_ADDRESS = "localhost";
	public static final int PORT = 4444; // 2000;
	public static final int TRANSACTIONS_NUM = 10; // 100;
	public static final int WORKER_THREAD_NUM = 8;
	public static final int ACCOUNT_NUMBERS[] = new int[] { 11111, 22222,
			33333, 44444, 55555, 66666, 77777, 88888 };

	public static void main(String[] args) throws InterruptedException {

		long startTime;
		long endTime;

		// Test case #1:
		// start up WORKER_THREAD_NUM worker threads (each of which will request
		// TRANSACTION_NUM transactions from the server)

		System.out.println("Benchmarking server performance with "
				+ BananaBankBenchmark.WORKER_THREAD_NUM
				+ " concurrent client connections...");

		ClientWorkerThread workers[] = new ClientWorkerThread[BananaBankBenchmark.WORKER_THREAD_NUM];

		startTime = System.currentTimeMillis();
		for (int i = 0; i < BananaBankBenchmark.WORKER_THREAD_NUM; i++) {
			workers[i] = new ClientWorkerThread();
			workers[i].start();
		}

		// wait until the worker threads finish
		for (int i = 0; i < BananaBankBenchmark.WORKER_THREAD_NUM; i++) {
			workers[i].join();
		}

		endTime = System.currentTimeMillis();
		System.out.println(WORKER_THREAD_NUM
				+ " concurrent clients finished in " + (endTime - startTime)
				+ " milliseconds");

		System.out.println(1.0*(WORKER_THREAD_NUM*TRANSACTIONS_NUM)/(endTime - startTime)*1000.0 + " requests per second served");

		// Test case #2:
		// Start up just one client thread (which will request
		// TRANSACTION_NUM transactions from the server)
		System.out
				.println("Benchmarking server performance with a single client connections...");

		startTime = System.currentTimeMillis();
		ClientWorkerThread worker = new ClientWorkerThread();
		worker.start();
		worker.join();
		endTime = System.currentTimeMillis();
		System.out.println("A single client finished in "
				+ (endTime - startTime) + " milliseconds");
		System.out.println(1.0*TRANSACTIONS_NUM/(endTime - startTime)*1000.0 + " requests per second served");

		
		// start up a thread that sends "SHUTDOWN" to the server
		new ShutdownWorkerThread().start();

	}
}
