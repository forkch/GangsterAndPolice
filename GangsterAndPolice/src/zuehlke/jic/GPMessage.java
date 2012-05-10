package zuehlke.jic;

public class GPMessage {

	private String clientId;
	private String message;
	private String time;

	public GPMessage(String clientId, String message, String time) {
		this.clientId = clientId;
		this.message = message;
		this.time = time;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
