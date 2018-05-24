package fr.liglab.adele.icasa.layering.applications.manager;

import org.json.JSONException;
import org.json.JSONObject;

public class ApplicationDescription {

	public final String provider;
	public final String implementation;
	public final String instance;
	public final boolean isValid;
	
	public ApplicationDescription(String provider, String implementation, String instance, boolean isValid) {
		this.provider 			= provider;
		this.implementation		= implementation;
		this.instance			= instance;
		
		this.isValid			= isValid;
	}
	
    public JSONObject serialize() throws JSONException {
    	
        JSONObject content = new JSONObject();
       
        content.putOnce("provider",provider);
        content.putOnce("implementation",implementation);
        content.putOnce("id",instance);
        content.putOnce("state",isValid);
        
        return content;
    }

}
