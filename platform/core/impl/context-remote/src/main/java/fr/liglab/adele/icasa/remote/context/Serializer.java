package fr.liglab.adele.icasa.remote.context;

import fr.liglab.adele.icasa.device.GenericDevice;
import fr.liglab.adele.icasa.remote.context.serialization.SerializedDevice;

public interface Serializer {
	
	public void serialize(SerializedDevice result, GenericDevice device) ;

}