package fr.liglab.adele.icasa.context.ipojo.module;

import java.util.List;

import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;

import fr.liglab.adele.icasa.context.model.annotations.provider.Creator;
import fr.liglab.adele.icasa.context.model.annotations.entity.ContextEntity.Relation;

public class RelationProviderProcessor extends ProviderProcessor<Creator.Field> {

	protected RelationProviderProcessor(ClassLoader classReferenceLoader) {
		super(Creator.Field.class,classReferenceLoader);
	}

	@Override
	protected void processCreator(Element creator) {
		
		List<String> typeArguments	= new TypeArgumentExtractor(getAnnotatedField().signature).getTypeArguments();
		
		String sourceEntity		= typeArguments.size() == 2 ? typeArguments.get(0) : null; 
		String relation			= getAnnotation().value();
		String targetEntity		= typeArguments.size() == 2 ? typeArguments.get(1) : null;
		
		if (sourceEntity != null  && targetEntity != null && ! relation.equals(Creator.Field.NO_PARAMETER)) {
			creator.addAttribute(new Attribute("entity",sourceEntity));
			creator.addAttribute(new Attribute("relation",Relation.ID(getSimpleClassName(sourceEntity),relation)));
			creator.addAttribute(new Attribute("target",targetEntity));
		} 
		else if (sourceEntity == null || targetEntity == null){
			error("relation creator field '%s' in class %s must parameterize type Creator.relation with the source and target entity class",
					getAnnotatedField().name, getAnnotatedClass().name);
		}
		else if (relation.equals(Creator.Field.NO_PARAMETER)) {
			error("relation creator field '%s' in class %s must specify the name of the relation",
					getAnnotatedField().name, getAnnotatedClass().name);
		}
	}

}
