package fr.liglab.adele.icasa.context.manager.impl.models.api;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Map;
import java.util.Set;

public interface LinkModelUpdate {

    void setMediationTreesOk(boolean mediationTreesOk);

    void setMediationTrees(Map<String, DefaultMutableTreeNode> mediationTrees);

    void setCreatorsToActivate(Set<String> creatorsToActivate);

    void setNonActivableServices(Set<String> nonActivableServices);
}
