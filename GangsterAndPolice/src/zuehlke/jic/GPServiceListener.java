package zuehlke.jic;


public interface GPServiceListener {

	public void onConnectionEstablished();
	
	public void onConnectionFailed(Exception exception);
	
	public void onRegistration(String clientId);

	public void onPositionUpdate(Player p);

	public void onMessageReceived(String message);

	public void onArrestSuccessful();

	public void onArrestUnsuccessful();

}
