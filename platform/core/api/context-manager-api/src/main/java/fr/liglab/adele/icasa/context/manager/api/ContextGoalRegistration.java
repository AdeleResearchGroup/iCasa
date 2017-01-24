package fr.liglab.adele.icasa.context.manager.api;

/**
 * TEMP
 * Les applications enregistrent leurs besoins au gestionnaire de contexte
 * Ces besoins représentent un but sur l'API à fournir
 */
public interface ContextGoalRegistration {

    boolean registerContextManagerGoals(String appId, ContextGoal contextGoal);

    ContextGoal getRegisteredContextManagerGoals(String appId);

    boolean unregisterContextManagerGoals(String appId);
}
