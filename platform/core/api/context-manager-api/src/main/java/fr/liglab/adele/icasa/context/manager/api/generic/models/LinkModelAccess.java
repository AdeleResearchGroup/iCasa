package fr.liglab.adele.icasa.context.manager.api.generic.models;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.Set;

public interface LinkModelAccess {

    boolean isMediationTreesOk();

    Map<String, DefaultMutableTreeNode> getMediationTrees();

    Set<String> getCreatorsToActivate();

    Set<String> getNonActivableServices();
}
