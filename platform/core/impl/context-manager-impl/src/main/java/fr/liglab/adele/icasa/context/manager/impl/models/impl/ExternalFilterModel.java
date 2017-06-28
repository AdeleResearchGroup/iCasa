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
package fr.liglab.adele.icasa.context.manager.impl.models.impl;

import fr.liglab.adele.icasa.context.manager.api.models.ExternalFilterModelAccess;
import fr.liglab.adele.icasa.context.manager.impl.models.api.ExternalFilterModelUpdate;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class ExternalFilterModel implements ExternalFilterModelAccess, ExternalFilterModelUpdate {

    private Set<String> lookupFilter = new HashSet<>();

    @Override
    public Set<String> getLookupFilter() {
        return Collections.unmodifiableSet(lookupFilter);
    }

    @Override
    public void setLookupFilter(Set<String> filter) {
        try{
            lookupFilter.clear();
            lookupFilter.addAll(filter);
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }

    @Override
    public void addServiceToLookupFilter(String service) {
        try{
            lookupFilter.add(service);
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }

    @Override
    public void removeServiceToLookupFilter(String service) {
        try{
            lookupFilter.remove(service);
        } catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }
}