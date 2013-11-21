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
package test.context.handler;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Validate;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.context.handler.annotations.RequiresContext;
import fr.liglab.adele.icasa.listener.IcasaListener;
import fr.liglab.adele.icasa.location.Position;

@Component(name="ContextClientComponent2")
@Instantiate
public class ContextClientComponent2 implements IcasaListener {
        
    @RequiresContext(id="manager2", contextGroupId="test-group-2")
    private ContextManager manager;
    
    @Validate
    private void start() {
        System.out.println("==========> " + manager.getZones().size());
        
        Position position = new Position(20, 10);
        System.out.println(position.x);
        
        manager.setZoneVariable("zone-1", "var-1", "var-1-value");
        
        
    }

}