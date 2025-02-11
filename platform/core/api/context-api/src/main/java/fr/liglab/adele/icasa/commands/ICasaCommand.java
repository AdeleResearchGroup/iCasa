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
package fr.liglab.adele.icasa.commands;

import java.io.InputStream;
import java.io.PrintStream;

import fr.liglab.adele.icasa.commands.Signature;
import org.json.JSONObject;

/**
 * An ICasaCommand defines the interface of service providing iCasa commands that can be executed with a set of arguments.
  */
public interface ICasaCommand {
	/**
	 * Default value for PROP_COMMAND_NAMESPACE
	 */
	public static final String DEFAULT_NAMESPACE = "icasa";

	/**
	 * @serviceproperty The name of the command
	 * @mandatory
	 */
	public static final String PROP_NAME = "name";

	/**
	 * @serviceproperty The description of the command
	 * @optional
	 */
	public static final String PROP_DESCRIPTION = "description";

	/**
	 * Execute a block of code and then returns the result from execution. This method should use {@code in} and
	 * {@code out} stream to print result instead of the direct use of {@code System.in} and {@code System.out}
	 * 
	 * @param in : the input stream.
	 * @param out : the output stream.
	 * @param param a json object of parameters
	 * 
	 * @return the result from the execution
	 * @throws Exception if anything goes wrong
	 */
	Object execute(InputStream in, PrintStream out, JSONObject param) throws Exception;

	/**
	 * To validate the given parameters.
	 * 
	 * @param param The parameters in JSON format
	 * @return true if parameters are valid, false if not.
	 * @throws Exception
	 */
	boolean validate(JSONObject param, Signature signature) throws Exception;

	/**
	 * Get the command description.
	 * 
	 * @return The description of the Script and command gogo.
	 */
	String getDescription();

	/**
	 * Get the name of the Script and command gogo.
	 * 
	 * @return The command name.
	 */
	String getName();

	/**
	 * Get signature by passing the number of arguments..
	 * 
	 * @return
	 */
	Signature getSignature(int size);

}