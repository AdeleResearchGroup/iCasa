package fr.liglab.adele.icasa.layering.applications.manager;


import org.json.JSONException;
import org.json.JSONObject;

public class ApplicationDescription {

	public final String 	implementation;
	public final String 	instances;
	public final boolean 	enabled;
	
	public ApplicationDescription(String implementation, String instances, boolean enabled) {
		this.implementation		= implementation;
		this.instances			= instances;
		this.enabled			= enabled;
	}
	
    public JSONObject serialize() throws JSONException {
    	
        JSONObject content = new JSONObject();
       
        content.putOnce("implementation",implementation);
        content.putOnce("instances",instances);
        content.putOnce("enabled",enabled);
        
        return content;
    }
    
}
