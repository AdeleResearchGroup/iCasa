/**
 *
 *   Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
 *   Licensed under the Apache License, Version 2.0 (the "License");
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
package test.context.source;

import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Unbind;

import fr.liglab.adele.icasa.device.GenericDevice;

@Component(name = "ConsumerFakeDevice")
@Instantiate
public class ConsumerFakeDevice {

	@Bind(id = "dev", filter = "(location=${person.paul.location})")
	public void bindFakeDevice(GenericDevice device) {
		System.out.println("--------------> Device Binded ----- " + device.getSerialNumber());
	}

	@Unbind(id = "dev")
	public void unbindFakeDevice(GenericDevice device) {
		System.out.println("--------------> Device Unbinded ----- " + device.getSerialNumber());
	}

}
