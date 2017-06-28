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
package fr.liglab.adele.icasa.context.manager.api.old.generic;

import java.util.Set;
import java.util.UUID;

/**
 * Interface of EntityResource Model
 * Modified by importers
 * Read by context manager components
 */
public interface ResourceRegistration {

    Set<Resource> getImportedResources(String importerId);

    boolean importResource(EntityResource resource);

    boolean importResource(RelationResource relation);

    boolean removeResource(UUID resourceId);

    boolean removeAllResourcesFromImporter(String importerId);
}
