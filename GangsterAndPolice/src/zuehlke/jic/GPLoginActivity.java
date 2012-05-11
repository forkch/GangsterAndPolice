package zuehlke.jic;

import zuehlke.jic.GPService.GPBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

public class GPLoginActivity extends Activity implements GPServiceListener {
	protected GPService mService;
	protected boolean mBound;
	private EditText nameText;
	private RadioButton policeRadio;
	private RadioButton gangsterRadio;
	private Handler handler;
	private GPApplication application;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		nameText = (EditText) findViewById(R.id.nameText);
		policeRadio = (RadioButton) findViewById(R.id.policeRadio);
		gangsterRadio = (RadioButton) findViewById(R.id.gangsterRadio);

		application = (GPApplication) getApplication();
		handler = new Handler(getMainLooper());
	}

	public void onRegisterClick(View view) {

		String name = nameText.getText().toString();
		if (name.isEmpty() || name == null) {
			toastMsg("Please enter a name");
			return;
		}
		String role = policeRadio.isChecked() ? "police" : "robber";
		try {
			mService.registerClient(name, role);
		} catch (GPServiceException e) {
			Toast.makeText(this, "Could not register", Toast.LENGTH_LONG);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Bind to LocalService
		Intent intent = new Intent(this, GPService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			GPBinder binder = (GPBinder) service;
			mService = binder.getService();
			application.setService(mService);
			mService.registerGPServiceListener(GPLoginActivity.this);
			try {
				mService.connectSocket();
			} catch (GPServiceException e) {
				Toast.makeText(GPLoginActivity.this, "Error connecting!",
						Toast.LENGTH_LONG);
			}
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	private void toastMsg(final String msg) {
		handler.post(new Runnable() {

			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	@Override
	public void onRegistration(final String clientId) {
		nameText.post(new Runnable() {

			@Override
			public void run() {
				GPLoginActivity.this.startActivity(new Intent(
						GPLoginActivity.this, GPMapActivity.class));
			}
		});
	}

	@Override
	public void onConnectionFailed(Exception e) {
		nameText.post(new Runnable() {

			@Override
			public void run() {
				toastMsg("Connection failed");

			}
		});
	}

	@Override
	public void onPositionUpdate(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageReceived(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionEstablished() {
		final Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.post(new Runnable() {

			@Override
			public void run() {
				nameText.setVisibility(EditText.VISIBLE);
				policeRadio.setVisibility(RadioButton.VISIBLE);
				gangsterRadio.setVisibility(RadioButton.VISIBLE);
				registerButton.setVisibility(Button.VISIBLE);

				findViewById(R.id.loadingProgressBar).setVisibility(
						ProgressBar.INVISIBLE);
			}
		});
	}

	@Override
	public void onNewPlayer(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(GPMessage msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onArrestablePlayer(Player arrestablePlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNoArrestablePlayerLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHit(Player player) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAllRobbersArrested(String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGameOver() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWeAreBeingArrested() {
		// TODO Auto-generated method stub
		
	}
}