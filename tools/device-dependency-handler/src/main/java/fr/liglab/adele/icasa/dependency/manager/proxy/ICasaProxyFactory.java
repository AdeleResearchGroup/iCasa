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
package fr.liglab.adele.icasa.dependency.manager.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.osgi.framework.BundleContext;

import fr.liglab.adele.icasa.access.AccessRight;
import fr.liglab.adele.icasa.dependency.manager.DeviceDependency;
import fr.liglab.adele.icasa.dependency.manager.DeviceDependencyHandler;
import fr.liglab.adele.icasa.dependency.manager.exception.AccessViolationException;
import fr.liglab.adele.icasa.device.GenericDevice;

public class ICasaProxyFactory implements InvocationHandler {

	/**
	 * 
	 */
   private final DeviceDependency m_dependency;

	/**
	 * HashCode method.
	 */
	private Method m_hashCodeMethod;

	/**
	 * Equals method.
	 */
	private Method m_equalsMethod;

	/**
	 * toStirng method.
	 */
	private Method m_toStringMethod;

	/**
	 * Creates a DynamicProxyFactory.
	 * @param dependency TODO
	 */
	public ICasaProxyFactory(DeviceDependency dependency) {
		m_dependency = dependency;
		try {
			m_hashCodeMethod = Object.class.getMethod("hashCode", null);
			m_equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
			m_toStringMethod = Object.class.getMethod("toString", null);
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError(e.getMessage());
		}
	}

	/**
	 * Creates a proxy object for the given specification. The proxy uses the given dependency to get the service
	 * object.
	 * 
	 * @param spec the service specification (interface)
	 * @return the proxy object.
	 */
	public Object getProxy(Class spec) {
		return java.lang.reflect.Proxy.newProxyInstance(m_dependency.getHandler().getInstanceManager().getClazz().getClassLoader(),
		      new Class[] { spec }, this);
	}

	/**
	 * Invocation Handler delegating invocation on the service object.
	 * 
	 * @param proxy the proxy object
	 * @param method the method
	 * @param args the arguments
	 * @return a proxy object.
	 * @throws Exception if the invocation throws an exception
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Exception {

		Class declaringClass = method.getDeclaringClass();
		if (declaringClass == Object.class) {
			return delegateObjectMethodsInvokation(method, args);			
		}
		return delegateServiceInvokation(method, args);
	}
	

	private Object delegateObjectMethodsInvokation(Method method, Object[] args) throws Exception {		
		if (method.equals(m_hashCodeMethod)) {
			return getService().hashCode();
		} else if (method.equals(m_equalsMethod)) {
			try {
				// if is a iCasaProxy return true if the tow services are the same
				InvocationHandler invocationHandler = Proxy.getInvocationHandler(args[0]);
				if (invocationHandler instanceof ICasaProxyFactory) {
		         ICasaProxyFactory argumentProxyFactory = (ICasaProxyFactory) invocationHandler;
		         return getService().equals(argumentProxyFactory.getService());
	         }
         } catch (IllegalArgumentException e) {
	         // Nothing to do
         }
			return Boolean.FALSE;
		} else if (method.equals(m_toStringMethod)) {
			return this.toString();
		} else {
			throw new InternalError("Unexpected Object method dispatched: " + method);
		}
	}
	
	
	protected Object delegateServiceInvokation(Method method, Object[] args) throws Exception {
		Object service = getService();
		
		if (service==null) {
		    throw new IllegalStateException("No device injected yet");
		}
		
		String deviceId = ((GenericDevice) service).getSerialNumber();
		System.out.println("===============================> " + deviceId);
		AccessRight accessRight = m_dependency.getAccessRight(deviceId);
		
		if (accessRight != null) {
			if (accessRight.hasMethodAccess(method)) {
				System.out.println("================ Invoking iCasa Handler =============");
				return method.invoke(service, args);				
			} else {
				throw new AccessViolationException("Access Policy: No access to method " + method.getName());
			}
		} else {
			throw new AccessViolationException("Access Policy: No access right found to device " + deviceId);
		}
	}
	
	protected Object getService() {
		return m_dependency.getService();
	}

}