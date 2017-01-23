/**
 * TEMP
 * Classe principale du gestionnaire de contexte
 */
package fr.liglab.adele.icasa.context.manager.impl;
import fr.liglab.adele.icasa.context.manager.api.ContextManagerGoal;
import fr.liglab.adele.icasa.context.manager.api.ContextManagerGoalRegistering;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

@Component
@Provides
@Instantiate
public class ContextManager implements ContextManagerGoalRegistering{



    @Override
    public boolean registerContextManagerGoals(String appId, ContextManagerGoal contextManagerGoal) {
        return false;
    }

    @Override
    public ContextManagerGoal getRegisteredContextManagerGoals(String appId) {
        return null;
    }

    @Override
    public boolean unregisterContextManagerGoals(String appId) {
        return false;
    }
}
