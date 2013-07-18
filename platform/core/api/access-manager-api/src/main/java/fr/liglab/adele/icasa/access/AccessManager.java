/*
 * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE Research
 * Group Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.icasa.access;

import java.lang.reflect.Method;

/**
 * This interface allows to inspect the rights access of applications to use iCasa devices.
 */
public interface AccessManager {

    /**
     * Get the right access of an application to use a specified device.
     * The {@code returned} object will be synchronized by the Access Manager to
     * maintain updated the using rights.
     * @param applicationId The identifier of the application.
     * @param deviceId The target device to use if it has the correct rights.
     * @return An {@link AccessRight} object which has the rights information
     * of the usage of the device.
     */
    AccessRight getRightAccess(String applicationId, String deviceId);

    /**
     * Get the right access of an application.
     * The returned object will be synchronized by the Access Manager to
     * maintain updated the access right.
     * @param applicationId The identifier of the application.
     * @return An array of {@link AccessRight} objects which has the rights information for the application.
     */
    AccessRight[] getRightAccess(String applicationId);

    /**
     * Set the right access for an application to use a given device.
     * @param applicationId The application identifier.
     * @param deviceId The device identifier.
     * @param methodName The method name to set the right access.
     * @param accessRight The right access.
     */
    void updateAccess(String applicationId, String deviceId, String methodName, boolean accessRight);

    /**
     * Set the right access for an application to use a given device.
     * @param applicationId The application identifier.
     * @param deviceId The device identifier.
     * @param method The method name to set the right access.
     * @param accessRight The right access.
     */
    void updateAccess(String applicationId, String deviceId, Method method, boolean accessRight);

    /**
     * Set the right access for an application to use a device.
     * @param applicationId The application wanting to use the device.
     * @param deviceId The device identifier.
     * @param right The right access.
     */
    void updateAccess(String applicationId, String deviceId, boolean right);

}
