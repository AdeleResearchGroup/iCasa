/**
 *
 *   Copyright 2011-2013 Universite Joseph Fourier, LIG, ADELE Research
 *   Group Licensed under a specific end user license agreement;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://adeleresearchgroup.github.com/iCasa/snapshot/license.html
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
	 *
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

    /**
     * Registers specified listener to value changes of specified application properties.
     *
     * @param applicationId application id
     * @param listener the listener to register
     */
    void addApplicationPreferenceChangeListener(String applicationId, PreferenceChangeListener listener);

    /**
     * Unregisters specified listener to value changes of specified application properties.
     *
     * @param applicationId application id
     * @param listener the listener to unregister
     */
    void removeApplicationPreferenceChangeListener(String applicationId, PreferenceChangeListener listener);

    /**
     * Registers specified listener to value changes of specified user properties.
     *
     * @param userId user id
     * @param listener the listener to register
     */
    void addUserPreferenceChangeListener(String userId, PreferenceChangeListener listener);

    /**
     * Unregisters specified listener to value changes of specified user properties.
     *
     * @param userId user id
     * @param listener the listener to unregister
     */
    void removeUserPreferenceChangeListener(String userId, PreferenceChangeListener listener);

    /**
     * Registers specified listener to value changes of specified global properties.
     *
     * @param listener the listener to register
     */
    void addGlobalPreferenceChangeListener(PreferenceChangeListener listener);

    /**
     * Unregisters specified listener to value changes of specified global properties.
     *
     * @param listener the listener to unregister
     */
    void removeGlobalPreferenceChangeListener(PreferenceChangeListener listener);
}
