package bananabank.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class BananaBank {

	private final Map<Integer, Account> accounts;

	/**
	 * Instantiates a BananaBank and loads the account numbers and the corresponding balances from a text file.
	 * @param fileName the file name
	 * @throws IOException if the file could not be read
	 */
	public BananaBank(String fileName) throws IOException {
		HashMap<Integer, Account> accounts = new HashMap<Integer, Account>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName)));
		String line;
		while ((line = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line);
			int accountNumber = Integer.parseInt(st.nextToken());
			int balance = Integer.parseInt(st.nextToken());
			accounts.put(accountNumber, new Account(accountNumber, balance));
		}
		br.close();
		this.accounts = Collections.unmodifiableMap(accounts);
	}

	/**
	 * Saves the account numbers and the corresponding balances into a text file
	 * @param fileName the name of the file to save to
	 * @throws IOException if the file cannot be written
	 */
	public void save(String fileName) throws IOException {
		PrintStream ps = new PrintStream(new FileOutputStream(fileName));
		for (Account a : this.accounts.values()) {
			ps.println(a.getAccountNumber() + " " + a.getBalance());
		}
		ps.close();
	}

	/**
	 * Finds an Account by account number 
	 * @param accountNumber the account number
	 * @return the Account object with the specified account number, or null if not found
	 */
	public Account getAccount(int accountNumber) {
		return this.accounts.get(accountNumber);
	}

	/**
	 * Returns all accounts.
	 * @return a collection of all Account objects
	 */
	public Collection<Account> getAllAccounts() {
		return Collections.unmodifiableCollection(this.accounts.values());
	}
}
