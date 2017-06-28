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
package fr.liglab.adele.icasa.context.manager.api.old.generic;

import java.util.UUID;

public abstract class Resource {
    private UUID id;
    private String importer;
    /*What type of context element this is*/
    /*TODO CHANGE TO FIT CONTEXT, EXAMPLE: HOW TO SPECIFY THAT THIS IS A LOCALISABLE ZIGBEE LAMP*/
    /*TODO OTHER EXAMPLE: RELATION "IS IN"*/
    private String type;

    Resource(String importer, String type){
        this.id = UUID.randomUUID();
        this.importer = importer;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }
    public String getImporter() {
        return importer;
    }
    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationResource)) return false;

        RelationResource resource = (RelationResource) o;

        return getId().equals(resource.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", importer='" + importer + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
