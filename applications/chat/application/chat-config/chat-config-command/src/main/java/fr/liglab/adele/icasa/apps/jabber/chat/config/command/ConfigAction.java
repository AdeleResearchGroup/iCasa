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

package fr.liglab.adele.icasa.apps.jabber.chat.config.command;

import fr.liglab.adele.icasa.apps.jabber.chat.config.ui.uiInt.ConfigDisplayer;
import org.apache.felix.gogo.commands.Action;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.HandlerDeclaration;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.service.command.CommandSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Command(name="config",
        scope="jabber",
        description="A settings command")
@HandlerDeclaration("<sh:command xmlns:sh='org.ow2.shelbie'/>")
public class ConfigAction implements Action {

    private static final Logger LOG= LoggerFactory.getLogger(ConfigAction.class);

    @Requires
   private ConfigDisplayer instance;

    public Object execute(CommandSession session) throws Exception {

       instance.showConfig();
        return null;
    }
}