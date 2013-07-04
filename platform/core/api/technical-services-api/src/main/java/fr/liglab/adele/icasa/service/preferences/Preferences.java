/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package fr.liglab.adele.icasa.service.preferences;

import java.util.Set;

/**
 * The Preferences interface provides a service to store preferences of:
 * <li>Global Preferences</li>
 * <li>Application Preferences</li>
 * <li>User Preferences</li>
 * 
 * @author Gabriel Pedraza Ferreira
 *
 */
public interface Preferences {

	/**
	 * Gets the value of a property for a global preference
	 * @param name the preference name
	 * @return the value associated to the preference
	 */
	Object getGlobalPropertyValue(String name);
	
	/**
	 * Gets the value of a property for a user preference
	 * @param user the user name
	 * @param name the preference name
	 * @return the value associated to the preference
	 */
	Object getUserPropertyValue(String user, String name);
	
	/**
	 * Gets the value of a property for a application preference
	 * @param applicationId the application Id
	 * @param name the preference name
	 * @return the value associated to the preference
	 */
	Object getApplicationPropertyValue(String applicationId, String name);
	
	/**
	 * Sets the value of a property for a global preference
	 * @param name the preference name
	 * @param value the new value associated to the preference
	 */
	void setGlobalPropertyValue(String name, Object value);
	
	/**
	 * Sets the value of a property for a user preference
	 * @param user the user name
	 * @param name the preference name
	 * @return the new value associated to the preference
	 */
	void setUserPropertyValue(String user, String name, Object value);
	
	/**
	 * Sets the value of a property for a application preference
	 * @param applicationId the application Id
	 * @param name the preference name
	 * @return the new value associated to the preference
	 */
	void setApplicationPropertyValue(String applicationId, String name, Object value);
	
	/**
	 * Gets a set of global properties' names
	 * @return set of properties' name
	 */
	Set<String> getGlobalProperties();
	
	/**
	 * Gets a set of user properties' names
	 * @param user the user name
	 * @return set of properties' name
	 */
	Set<String> getUserProperties(String user);
	
	/**
	 * Gets a set of application properties' names
	 * @param applicationId the application Id
	 * @return set of properties' name
	 */
	Set<String> getApplicationProperties(String applicationId);
}
