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
package fr.liglab.adele.icasa.simulator.provider;

import fr.liglab.adele.cream.annotations.entity.ContextEntity.State;
import fr.liglab.adele.cream.annotations.provider.Creator;
import fr.liglab.adele.cream.annotations.provider.OriginEnum;
import fr.liglab.adele.icasa.simulator.impl.PersonImpl;
import fr.liglab.adele.icasa.simulator.person.Person;
import fr.liglab.adele.icasa.simulator.person.PersonProvider;
import fr.liglab.adele.icasa.simulator.person.PersonType;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component(immediate = true, publicFactory=false)
@Provides(specifications = PersonProvider.class)
@Instantiate
public class PersonProviderImpl implements PersonProvider {

    @Creator.Field(origin = OriginEnum.local) Creator.Entity<PersonImpl> personCreator;

    @Override
    public void createPerson(String personName, String personType) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(State.id(Person.class,Person.NAME), personName);
        properties.put(State.id(Person.class,Person.TYPE), PersonType.getPersonType(personType));

        personCreator.create(personName, properties);
    }

    @Override
    public void removePerson(String personName) {
        personCreator.delete(personName);
    }

    @Override
    public Set<String> getPersonTypes() {
        Set<String> personTypes = new HashSet<>();
        for (String pId : personCreator.getInstances()){
            personTypes.add(personCreator.getInstance(pId).getPersonType().getName());
        }
        return personTypes;
    }

    @Override
    public void removeAllPersons() {
        personCreator.deleteAll();
    }
}
