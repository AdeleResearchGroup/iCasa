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
package fr.liglab.adele.icasa.context.manager.impl;

import org.slf4j.LoggerFactory;

/**
 * TEMP
 * Classe qui détermine la meilleure configuration avec les buts et l'environnement actuel
 */
public final class ContextResolutionMachine implements Runnable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ContextResolutionMachine.class);

    private static int i =0;

    @Override
    public void run() {
        /*TODO Hard coder ???*/
        /*Attention aux multiples acces*/
        LOG.info("Execution " + i++);
    }
}
