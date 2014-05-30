/**
 * 
 */
package fr.liglab.adele.habits.monitoring.autonomic.manager.listeners;

import java.util.EventListener;

/**
 *
 */
public interface DPInfosListener extends EventListener {

	/**
	 * Listener method triggered when a new DP is added.
	 * @param addedDP the added DP informations (url, name, handled interfaces).
	 */
	void DPInfosAdded(DPInfos addedDP);
	
	/**
	 * Listener method triggered when a DP is removed.
	 * @param removedDP the removed DP informations (url, name, handled interfaces). 
	 */
	void DPInfosRemoved(DPInfos removedDP);
}
