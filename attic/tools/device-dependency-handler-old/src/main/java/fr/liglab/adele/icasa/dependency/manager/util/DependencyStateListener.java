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
package fr.liglab.adele.icasa.dependency.manager.util;


/**
 * This interface allows a class to be notified of service dependency state changes.
 */
public interface DependencyStateListener {

    /**
     * The given dependency becomes valid.
     * @param dependency the dependency becoming valid.
     */
    void validate(DependencyModel dependency);
    
    /**
     * The given dependency becomes invalid.
     * @param dependency the dependency becoming invalid.
     */
    void invalidate(DependencyModel dependency);
}
