package fr.liglab.adele.icasa.context.manager.api;

/**
 * TEMP
 * Les applications enregistrent leurs besoins au gestionnaire de contexte
 * Ces besoins représentent un but sur l'API à fournir
 */
public interface ContextManagerGoalRegistration {

    boolean registerContextManagerGoals(String appId, ContextManagerGoal contextManagerGoal);

    ContextManagerGoal getRegisteredContextManagerGoals(String appId);

    boolean unregisterContextManagerGoals(String appId);
}
