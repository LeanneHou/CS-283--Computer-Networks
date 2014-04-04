package com.example.tictactoeclient;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	Game newGame;
	boolean isBound = false;
	MyBoundService myService;
	int ourNum; // the random number we generated to determine who goes first
	String ourSymbol = "X"; // O or X
	String opponentsSymbol = "O"; // O or X
	
	TextView text;
	Button button1;
	Button button2;
	Button button3;
	Button button4;
	Button button5;
	Button button6;
	Button button7;
	Button button8;
	Button button9;
	Button start;

	
	Handler myHandler = new MyHandler(this);
	Messenger myMessenger = new Messenger(myHandler);
	
	public static final int OPP_FOUND = 0;
	public static final int GOT_RAN_NUM = 1;
	public static final int GOT_ROW_COL = 2;
	public static final int CONNECTED = 3;
	
	private static class MyHandler extends Handler {
		WeakReference<MainActivity> wr;
		
		MyHandler(MainActivity activity){
			wr = new WeakReference<MainActivity> (activity);
		}
		
		@Override
		public void handleMessage (Message msg){
			MainActivity activity = wr.get();
			if (activity != null) {
				switch(msg.what) {
				case CONNECTED:
					activity.connected();
				case OPP_FOUND: 
					activity.opponentFound(msg.arg1);
					break;
				case GOT_RAN_NUM:
					activity.goFirst(msg.arg1);
					break;
				case GOT_ROW_COL:
					// put down the opponent's move
					activity.opponentMove(msg.arg1, msg.arg2);
					break;
				}
			}
		}
	}
	
	public void connected(){
		Log.d("MainActivity","connected");

		text.setText("Connected to the server");
	}
	
	public void opponentFound(int num){
		Log.d("MainActivity","opponentFound");
		
		text.setText("Found opponent");
		ourNum = num;
	}
	
	public void goFirst(int num){
		Log.d("MainActivity","got First ( " + num);

		if (ourNum > num) {
			ourSymbol = "X";
			opponentsSymbol = "O";
			text.setText("You go first");
		} else {
			ourSymbol = "O";
			opponentsSymbol = "X";
			text.setText("Your opponent goes first");
		}
	}
	
	public void opponentMove(int row, int col){
		Log.d("MainActivity","opponentMove ("+row+","+col+")");

		makeMove(row, col, opponentsSymbol);
	}
	
	public void makeMove(int row, int col, String str){
		Log.d("MainActivity","make move ("+row+","+col+")"+str);
		int input;
		
		if (str == "X"){
			input = -1;
		} else { // "O" = 1;
			input = 1;
		}
		
		if (row == 0){
			if (col == 0){
				button1.setText(str);
			} else if (col == 1) {
				button2.setText(str);
			} else {
				button3.setText(str);
			}
		} else if (row == 1){
			if (col == 0){
				button4.setText(str);
			} else if (col == 1) {
				button5.setText(str);
			} else {
				button6.setText(str);
			}
		} else { // row == 2
			if (col == 0){
				button7.setText(str);
			} else if (col == 1) {
				button8.setText(str);
			} else {
				button9.setText(str);
			}
		}

		newGame.put(row, col, input);
		if (newGame.ifWin(row, col)){
			if (str == ourSymbol){
				text.setText("You Win!");
			} else { // str == opponentsSymbol
				text.setText("You Lost :(");

			}
		} else if (newGame.ifTie()) {
			text.setText("Tie!");
		}
		
		// make service send the message about the move to the server
		if (str == ourSymbol){ // if we are making a move
			Log.d("Main Activity", "makeMove: send move(" + row+","+col + ") to service");
			myService.move(row,col); // tell the service
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout);
		
		text = (TextView) findViewById(R.id.textView1);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button5 = (Button) findViewById(R.id.button5);
		button6 = (Button) findViewById(R.id.button6);
		button7 = (Button) findViewById(R.id.button7);
		button8 = (Button) findViewById(R.id.button8);
		button9 = (Button) findViewById(R.id.button9);
		start = (Button) findViewById(R.id.button10);
		
		button1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button1 clicked");			
				makeMove(0, 0, ourSymbol);
			}	
		});
		
		
		button2.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button2 clicked");
				makeMove(0, 1, ourSymbol);
			}	
		});
		
		button3.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button3 clicked");
				makeMove(0, 2, ourSymbol);
			}	
		});
		
		button4.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button4 clicked");
				makeMove(1, 0, ourSymbol);
			}	
		});
		
		button5.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button5 clicked");
				makeMove(1, 1, ourSymbol);
			}	
		});
		
		button6.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button6 clicked");
				makeMove(1, 2, ourSymbol);
			}	
		});
		
		button7.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button7 clicked");
				makeMove(2, 0, ourSymbol);
			}	
		});
		
		button8.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button8 clicked");
				makeMove(2, 1, ourSymbol);
			}	
		});

		button9.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Log.d("MainActivity","button9 clicked");
				makeMove(2, 2, ourSymbol);
			}	
		});
		
		start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","start button clicked");
				newGame = new Game();
				
				myService.start();
			}	
		});	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MyBoundService.class);
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		Log.e("MainActivity","onStart");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (isBound) {
			unbindService(serviceConnection);
		}
	}
	
	
	private ServiceConnection serviceConnection = new ServiceConnection () {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			Log.e("MainActivity","onServiceConnected");
			MyBoundService.MyBinder binder = (MyBoundService.MyBinder) arg1;
			myService = binder.getService();
			myService.passMessenger(myMessenger);
			isBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			isBound = false;
		}
		
	};

}
