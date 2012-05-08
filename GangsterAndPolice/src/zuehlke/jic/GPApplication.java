package zuehlke.jic;

import android.app.Application;

public class GPApplication extends Application {

	private GPService service;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}


	public GPService getService() {
		return service;
	}
	public void setService(GPService service) {
		this.service = service;
	}
	
}
