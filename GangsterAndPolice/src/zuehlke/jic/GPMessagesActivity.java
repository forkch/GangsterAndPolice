package zuehlke.jic;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GPMessagesActivity extends Activity implements GPServiceListener {

	private ArrayAdapter<GPMessage> arrayAdapter;
	private GPApplication application;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messages);
		application = (GPApplication) getApplication();
		application.getService().registerGPServiceListener(this);
		handler = new Handler(Looper.getMainLooper());
	}

	@Override
	protected void onResume() {
		super.onResume();
		final List<GPMessage> messages = application.getMessages();
		ListView listView = (ListView) findViewById(R.id.messageList);
		arrayAdapter = new ArrayAdapter<GPMessage>(this,
				R.layout.message_list_item, messages) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = convertView;
				TextView textView;
				if (row == null) {
					LayoutInflater inflater = ((Activity) GPMessagesActivity.this)
							.getLayoutInflater();
					row = inflater.inflate(R.layout.message_list_item, parent,
							false);
					textView = (TextView) row.findViewById(R.id.listItem);
					row.setTag(textView);
				} else {
					textView = (TextView) row.getTag();
				}

				GPMessage message = messages.get(position);
				textView.setText(createMessageString(message));

				return row;

			}
		};
		listView.setAdapter(arrayAdapter);
	}

	protected CharSequence createMessageString(GPMessage message) {
		if (message.getClientId().equals(application.getClientId())) {
			
			return "Me: " + message.getMessage() + " ("
					+ message.getTime() + ")";
		} else {
			Player player = application.getPlayers().get(message.getClientId());

			return player.getName() + ": " + message.getMessage() + " ("
					+ message.getTime() + ")";
		}
	}

	@Override
	public void onConnectionEstablished() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionFailed(Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegistration(String clientId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPositionUpdate(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageReceived(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewPlayer(Player p) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessage(GPMessage msg) {
		handler.post(new Runnable() {

			public void run() {
				arrayAdapter.notifyDataSetChanged();
			}
		});

	}

	@Override
	public void onArrestablePlayer(Player arrestablePlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNoArrestablePlayer() {
		// TODO Auto-generated method stub
		
	}

}
