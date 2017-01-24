/**
 * TEMP
 * Classe principale du gestionnaire de contexte
 */
package fr.liglab.adele.icasa.context.manager.impl;
import fr.liglab.adele.icasa.context.manager.api.ContextGoal;
import fr.liglab.adele.icasa.context.manager.api.ContextManagerGoalRegistration;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

@Component
@Provides
@Instantiate
public class ContextManager implements ContextManagerGoalRegistration {



    @Override
    public boolean registerContextManagerGoals(String appId, ContextGoal contextGoal) {
        return false;
    }

    @Override
    public ContextGoal getRegisteredContextManagerGoals(String appId) {
        return null;
    }

    @Override
    public boolean unregisterContextManagerGoals(String appId) {
        return false;
    }
}
