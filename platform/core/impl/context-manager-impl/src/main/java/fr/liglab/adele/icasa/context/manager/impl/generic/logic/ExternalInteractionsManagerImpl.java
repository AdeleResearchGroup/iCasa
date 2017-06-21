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
package fr.liglab.adele.icasa.context.manager.impl.generic.logic;

import fr.liglab.adele.icasa.context.manager.api.generic.ContextManagerAdmin;
import fr.liglab.adele.icasa.context.manager.api.generic.models.ExternalFilterModelAccess;
import fr.liglab.adele.iop.device.api.IOPLookupService;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class ExternalInteractionsManagerImpl implements ExternalInteractionsManager {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalInteractionsManagerImpl.class);

    @Requires
    @SuppressWarnings("unused")
    private ExternalFilterModelAccess externalFilterModel;

    /*Environment filter - Lookup IOP Controller*/
    @Requires(optional = true)
    @SuppressWarnings("unused")
    private IOPLookupService iopLookupService;

    /*Environment lookup filter*/
    private static Set<String> previousLookupFilter = new HashSet<>();

    /*Environment interface*/
    public void updateLookupFilter() {

        Set<String> newFilter = externalFilterModel.getLookupFilter();
        if(ContextManagerAdmin.getAutoLookup()) {

            Set<String> toConsider = new HashSet<>();
            Set<String> toDiscard  = new HashSet<>();

            /*Added services*/
            for (String service : newFilter) {
                if (!previousLookupFilter.contains(service)) {
                    toConsider.add(service);
                }
            }

            /*Removed services*/
            for (String service : previousLookupFilter) {
                if (!newFilter.contains(service)) {
                    toDiscard.add(service);
                }
            }

            /*Environment lookup*/
            if (iopLookupService != null) {
                if(!toConsider.isEmpty()){
                    String[] c = new String[toConsider.size()];
                    c = toConsider.toArray(c);
                    iopLookupService.consider(c);
                    previousLookupFilter.addAll(toConsider);
                }

                if(!toDiscard.isEmpty()){
                    String[] d = new String[toDiscard.size()];
                    d = toDiscard.toArray(d);
                    iopLookupService.discard(d);
                    previousLookupFilter.removeAll(toDiscard);
                }

                int logLevel = ContextManagerAdmin.getLogLevel();
                if(logLevel>=3) {
                    LOG.info("AUTO LOOKUP FILTER CONSIDER: " + toConsider);
                    LOG.info("AUTO LOOKUP FILTER DISCARD: " + toDiscard);
                }
                if(logLevel>=1) {
                    LOG.info("AUTO LOOKUP FILTER: " + previousLookupFilter);
                }
            }
        }
    }
}
