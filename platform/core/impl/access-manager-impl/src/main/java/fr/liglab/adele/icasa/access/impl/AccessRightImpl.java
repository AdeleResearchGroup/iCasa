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
package fr.liglab.adele.icasa.access.impl;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.access.DeviceAccessPolicy;
import fr.liglab.adele.icasa.access.AccessRight;
import fr.liglab.adele.icasa.access.AccessRightListener;
import fr.liglab.adele.icasa.access.MemberAccessPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 *
 */
public class AccessRightImpl implements AccessRight {

    private static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG + ".access");

    private volatile DeviceAccessPolicy policy = DeviceAccessPolicy.HIDDEN;

	private final List VISIBLE_METHODS_LIST = Arrays.asList(new String[] { "getSerialNumber", "getState", "getFault", "removeListener", "getPropertyValue" });

	protected List<AccessRightListener> listeners = new ArrayList<AccessRightListener>();

	private final String applicationId;

	private final String deviceId;

	private final Long identifier;

	/**
	 * A map containing the right access to call methods.
	 */
	protected Map<String, MemberAccessPolicy> rightMethodAccess = new HashMap<String, MemberAccessPolicy>();

	private Map<String, Object> internalMap = new HashMap();
	private Map<String, Object> internalMethods = new HashMap();

	public AccessRightImpl(Long identifier, String application, String device, DeviceAccessPolicy policy) {
		this.applicationId = application;
		this.deviceId = device;
		this.identifier = identifier;
		this.policy = policy;
		this.internalMap.put("applicationId", applicationId);
		this.internalMap.put("deviceId", deviceId);
		this.internalMap.put("policy", policy.toString());
		this.internalMap.put("methods", internalMethods);
	}

	public AccessRightImpl(Long identifier, String application, String device) {
		this(identifier, application, device, DeviceAccessPolicy.HIDDEN);
	}

	public AccessRightImpl(Long identifier, Map<String, Object> fromMap) throws UnknownFormatConversionException {
		this.applicationId = String.valueOf(fromMap.get("applicationId"));
		this.deviceId = String.valueOf(fromMap.get("deviceId"));
		this.policy = DeviceAccessPolicy.fromString(String.valueOf(fromMap.get("policy")));
		if (applicationId == null || deviceId == null) {
            logger.error("Unable to create AccessRight from map, missing values (applicationId, deviceId)");
            throw new NullPointerException("Unable to obtain DeviceAccessPolicy from: "
                    + fromMap.get("policy"));
		}
		if (policy == null) {
            logger.error("Unable to create AccessRight from map, missing values (applicationId, deviceId)");
			throw new UnknownFormatConversionException("Unable to obtain DeviceAccessPolicy from: "
			      + fromMap.get("policy"));
		}
		this.identifier = identifier;
		internalMap.put("applicationId", applicationId);
		internalMap.put("deviceId", deviceId);
		internalMap.put("policy", policy.toString());
		if (fromMap.containsKey("methods")) {
			methodAccessFromMap((Map) fromMap.get("methods"));
		}
		internalMap.put("methods", internalMethods);
	}

	private void methodAccessFromMap(Map<String, String> methodAccess) throws UnknownFormatConversionException {
		for (Map.Entry<String, String> entry : methodAccess.entrySet()) {
			MemberAccessPolicy methodPolicy = MemberAccessPolicy.fromString(entry.getValue());
			if (methodPolicy == null) {
                logger.error("Unable to obtain MemberAccessPolicy from:" + entry.getValue());
				throw new UnknownFormatConversionException("Unable to obtain MemberAccessPolicy from:" + entry.getValue());
			}
			rightMethodAccess.put(entry.getKey(), MemberAccessPolicy.fromString(entry.getValue()));
			internalMethods.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * See if the application has the right to access the device.
	 * 
	 * @return true when the application has the right to access the device. False if not.
	 */
	@Override
	public synchronized boolean isVisible() {
		return policy.compareTo(DeviceAccessPolicy.TOHIDE) > 0;
	}

	/**
	 * See if the application has the right access to call the given method.
	 * 
	 * @param methodName the method to see the right access.
	 * @return true when the application can call the method, false if not.
	 */
	@Override
	public boolean hasMethodAccess(String methodName) throws NullPointerException {
		if (methodName == null) {
            logger.error("Method must not be null");
            throw new NullPointerException("Method must not be null");
		}
		MemberAccessPolicy memberAccessPolicy = MemberAccessPolicy.HIDDEN;
		Boolean exists = false;
		synchronized (this) {
			if (policy.compareTo(DeviceAccessPolicy.TOTAL) == 0) { // If has total access, return true immediately
				return true;
			} else if (policy.compareTo(DeviceAccessPolicy.VISIBLE) == 0) { // If is visible see method name.
				return hasMethodAccessWithVisible(methodName);
			} else if(policy.compareTo(DeviceAccessPolicy.TOHIDE) == 0){ // If will be hidden.
                return hasMethodAccessWithVisible(methodName);
            } else if (policy.compareTo(DeviceAccessPolicy.HIDDEN) == 0) { // If is hidden, return false.
				return false;
			} else if (rightMethodAccess.containsKey(methodName)) { // If is partial
				memberAccessPolicy = rightMethodAccess.get(methodName); // get the right access.
				exists = true;
			}
		}
		if (!exists) { // Added if access right does not exist. It only does in partial.
			updateMethodAccessRight(methodName, MemberAccessPolicy.HIDDEN);
		}
		return memberAccessPolicy.compareTo(MemberAccessPolicy.HIDDEN) > 0 || hasMethodAccessWithVisible(methodName);
	}

	private boolean hasMethodAccessWithVisible(String methodName) {
		if (VISIBLE_METHODS_LIST.contains(methodName)) {
			return true;
		}
		return false;
	}

	/**
	 * Get the list of method whose access has been defined. If an existent device method, does not appear in the list,
	 * the access to the method is denied.
	 * 
	 * @return an array of the existent method access.
	 */
	@Override
	public synchronized String[] getMethodList() {
		return rightMethodAccess.keySet().toArray(new String[0]);
	}

	/**
	 * See if the application has the right access to call the given method.
	 * 
	 * @param method the method to see the right access.
	 * @return true when the application can call the method, false if not.
	 */
	@Override
	public boolean hasMethodAccess(Method method) throws NullPointerException {
		if (method == null) {
			throw new NullPointerException("Method must not be null");
		}
		return hasMethodAccess(method.getName());
	}

	/**
	 * Get the application wanting to access the device.
	 * 
	 * @return the application identifier.
	 */
	@Override
	public String getApplicationId() {
		return applicationId;
	}

	/**
	 * Get the device identifier the application wants to access.
	 * 
	 * @return the device identifier.
	 */
	@Override
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Add a listener to be notified when the access right has been changed.
	 * 
	 * @param listener The listener to be called when an access right has changed.
	 */
	@Override
	public synchronized void addListener(AccessRightListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener to be called when an access right has changed.
	 */
	@Override
	public synchronized void removeListener(AccessRightListener listener) {
		listeners.remove(listener);
	}

	private synchronized List<AccessRightListener> getListeners() {
		return new ArrayList<AccessRightListener>(listeners);
	}

    private void updateAccessToHide(){
        updateDeviceAccessRight(DeviceAccessPolicy.TOHIDE);
        updateDeviceAccessRight(DeviceAccessPolicy.HIDDEN);
    }

    private void updateDeviceAccessRight(DeviceAccessPolicy right){
        List<AccessRightListener> listenerList = null;
        synchronized (this) {
            if (right.equals(policy)) { // If is the same, does not trigger callback.
                return;
            }
            policy = right;
            this.internalMap.put("policy", policy.toString());
            listenerList = getListeners();
        }
        logger.debug("Access Right updated: " + right);
        for (AccessRightListener listener : listenerList) {
            listener.onAccessRightModified(this);
        }
    }

	/**
	 * 
	 * @param right
	 */
	protected boolean updateAccessRight(DeviceAccessPolicy right) {
        if(right == null){
            logger.error("Unable to set invalid policy" + right);
            throw new NullPointerException("Null policy");
        }
        if(right.compareTo(DeviceAccessPolicy.HIDDEN) == 0){
            updateAccessToHide();
        } else if (right.compareTo(DeviceAccessPolicy.TOHIDE) == 0){
            logger.error("Unable to set a transient device policy");
            return false;
        } else {
            updateDeviceAccessRight(right);
        }
        return true;
	}

	protected boolean updateMethodAccessRight(String method, MemberAccessPolicy right) throws NullPointerException {
		List<AccessRightListener> listenerList = null;
		if (method == null) {
            logger.error("Unable to set access right. Method must not be null");
            throw new NullPointerException("Method must not be null");
		}
		synchronized (this) {
			if (rightMethodAccess.containsKey(method)) {
				if (rightMethodAccess.get(method).equals(right)) {
					return false; // If is the same, do nothing.
				}
			}
			rightMethodAccess.put(method, right);
			internalMethods.put(method, right.toString());
			listenerList = getListeners();
		}
        logger.debug("Updating access right in method " + method + " right: " + right);
        for (AccessRightListener listener : listenerList) {
			listener.onMethodAccessRightModified(this, method);
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		AccessRightImpl that = (AccessRightImpl) o;

		if (!applicationId.equals(that.applicationId))
			return false;
		if (!deviceId.equals(that.deviceId))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = applicationId.hashCode();
		result = 31 * result + deviceId.hashCode();
		return result;
	}

	public DeviceAccessPolicy getPolicy() {
		return policy;
	}

	@Override
	public Long getIdentifier() {
		return identifier;
	}

	public synchronized Map toMap() {
		return new HashMap(internalMap);
	}

}
