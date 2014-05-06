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
package fr.liglab.adele.icasa;

/**
 * This class allows to describe a zone variable.
 *
 */
public class Variable {

    private String _name;
    private String _description;
    private Class _valueType;

    public Variable(String varName, Class valueType, String description) {
        _name = varName;
        _valueType = valueType;
        _description = description;
    }


    /**
     * Returns variable name.
     *
     * @return variable name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns variable description.
     *
     * @return variable description.
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Returns variable value type.
     *
     * @return variable value type.
     */
    public Class getValueType() {
        return _valueType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Variable variable = (Variable) o;

        if (!_name.equals(variable._name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return _name.hashCode();
    }
}
