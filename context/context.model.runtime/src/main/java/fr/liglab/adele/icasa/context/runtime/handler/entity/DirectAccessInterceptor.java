package fr.liglab.adele.icasa.context.runtime.handler.entity;

import org.apache.felix.ipojo.ConfigurationException;
import org.apache.felix.ipojo.FieldInterceptor;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.PojoMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor to handle state fields that are directly manipulated by the entity code
 */
public class DirectAccessInterceptor implements FieldInterceptor {

    /**
     * The associated entity handler in charge of keeping the context state
     */
	private final EntityHandler entityHandler;

	/**
	 * The mapping from fields handled by this interceptor to states of the context
	 */
	private final Map<String,String> fieldToState = new HashMap<>();
	
	/**
	 * @param entityHandler
	 */
	public DirectAccessInterceptor(EntityHandler entityHandler) {
		this.entityHandler = entityHandler;
	}

	/**
	 * Adds a new managed field
	 */
	public void handleState(InstanceManager component, PojoMetadata componentMetadata, Element state) throws ConfigurationException {
		
		String stateId				= state.getAttribute("id");
		String stateField			= state.getAttribute("field");

		/*
		 * Check the association field to state
		 */
		if (stateField == null) {
			throw new ConfigurationException("Malformed Manifest : a state variable is declared with no 'field' attribute");
		}
		
        FieldMetadata fieldMetadata = componentMetadata.getField(stateField);
		if (fieldMetadata == null) {
			throw new ConfigurationException("Malformed Manifest : the specified field doesn't exists "+stateField);
		}
		
        fieldToState.put(stateField,stateId);
        component.register(fieldMetadata,this);
	}
	
    @Override
    public Object onGet(Object pojo, String fieldName, Object value) {
    	return entityHandler.getStateValue(fieldToState.get(fieldName));
    }
	
	@Override
    public void onSet(Object pojo, String fieldName, Object value) {
		entityHandler.update(fieldToState.get(fieldName),value);
    }

}