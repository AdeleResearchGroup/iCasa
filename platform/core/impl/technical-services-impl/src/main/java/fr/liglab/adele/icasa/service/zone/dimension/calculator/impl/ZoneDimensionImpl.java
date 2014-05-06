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
package fr.liglab.adele.icasa.service.zone.dimension.calculator.impl;

import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.TechnicalService;
import fr.liglab.adele.icasa.Variable;
import fr.liglab.adele.icasa.location.LocatedDevice;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.util.ZoneTracker;
import fr.liglab.adele.icasa.location.util.ZoneTrackerCustomizer;
import fr.liglab.adele.icasa.service.preferences.PreferenceChangeListener;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import fr.liglab.adele.icasa.service.zone.dimension.calculator.ZoneDimension;
import fr.liglab.adele.icasa.service.zone.size.calculator.ZoneSizeCalculator;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The Class ZoneDimensionImpl implements the Technical Service responsible for
 * providing each zone dimension.
 *
 */
@Component
@Instantiate
@Provides(specifications = {TechnicalService.class, ZoneDimension.class})
class ZoneDimensionImpl implements TechnicalService, ZoneDimension, ZoneTrackerCustomizer, PreferenceChangeListener {

	private final static Logger L = Logger.getLogger(ZoneDimension.class.getName());

	/**
	 * The array of zone variables computed by this technical service.
	 */
	private static final Variable[] COMPUTED_ZONE_VARIABLES = new Variable[] { ZONE_AREA_VAR,
	        ZONE_VOLUME_VAR };

	@Requires
	/**
	 * The context manager is provided by the simulator and allows to modify the different zones properties.
	 */
	private ContextManager m_contextManager;

    @Requires
    private ZoneSizeCalculator m_zoneSizeCalculator;

    @Requires
    private Preferences m_preferences;

	private ZoneTracker m_zoneTracker;

    private BundleContext m_bundleContext;

    public ZoneDimensionImpl(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.TechnicalService#getComputedGlobalVariables()
	 */
	@Override
	public Set<Variable> getComputedGlobalVariables() {
		return Collections.emptySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.TechnicalService#getComputedZoneVariables()
	 */
	@Override
	public Set<Variable> getComputedZoneVariables() {
		// convert the array of Computed variables to a set
		Set<Variable> computedVariables = new HashSet<Variable>();
		for (Variable computedVariable : COMPUTED_ZONE_VARIABLES) {
			computedVariables.add(computedVariable);
		}
		// return the converted result :
		return computedVariables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.TechnicalService#getRequiredGlobalVariables()
	 */
	@Override
	public Set<Variable> getRequiredGlobalVariables() {
		return Collections.emptySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.TechnicalService#getRequiredZoneVariables()
	 */
	@Override
	public Set<Variable> getRequiredZoneVariables() {
		return Collections.emptySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.TechnicalService#getUsedDevices()
	 */
	@Override
	public Set<LocatedDevice> getUsedDevices() {
		return Collections.emptySet();
	}

    /**
     * Update zone dimension properties (area, volume,...)
     *
     * @param zone
     *            : the zone to update.
     */
    private void updateZoneDimensionProperties(Zone zone) {

        assert (zone != null);

        // Gets the dimensions (convert from pixels to meters).
        final double x = zone.getXLength() * m_zoneSizeCalculator.getXScaleFactor();
        final double y = zone.getYLength() * m_zoneSizeCalculator.getYScaleFactor();
        final double z = zone.getZLength() * m_zoneSizeCalculator.getZScaleFactor();

        assert ((x > 0.0d) && (y > 0.0d) && (z > 0.0d)) : "negative dimensions !";


        // computes area and volume of the zone. We assume that the zone is
        // a rectangle (not a trapezoid).
        final double area = x * y;
        final double volume = area * z;


        if (L.isLoggable(Level.INFO)) {
            L.info(String.format("Update the zone %s area = %f m2 ; volume = %f m3",
                    zone.getId(), area, volume));
        }

        // set the different properties (area, volume, ...).
        m_contextManager.setZoneVariable(zone.getId(), ZONE_AREA, area);
        m_contextManager.setZoneVariable(zone.getId(), ZONE_VOLUME, volume);
    }

    @Override
    public boolean addingZone(Zone zone) {
        return true;
    }

    @Override
    public void addedZone(Zone zone) {
        // the zone is new => update the zone properties.
        updateZoneDimensionProperties(zone);
    }

    @Override
    public void modifiedZone(Zone zone, String variableName, Object oldValue, Object newValue) {
        // do nothing
    }

    @Override
    public void movedZone(Zone zone, Position oldPosition, Position newPosition) {
        // a zone move may change some variables (in the future, if we add
        // some coordinates to the variables) so update.
        updateZoneDimensionProperties(zone);
    }

    @Override
    public void resizedZone(Zone zone) {
        updateZoneDimensionProperties(zone);
    }

    @Override
    public void removedZone(Zone zone) {
        // I trust iCASA for removing all variables.
    }

	/**
	 * Start the technical service.
	 * 
	 * @throws Throwable
	 */
	@Validate
	private void start() {
		L.info("The dimension service is starting");
		// add the listener responsible for updating dimension.

        m_zoneTracker = new ZoneTracker(m_bundleContext, this);
        m_zoneTracker.open();

        m_preferences.addGlobalPreferenceChangeListener(this);
	}

	/**
	 * Stop the technical service.
	 */
	@Invalidate
	private void stop() {
		L.info("The dimension service is stopping");

        m_preferences.removeGlobalPreferenceChangeListener(this);

		// remove the listener responsible for updating dimension.
        if (m_zoneTracker != null) {
            m_zoneTracker.close();
            m_zoneTracker = null;
        }

        // do not remove area and volume props as we do not know if another service modifies it
	}

    @Override
    public void changedProperty(String propertyName, Object oldValue, Object newValue) {
        if (!ZoneSizeCalculator.X_SCALE_FACTOR_PROP_NAME.equals(propertyName) &&
                !ZoneSizeCalculator.Y_SCALE_FACTOR_PROP_NAME.equals(propertyName) &&
                !ZoneSizeCalculator.Z_SCALE_FACTOR_PROP_NAME.equals(propertyName))
            return; // this change has no impact on computation

        if (m_zoneTracker == null || m_zoneTracker.getZones() == null)
            return;

        for (Zone zone : m_zoneTracker.getZones())
            updateZoneDimensionProperties(zone);
    }
}
