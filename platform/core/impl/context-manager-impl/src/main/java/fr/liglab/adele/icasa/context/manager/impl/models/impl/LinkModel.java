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

import fr.liglab.adele.icasa.context.manager.api.generic.models.LinkModelAccess;
import fr.liglab.adele.icasa.context.manager.impl.models.api.LinkModelUpdate;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*ToDo*/
@Component(immediate = true, publicFactory = false)
@Instantiate
@Provides
@SuppressWarnings("unused")
public class LinkModel implements LinkModelAccess, LinkModelUpdate{

    private boolean mediationTreesOk = false;
    private Map<String, DefaultMutableTreeNode> mediationTrees = new HashMap<>();
    private Set<String> creatorsToActivate = new HashSet<>();
    private Set<String> nonActivableServices = new HashSet<>();

    public boolean isMediationTreesOk() {
        return mediationTreesOk;
    }

    public void setMediationTreesOk(boolean mediationTreesOk) {
        this.mediationTreesOk = mediationTreesOk;
    }

    public Map<String, DefaultMutableTreeNode> getMediationTrees() {
        return mediationTrees;
    }

    public void setMediationTrees(Map<String, DefaultMutableTreeNode> mediationTrees) {
        this.mediationTrees = mediationTrees;
    }

    public Set<String> getCreatorsToActivate() {
        return creatorsToActivate;
    }

    public void setCreatorsToActivate(Set<String> creatorsToActivate) {
        this.creatorsToActivate = creatorsToActivate;
    }

    public Set<String> getNonActivableServices() {
        return nonActivableServices;
    }

    public void setNonActivableServices(Set<String> nonActivableServices) {
        this.nonActivableServices = nonActivableServices;
    }
}
