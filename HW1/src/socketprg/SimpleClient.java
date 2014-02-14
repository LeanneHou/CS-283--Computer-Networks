package socketprg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class SimpleClient {

	public static void main(String[] args){
		Socket s = null;
		try {
			s = new Socket("localhost", 4444);
			System.out.println("Client is connected to the server");
			PrintStream pw = new PrintStream(s.getOutputStream());
			pw.println("hello world");
			
			BufferedReader r = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			String line = r.readLine();
			System.out.println("Received: " + line);
			
			r.close();
			pw.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        } finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}