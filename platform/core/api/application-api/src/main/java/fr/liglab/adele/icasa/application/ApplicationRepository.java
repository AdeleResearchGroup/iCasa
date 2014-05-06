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

import fr.liglab.adele.icasa.common.ProgressMonitor;

import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * It represents a repository of applications.
 *
 *
 */
public interface ApplicationRepository {

	/**
	 * Returns list of available applications.
	 * Returned list contains only last application version.
	 * 
	 * @return list of available applications.
	 */
	public List<ApplicationVersion> getAvailableApplications();
	
	//TODO add more advanced request support
	
	/**
	 * Returns URLs of bundles we need to deploy to install specified application version.
	 * 
	 * @param appVersion application version to install
	 * @param monitor an operation progress monitor (may be null)
	 * @return URLs of bundles we need to deploy to install specified application version.
	 */
	public Set<URL> getApplicationBundleURLs(ApplicationVersion appVersion, ProgressMonitor monitor);
}
