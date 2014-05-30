/**
 * 
 */
package fr.liglab.adele.habits.monitoring.autonomic.manager.listeners;

import java.util.HashSet;
import java.util.Set;

/**
 * Informations related to deployment package.
 * 
 */
public class DPInfos {

	private String url;
	private String name;
	private Set<String> interfaces;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getInterfaces() {
		if (interfaces == null){
			interfaces = new HashSet<String>();
		}
		return interfaces;
	}

	public void setInterfaces(Set<String> interfaces) {
		this.interfaces = interfaces;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DPInfos other = (DPInfos) obj;
		if (interfaces == null) {
			if (other.interfaces != null)
				return false;
		} else if (!interfaces.equals(other.interfaces))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DPInfos [url=" + url + ", name=" + name + ", interfaces="
				+ interfaces + "]";
	}
	
}
