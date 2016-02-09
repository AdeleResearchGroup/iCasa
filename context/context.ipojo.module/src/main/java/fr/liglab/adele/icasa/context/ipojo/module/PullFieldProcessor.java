package fr.liglab.adele.icasa.context.ipojo.module;

import fr.liglab.adele.icasa.context.model.annotations.entity.ContextEntity.State;

/**
 * Created by aygalinc on 15/01/16.
 */
public class PullFieldProcessor extends StateProcessor<State.Pull>  {

    public PullFieldProcessor(ClassLoader classReferenceLoader) {
        super(State.Pull.class,classReferenceLoader);
    }

	@Override
	protected String getStateId() {
		return State.ID(getAnnotation().service(), getAnnotation().state());
	}
    
	@Override
	protected void processStateAttributes() {
		addStateAttribute("pull",getAnnotatedField().name,false);
		addStateAttribute("period",Long.toString(getAnnotation().period()),true);
		addStateAttribute("unit",getAnnotation().unit().name(),true);
    }
}
