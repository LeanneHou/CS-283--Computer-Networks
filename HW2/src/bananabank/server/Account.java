package bananabank.server;

public class Account {
	private final int accountNumber;
	private int balance;

	/**
	 * Creates a new account.
	 * @param accountNumber is the account number
	 * @param balance is the balance
	 */
	public Account(int accountNumber, int balance) {
		this.accountNumber = accountNumber;
		this.balance = balance;
	}
	
	/**
	 * Returns the account balance
	 * @return the balance
	 */
	public int getBalance() {
		try {Thread.sleep(5);} catch (InterruptedException e) {}
		return balance;
	}

	/**
	 * Sets the balance
	 * @param balance the balance
	 */
	private void setBalance(int balance) {
		try {Thread.sleep(5);} catch (InterruptedException e) {}
		this.balance = balance;
	}

	/**
	 * Returns the account number
	 * @return the account number
	 */
	public int getAccountNumber() {
		return accountNumber;
	}
	
	public void transferTo(int amount, Account dstAccount) {
		this.setBalance(this.getBalance() - amount);
		dstAccount.setBalance(dstAccount.getBalance() + amount);
	}
}
