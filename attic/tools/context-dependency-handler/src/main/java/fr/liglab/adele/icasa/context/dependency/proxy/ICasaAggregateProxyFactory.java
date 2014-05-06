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
/**
 * 
 */
package fr.liglab.adele.icasa.context.dependency.proxy;

import fr.liglab.adele.icasa.context.dependency.ContextDependency;


/**
 *
 */
public class ICasaAggregateProxyFactory extends ICasaContextProxyFactory {
	
	private Object m_service;
	
	public ICasaAggregateProxyFactory(ContextDependency dependency, Object service) {
	   super(dependency);
	   m_service = service;
   }
	
	@Override
	protected Object getService() {
	   return m_service;
	}

}
