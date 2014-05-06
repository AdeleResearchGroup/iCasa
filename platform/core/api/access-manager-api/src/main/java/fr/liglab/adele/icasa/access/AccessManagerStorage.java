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
package fr.liglab.adele.icasa.access;

import java.io.IOException;

/**
 * The service providing this interface, will be in charge of persist Access Manager configuration,
 * as well as load its configuration.
 */
public interface AccessManagerStorage {
    /**
     * Method called when persisting Access Manager configuration.
     * @throws IOException when there is a problem in writing Access Configuration.
     */
     void persist() throws IOException;

    /**
     * Load the access Manager Configuration.
     * @throws IOException when it can't read the Access Configuration.
     */
    void loadAccess() throws IOException;
}
