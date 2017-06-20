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
package fr.liglab.adele.icasa.context.manager.impl.temp.generic;

import fr.liglab.adele.icasa.context.manager.api.specific.ContextAPIEnum;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Goal {
    private UUID id;

    private ContextAPIEnum service;
    private int config;

    private Set<String> requiringApp;

    private boolean providable;
    private boolean activated;
    private boolean available;

    public Goal(ContextAPIEnum service, int config){
        this.id = UUID.randomUUID();
        this.service = service;
        this.config = config;

        this.requiringApp = new HashSet<>();
        this.providable = false;
        this.activated = false;
        this.available = false;
    }

    public UUID getId() {
        return id;
    }

    public ContextAPIEnum getService() {
        return service;
    }

    public int getConfig() {
        return config;
    }

    public Set<String> getRequiringApp() {
        return requiringApp;
    }

    public boolean isProvidable() {
        return providable;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setProvidable(boolean providable) {
        this.providable = providable;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean addRequiringApp(String appId){
        boolean results = true;
        try{
            requiringApp.add(appId);
        } catch (NullPointerException ne){
            results = false;
        }
        return results;
    }

    public boolean removeRequiringApp(String appId){
        boolean results = true;
        try{
            requiringApp.remove(appId);
        } catch (NullPointerException ne){
            results = false;
        }
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Goal)) return false;

        Goal goal = (Goal) o;

        return getId() != null ? getId().equals(goal.getId()) : goal.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "service=" + service +
                ", config=" + config +
                ", requiringApp=" + requiringApp +
                ", providable=" + providable +
                ", activated=" + activated +
                ", available=" + available +
                '}';
    }
}
