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
package fr.liglab.adele.icasa.zigbee.device.importer.test;

/*
 * #%L
 * OW2 Chameleon - Fuchsia Importer Philips Hue
 * %%
 * Copyright (C) 2009 - 2014 OW2 Chameleon
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import junit.framework.Assert;
import org.fest.reflect.reference.TypeRef;
import org.junit.Test;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclaration;
import org.ow2.chameleon.fuchsia.core.declaration.ImportDeclarationBuilder;
import org.ow2.chameleon.fuchsia.core.exceptions.BinderException;
import java.util.HashMap;
import static org.mockito.Mockito.*;

public class ZigbeeDeviceImporterTest extends ZigbeeDeviceImporterAbstractTest{

    private final String ZIGBEE_TYPE_CODE= "C005";
    private final String ZIGBEE_SERIAL_NUMBER= "zigbee#2";
    private final String ZIGBEE_MODULE_ADRESS="2" ;

    private HashMap<String, Object> generateValidMetadata(){
        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("zigbee.device.type.code",ZIGBEE_TYPE_CODE);
        metadata.put("zigbee.module.adress",ZIGBEE_MODULE_ADRESS);
        metadata.put("zigbee.module.serial.number",ZIGBEE_SERIAL_NUMBER);
        return metadata;
    }

    @Test
    public void checkDeclarationTurnsHandledAfterCallAndUnhandledAfterRemoved() throws BinderException {

        ImportDeclaration declaration = spy(ImportDeclarationBuilder.fromMetadata(generateValidMetadata()).build());
        importer.registration(serviceReference);
        declaration.bind(serviceReference);

        importer.useDeclaration(declaration);


        verify(declaration,times(1)).handle(serviceReference);
  //      Assert.assertEquals(1,importer.getImportDeclarations().size());

        importer.denyDeclaration(declaration);
        verify(declaration,times(1)).unhandle(serviceReference);

    }

}
