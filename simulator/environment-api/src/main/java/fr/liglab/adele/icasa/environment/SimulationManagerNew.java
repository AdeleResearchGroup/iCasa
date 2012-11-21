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
package fr.liglab.adele.icasa.environment;

import java.util.List;
import java.util.Set;

/**
 * TODO Comments.
 * 
 * @author bourretp
 */
public interface SimulationManagerNew {

	void createZone(String id, String description, int leftX, int topY, int width, int height);

	List<Zone> getZones();
	
	List<Device> getDevices();
	
	Zone getZone(String zoneId);
	
	Position getDevicePosition(String deviceSerialNumber);

	void setDevicePosition(String deviceSerialNumber, Position position);

	void setDeviceZone(String deviceSerialNumber, String zoneId);

	void setPersonPosition(String userName, Position position);

	void setPersonZone(String userName, String environmentId);

	void removeAllPersons();

	void addPerson(String userName);

	void removePerson(String userName);

	public List<Person> getPersons();

	Set<String> getEnvironmentVariables(String zoneId);

	Double getVariableValue(String zoneId, String variable);

	void setEnvironmentVariable(String zoneId, String variable, Double value);

	void setDeviceFault(String deviceId, boolean value);

	void setDeviceState(String deviceId, boolean value);

	void createDevice(String factoryName, String deviceId, String description);

	void removeDevice(String deviceId);

	Set<String> getDeviceFactories();

}
