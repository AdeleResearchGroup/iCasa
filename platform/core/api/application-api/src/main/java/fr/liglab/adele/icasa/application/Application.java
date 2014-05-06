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
package fr.liglab.adele.icasa.application;

import java.util.Set;

import fr.liglab.adele.icasa.common.Attributable;
import fr.liglab.adele.icasa.common.Identifiable;
import fr.liglab.adele.icasa.common.ProgressMonitor;

import org.osgi.framework.Bundle;

/**
 * Represents an installed application.
 *
 *
 */
public interface Application extends Attributable, Identifiable {
	
	public static final String APP_ID_BUNDLE_HEADER = "ICasa-Application-Id";
	
	/**
	 * This header is optional.
	 */
	public static final String APP_NAME_BUNDLE_HEADER = "ICasa-Application-Name";
	
	public static final String APP_VERSION_BUNDLE_HEADER = "ICasa-Application-Version";

	/**
	 * Returns the human readable application name.
	 * This name should not be more than 80 characters.
	 * This value must be set as an attribute value.
	 * 
	 * @return the human readable application name.
	 */
	public String getName();
	
	/**
	 * Returns the vendor of this application.
	 * This value must be set as an attribute value.
	 * 
	 * @return the vendor of this application.
	 */
	public String getVendor();
	
	/**
	 * Returns installed version of this application.
	 * This value must be set as an attribute value.
	 * 
	 * @return installed version of this application.
	 */
	public String getVersion();
	
	/**
	 * Returns the category of this application.
	 * It defines which kind of application is provided and will result on a computed criticity value.
	 * This value must be set as an attribute value.
	 * 
	 * @return the category of this application.
	 */
	public ApplicationCategory getCategory();
	
	/**
	 * Starts this application.
	 * Resources for this application must not be allocated before it has started. 
	 * 
	 * @param monitor a operation progress monitor (may be null)
	 */
	public void start(ProgressMonitor monitor);
	
	/**
	 * Stops this application.
	 * Allocated resources must be clean up before returning. 
	 * 
	 * @param monitor a operation progress monitor (may be null)
	 */
	public void stop(ProgressMonitor monitor);
	
	/**
	 * Resume this application if it has been paused.
	 * 
	 * @param monitor a operation progress monitor (may be null)
	 */
	public void resume(ProgressMonitor monitor);
	
	/**
	 * Stop application computation temporary.
	 * All computation must be stopped or paused.
	 * In particular, there should not be any messages sent from this digital application.
	 * However, incoming messages may still arrive.
	 * Allocated resource could remain in memory.
	 * 
	 * @param monitor a operation progress monitor (may be null)
	 */
	public void pause(ProgressMonitor monitor);
	
	/**
	 * Returns all started bundles which are part of this application implementation.
	 * The application bundles may be installed but not started, in this case, returns an empty set.
	 * 
	 * @return all bundles which are part of this application implementation.
	 */
	public Set<Bundle> getBundles();
	
	/**
	 * Returns application activation state.
	 * 
	 * @return application activation state.
	 */
	public ApplicationState getState();
}
