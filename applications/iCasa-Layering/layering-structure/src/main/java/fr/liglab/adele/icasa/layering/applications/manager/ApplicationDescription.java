package fr.liglab.adele.icasa.layering.applications.manager;


public class ApplicationDescription {

	public final String 	implementation;
	public final String 	instances;
	public final boolean 	enabled;
	
	public ApplicationDescription(String implementation, String instances, boolean enabled) {
		this.implementation		= implementation;
		this.instances			= instances;
		this.enabled			= enabled;
	}
	
    
}
