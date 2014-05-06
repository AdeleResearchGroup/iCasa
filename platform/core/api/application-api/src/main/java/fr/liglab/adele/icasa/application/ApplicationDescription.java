package fr.liglab.adele.icasa.application;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import java.util.Set;

/**
 *
 */
public interface ApplicationDescription {

    public String getId();

    public String getName();

    public String getVendor();

    public Version getVersion();

    public String getCategory();

    public Set<Bundle> getBundles();

}
