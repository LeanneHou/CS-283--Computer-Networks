package com.example.cs283final;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

public class MainActivity extends Activity {

	boolean isBound = false;
	MyBoundService myService;

	Handler myHandler = new MyHandler(this);
	Messenger myMessenger = new Messenger(myHandler);
	
	private ArrayAdapter<String> mAdapter; 
	private ListView mListView;
		
	String myGender;
	String preferredGender;
	
	RadioButton imMale;
	RadioButton imFemale;
	RadioButton intoMale;
	RadioButton intoFemale;
	EditText text;
	Button send;
	Button find;
	Button quit;

	public static final int GOT_MSG = 1;
	
	public static class MyHandler extends Handler {
		WeakReference<MainActivity> wr;
		
		MyHandler(MainActivity activity){
			wr = new WeakReference<MainActivity> (activity);
		}
		
		@Override
		public void handleMessage (Message msg){
			MainActivity activity = wr.get();
			if (activity != null) {
				if (msg.what == GOT_MSG){
					activity.gotMsg((String)msg.obj);
				}
			}
		}
	}
	
	public void gotMsg(String msg){
		Log.d("MainActivity","got message");
		mAdapter.add("Anonymous: "+msg);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_layout);
		
		mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		mListView = (ListView) findViewById(R.id.listView1);
		mListView.setAdapter(mAdapter);
		
		send = (Button) findViewById(R.id.button10);
		find = (Button) findViewById(R.id.button1);
		quit = (Button) findViewById(R.id.button2);
		text = (EditText) findViewById(R.id.editText1);
		imMale = (RadioButton) findViewById(R.id.radioButton1);
		imFemale = (RadioButton) findViewById(R.id.radioButton2);
		intoMale = (RadioButton) findViewById(R.id.radioButton3);
		intoFemale = (RadioButton) findViewById(R.id.radioButton4);

		imMale.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","I'm male");
				myGender = "M";
			}	
		});
		
		imFemale.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","I'm female");
				myGender = "F";
			}	
		});
		
		intoMale.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","I'm into male");
				preferredGender = "M";
			}	
		});
		
		intoFemale.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","I'm into female");
				preferredGender = "F";
			}	
		});
		
		send.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","send button clicked");
				String msg = text.getText().toString();
				text.setText("");
				mAdapter.add("Me: " + msg);
				
				myService.sendMessage(msg);
			}	
		});	
		
		find.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","find button clicked");
				
				myService.start(myGender, preferredGender);
				Log.d("MainActivity","myService started");
			}	
		});	
		
		quit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.d("MainActivity","quit button clicked");
				
				myService.quit(); // tell the service	
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
		Log.e("MainActivity","onStart1");

		Intent intent = new Intent(this, MyBoundService.class);
		
		Log.e("MainActivity","onStart2");

		bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
		Log.e("MainActivity","onStart3");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if (isBound) {
			unbindService(mServiceConnection);
		}
	}
	
	private ServiceConnection mServiceConnection = new ServiceConnection () {

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
