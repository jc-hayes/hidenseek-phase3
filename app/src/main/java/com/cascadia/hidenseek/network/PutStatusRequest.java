package com.cascadia.hidenseek.network;

import org.json.JSONException;

import com.cascadia.hidenseek.Player;
import com.cascadia.hidenseek.network.NetworkBase.RequestType;

public abstract class PutStatusRequest extends NetworkRequest {

	public void DoRequest(Player p) {
		Request r = new Request();
		r.url = baseUrl + "players/" + p.getId() + "/status/";
		r.type = RequestType.PUT;
		try {
			r.jsonArgs = p.statusToJSON();
		} catch(JSONException e) {
			onException(e);
		}
		doRequest(r);
	}
	
	//To be overwritten
	protected void onComplete() { }
	
	@Override
	protected final void processPostExecute(String s) {
		onComplete();
	}
}
