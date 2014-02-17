CS 283 HW1, Liyan Hou

The benchmark client creates many threads once it runs. Each thread creates 100 strings in lower case, and then receives 100 strings in upper case from the server.
Outputs of the benchmark client testing•the single threaded TCP server implementation
	7 milliseconds (1000 strings)•the multithreaded TCP server implementation
	0 milliseconds (1000 strings)
Reasoning why the multithreaded server performs betterBecause in a single threaded server, it has to work on the clients’ input one after another. But in a multithreaded server, it generates threads to work on the clients’ input simultaneously, so it’s faster.