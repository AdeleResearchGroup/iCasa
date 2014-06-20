/**
 * Copyright 2010 Bull S.A.S.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.icasa.jabber.chat.commands.turnaction;

import fr.liglab.adele.icasa.apps.jabber.chat.regulators.regInt.Regulator;
import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.service.command.CommandSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Command(name="turn",
        scope="jabber",
        description="A simple turn command")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class TurnAction implements Action {
    private static final Logger LOG= LoggerFactory.getLogger(TurnAction.class);
    @Requires(optional=true, filter="(type=light)")
    private Regulator LReg;

    @Requires(optional=true, filter="(type=temperature)")
    private Regulator TempReg;

    @Option(name = "-l",
            aliases = "--loc",
            required = false,
            description = "Devices' location")
    private String location = "all";

    @Option(name = "-d",
            aliases = "--device",
            required = false,
            description = "Device type")
    private String device = "light";

    @Option(name = "-o",
            aliases = "--option",
            required = false,
            description = "option")
    private String option = "on";

    public Object execute(CommandSession session) throws Exception {

        // Select the output language
        if (option.equals("on")) {
            if(device.equals("light")){
                LReg.setOn(location);
            }else if(device.equals("temperature")){
                TempReg.setOn(location);
            }else{
                LOG.info("device unrecognized");
            }

        } else if (option.equals("off")) {
            if(device.equals("light")){
                LReg.setOff(location);
            }else if(device.equals("temperature")){
                TempReg.setOff(location);
            }
            else{
                LOG.info("device unrecognized");
            }
        } else {
            throw new Exception("Unknown command");
        }
        return null;

    }
}