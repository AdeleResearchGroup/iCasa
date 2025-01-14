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
import java.util.ArrayList;
import java.util.List;

import fr.liglab.adele.icasa.Constants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCommand implements ICasaCommand {

    private static Logger log = LoggerFactory.getLogger(Constants.ICASA_LOG);

    protected static Signature EMPTY_SIGNATURE = new Signature(new String[0]);

    /**
     * Default namespace for iCasa commands
     */
    private static String NAMESPACE = "icasa";

    private List<Signature> signatureList = new ArrayList<Signature>(1);

	@Override
	public Object execute(InputStream in, PrintStream out, JSONObject param) throws Exception {
        Signature signature = getSignature(param.length());
		if (validate(param, signature)){
            return execute(in, out, param, signature);
        } else {
            out.println(getDescription());
            Exception ex = new Exception("Invalid parameters: " + param);
            log.error("Invalid parameters in command " +  getName(), ex);
            throw ex;
        }
	}

    protected void addSignature(Signature signature){
        for(Signature sign: signatureList){
            if(sign.getParameters().length == signature.getParameters().length){
                InstantiationError ex =  new InstantiationError("Unable to add two signature with the same number of parameters");
                log.error("Signature error in command " + getName(), ex);
                throw ex;
            }
        }
        signatureList.add(signature);

    }

    public Signature getSignature(int param){
        for (Signature signature: signatureList){
            if (signature.getParameters().length == param){
                return signature;
            }
        }
        return null;
    }

    /**
     * Get the command description.
     *
     * @return The description of the Script and command gogo.
     */
    @Override
    public String getDescription() {
        StringBuilder description = new StringBuilder("Parameters: \n");
        for(Signature sign: signatureList){
            String[] params = sign.getParameters();
            description.append("\t(");
            for (String param: params){
                description.append(" ");
                description.append(param);
                description.append(" ");
            }
            description.append(")\n");
        }
        return description.toString();
    }

    /**
     *
     * @param param The parameters in JSON format
     * @return true if all parameters are in the JSON object, false if not. For optional
     * parameters.
     * @throws Exception
     */
    @Override
	public boolean validate(JSONObject param, Signature signature) throws Exception {
   	 if (param==null || signature==null) {
   		 return false;
   	 }
        String[] params = signature.getParameters();
        for (String name: params){
            if(!param.has(name)){
                log.debug("Parameters values" + JSONObject.getNames(param));
                log.debug("Command call is invalid with the given parameters" + getName());
                return false;
            }
        }
        return true;// All params are in the Json object.
    }
	
	public abstract Object execute(InputStream in, PrintStream out, JSONObject param, Signature signature) throws Exception;
}
