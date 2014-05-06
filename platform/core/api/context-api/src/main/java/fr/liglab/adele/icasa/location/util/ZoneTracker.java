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
package fr.liglab.adele.icasa.location.util;

import fr.liglab.adele.icasa.Constants;
import fr.liglab.adele.icasa.ContextManager;
import fr.liglab.adele.icasa.location.Position;
import fr.liglab.adele.icasa.location.Zone;
import fr.liglab.adele.icasa.location.ZoneListener;
import org.apache.felix.ipojo.util.Tracker;
import org.apache.felix.ipojo.util.TrackerCustomizer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 */
public class ZoneTracker implements ZoneTrackerCustomizer {

    protected static Logger logger = LoggerFactory.getLogger(Constants.ICASA_LOG);

    private ZoneTrackerCustomizer trackerCustomizer;

	private ZoneFilter zoneFilter;

	private ZoneFilter zoneCategoryFilter;

	private BundleContext context;
	/**
	 * The tracked zones: String zone identifier-> zone object.
	 */
	private Tracked tracked;

	/**
	 * Tracker of technical service used to listen zone events.
	 */
	protected Tracker contextMgrTracker;

	/**
	 * Creates a ZoneTracker object for the specified zone. Zone.
	 * 
	 * @param context
	 * @param customizer
	 * @param mandatoryVariables
	 */
	public ZoneTracker(BundleContext context, ZoneTrackerCustomizer customizer, final String... mandatoryVariables) {
		this.context = context;
		// use custom tracker, or this tracker customizer.
		this.trackerCustomizer = (customizer != null) ? customizer : this;
		this.zoneCategoryFilter = new DefaultTrueFilter();
		// use always matcher filter or filer with mandatory variables.
		this.zoneFilter = (mandatoryVariables.length == 0) ? new DefaultTrueFilter() : new MandatoryVariableFilter(
		      mandatoryVariables);
	}

	/**
	 * Opens this ZoneTracker object and begin tracking services.
	 * <p>
	 * Services which match the search criteria specified when this ZoneTracker object was created are now tracked by
	 * this ZoneTracker object.
	 */
	public synchronized void open() {
        logger.debug("[ZoneTracker] open");

        if (tracked != null) {
			return;
		}

		tracked = new Tracked();
		contextMgrTracker = new Tracker(context, ContextManager.class.getName(), new TrackerCustomizer() {
			@Override
			public boolean addingService(ServiceReference serviceReference) {
				return true;
			}

			@Override
			public void addedService(ServiceReference serviceReference) {
                logger.debug("[ZoneTracker] iCasa Context Manager added");
				ContextManager contextMgr = (ContextManager) context.getService(serviceReference);
				if (tracked == null)
					return;

				synchronized (tracked) {
					tracked.startTracking(contextMgr);
				}
				tracked.trackInitialZones();
			}

			@Override
			public void modifiedService(ServiceReference serviceReference, Object o) {
				// Nothing to do.
			}

			@Override
			public void removedService(ServiceReference serviceReference, Object o) {
                logger.debug("[ZoneTracker] iCasa Context Manager removed");
				if (tracked == null)
					return;

				synchronized (tracked) {
					tracked.stopTracking();
				}
			}
		});
		contextMgrTracker.open();
	}

	/**
	 * Closes this ZoneTracker object.
	 * <p>
	 * This method should be called when this ZoneTracker object should end the tracking of services.
	 */
	public synchronized void close() {
        logger.debug("[ZoneTracker] close");
		if (contextMgrTracker != null) {
			contextMgrTracker.close();
			contextMgrTracker = null;
		}

		if (tracked == null) {
			return;
		}

		tracked.close();
		tracked = null;

	}

	/**
	 * A zone is being added to the Tracker object. This method is called before a zone which matched the search
	 * parameters of the Tracker object is added to it. This method must return true to be tracked for this zone object.
	 * 
	 * @param zone the zone being added to the Tracker object.
	 * @return true if the zone will be tracked, false if not.
	 */
	@Override
	public boolean addingZone(Zone zone) {
		return true;
	}

	/**
	 * A zone tracked by the Tracker object has been added in the list. This method is called when a zone has been added
	 * in the managed list (after addingZone) and if the zone has not disappeared before during the callback.
	 * 
	 * @param zone the added zone
	 */
	@Override
	public void addedZone(Zone zone) {
        // do nothing
	}

	/**
	 * Called when a zone tracked by the Tracker object has been modified. A tracked zone is considered modified
	 * according to tracker configuration.
	 * 
	 * @param zone the changed zone
	 * @param variableName name of the variable that has changed
	 * @param oldValue previous value of the property
	 * @param newValue new value of the property
	 */
	@Override
	public void modifiedZone(Zone zone, String variableName, Object oldValue, Object newValue) {
        // do nothing
	}

	/**
	 * @param zone
	 * @param oldPosition
	 * @param newPosition
	 */
	@Override
	public void movedZone(Zone zone, Position oldPosition, Position newPosition) {
        // do nothing
	}

    @Override
    public void resizedZone(Zone zone) {
        // do nothing
    }

    /**
	 * A zone tracked by the Tracker object has been removed. This method is called after a zone is no longer being
	 * tracked by the Tracker object.
	 * 
	 * @param zone the removed zone.
	 */
	@Override
	public void removedZone(Zone zone) {
        // do nothing
	}

	/**
	 * Gets the list of stored zones.
	 * 
	 * @return the list containing zp,es
	 */
	public List<Zone> getZones() {
		Tracked tracked = this.tracked; // use local var since we are not synchronized
		if (tracked == null) { // if ZoneTracker is not open
			return null;
		}
		synchronized (tracked) {
			int length = tracked.size();
			if (length == 0) {
				return null;
			}
			List<Zone> references = new ArrayList<Zone>(length);
			references.addAll(tracked.values());
			// The resulting array is sorted by ranking.
			return references;
		}
	}

	/**
	 * Returns the number of zones being tracked by this ZoneTracker object.
	 * 
	 * @return the Number of zones being tracked.
	 */
	public int size() {
		Tracked tracked = this.tracked; // use local var since we are not synchronized
		if (tracked == null) { /* if ZoneTracker is not open */
			return 0;
		}
		return tracked.size();
	}

	class Tracked extends HashMap<String, Zone> {

		/**
		 * The list of ServiceReferences in the process of being added. This is used to deal with nesting of
		 * ServiceEvents. Since ServiceEvents are synchronously delivered, ServiceEvents can be nested. For example, when
		 * processing the adding of a service and the customizer causes the service to be unregistered, notification to
		 * the nested call to untrack that the service was unregistered can be made to the track method. Since the
		 * ArrayList implementation is not synchronized, all access to this list must be protected by the same
		 * synchronized object for thread safety.
		 */
		private List<Zone> adding;

		/**
		 * <code>true</code> if the tracked object is closed. This field is volatile because it is set by one thread and
		 * read by another.
		 */
		private volatile boolean closed;

		/**
		 * The Initial list of ServiceReferences for the tracker. This is used to correctly process the initial services
		 * which could become unregistered before they are tracked. This is necessary since the initial set of tracked
		 * services are not "announced" by ServiceEvents and therefore the ServiceEvent for unregistration could be
		 * delivered before we track the service. A service must not be in both the initial and adding lists at the same
		 * time. A service must be moved from the initial list to the adding list "atomically" before we begin tracking
		 * it. Since the LinkedList implementation is not synchronized, all access to this list must be protected by the
		 * same synchronized object for thread safety.
		 */
		private List<Zone> initial;

		/**
		 * Context Manager used to track zone events.
		 */
		private ContextManager contextManager;

		// private List<Zone> listenedZones = new ArrayList<Zone>();

		/**
		 * Tracked constructor.
		 */
		protected Tracked() {
			super();
			closed = false;
			adding = new ArrayList<Zone>(6);
			initial = new LinkedList<Zone>();
		}

		/**
		 * Sets initial list of services into tracker before ServiceEvents begin to be received. This method must be
		 * called from zoneTracker.open while synchronized on this object in the same synchronized block as the
		 * addServiceListener call.
		 * 
		 * @param zones The initial list of services to be tracked.
		 */
		protected void setInitialZones(Zone[] zones) {
			if (zones == null) {
				return;
			}
            for(Zone initialZone: zones){
                if(zoneFilter.match(initialZone)){
                    initial.add(initialZone);
                }
            }
		}

		/**
		 * Tracks the initial list of services. This is called after ServiceEvents can begin to be received. This method
		 * must be called from zoneTracker.open while not synchronized on this object after the addServiceListener call.
		 */
		protected void trackInitialZones() {
            logger.debug("[ZoneTracker] track initial zones");
			while (true) {
				Zone zone;
				synchronized (this) {
					if (initial.isEmpty()) { // if there are no more initial services
						return; // we are done
					}

					// move the first service from the initial list to the adding list within this synchronized block.
					zone = (Zone) ((LinkedList) initial).removeFirst();
					if (this.containsKey(zone.getId())) { // Check if the zone is already tracked.
						// if we are already tracking this service
						continue; /* skip this service */
					}
					if (adding.contains(zone)) {
						// if this service is already in the process of being added.
						continue; // skip this service
					}
					adding.add(zone);
				}
				trackAdding(zone); // Begin tracking it. We call trackAdding since we have already put the reference in the
										 // adding list.
			}
		}

		/**
		 * Called by the owning ZoneTracker object when it is closed.
		 */
		protected void close() {
			closed = true;
		}

		/**
		 * Begins to track the specified zone.
		 * 
		 * @param zone the zone to be tracked.
		 */
		protected void trackIfMatch(Zone zone) {
			boolean alreadyTracked = isTracked(zone);

			if (alreadyTracked) { // we are already tracking the zone
				// Call customizer outside of synchronized region

				return;
			}
			if (!zoneFilter.match(zone))
				return; // should not be tracked if mandatory properties do not exist

			synchronized (this) {
				if (adding.contains(zone)) { // if this service is already in the process of being added.
					return;
				}
				adding.add(zone); // mark this service is being added
			}

			trackAdding(zone); // call trackAdding now that we have put the zone in the adding list
		}

		/**
		 * Common logic to add a zone to the tracker used by track and trackInitialZones. The specified zone must have
		 * been placed in the adding list before calling this method.
		 * 
		 * @param zone the zone to be tracked.
		 */
		private void trackAdding(Zone zone) {
            logger.debug("[ZoneTracker] track zone " + zone.getId());
            boolean mustBeTracked = false;
			boolean becameUntracked = false;
			boolean mustCallAdded = false;
			// Call customizer outside of synchronized region
			try {
				mustBeTracked = trackerCustomizer.addingZone(zone);
			} finally {
				synchronized (this) {
					if (adding.remove(zone)) { // if the zone was not untracked during the customizer callback
						if (mustBeTracked) {
							this.put(zone.getId(), zone);
							mustCallAdded = true;
						}
					} else {
						becameUntracked = true;
						// If already get during the customizer callback
					}
				}
			}

			// Call customizer outside of synchronized region
			if (becameUntracked) {
				// The service became untracked during the customizer callback.
				trackerCustomizer.removedZone(zone);
			} else {
				if (mustCallAdded) {
					trackerCustomizer.addedZone(zone);
				}
			}
		}

		/**
		 * Discontinues tracking the zone. Do nothing if specified zone is not tracked.
		 * 
		 * @param zone the tracked zone.
		 */
		protected void untrack(Zone zone, boolean onlyIfNotMatch) {
            logger.debug("[ZoneTracker] untrack zone " + zone.getId());
            if (onlyIfNotMatch && zoneFilter.match(zone))
				return;

			synchronized (this) {
				if (initial.remove(zone)) { // if this zone is already in the list of initial references to process
					return; // we have removed it from the list and it will not be processed
				}

				if (adding.remove(zone)) { // if the zone is in the process of being added
					return; // in case the zone is untracked while in the process of adding
				}

				String zoneId = zone.getId();
				boolean isTraked = isTracked(zone); // Check if we was tracking the reference
				this.remove(zoneId); // must remove from tracker before calling customizer callback

				if (!isTraked)
					return;
			}
			// Call customizer outside of synchronized region and only if we are not closed
			if (!closed) {
				trackerCustomizer.removedZone(zone);
			}
			// If the customizer throws an unchecked exception, it is safe to let it propagate
		}

		public synchronized void startTracking(ContextManager contextMgr) {
			contextManager = contextMgr;
			contextManager.addListener(globalZoneListener);

			List<Zone> zones = contextManager.getZones();
			List<Zone> initialMatchingZones = new ArrayList<Zone>(zones);
			setInitialZones(initialMatchingZones.toArray(new Zone[initialMatchingZones.size()]));
		}

		public synchronized void stopTracking() {
			try {
				contextManager.removeListener(globalZoneListener);
			} catch (Exception e) {
				// ignore it
			}
            if(size()>0){
                for (Zone zone : getZones()) {
                    globalZoneListener.zoneRemoved(zone);
                }
            }
		}

		private synchronized boolean isTracked(Zone zone) {
			return this.containsKey(zone.getId());
		}

		/*
		 * Zone listener used to filter on zone variables
		 */
		private ZoneListener globalZoneListener = new EmptyZoneListener() {

			@Override
			public void zoneAdded(Zone zone) {
                logger.debug("[ZoneTrackerListener] zone added " + zone.getId());
                trackIfMatch(zone);
			}

			@Override
			public void zoneRemoved(Zone zone) {
                logger.debug("[ZoneTrackerListener] zone removed " + zone.getId());
				untrack(zone, false);
			}

			@Override
			public void zoneVariableAdded(Zone zone, String propertyName) {
                logger.debug("[ZoneTrackerListener] zone variable added " + zone.getId());
				trackIfMatch(zone);
			}

			@Override
			public void zoneVariableRemoved(Zone zone, String propertyName) {
                logger.debug("[ZoneTrackerListener] zone variable removed " + zone.getId());
				untrack(zone, true);
			}

			@Override
			public void zoneMoved(Zone zone, Position oldPosition, Position newPosition) {
				if (isTracked(zone)) {
					trackerCustomizer.movedZone(zone, oldPosition, newPosition);
				}
			}

            @Override
            public void zoneResized(Zone zone) {
                if (isTracked(zone)) {
                    trackerCustomizer.resizedZone(zone);
                }
            }

			@Override
			public void zoneVariableModified(Zone zone, String propertyName, Object oldValue, Object newValue) {
				if (isTracked(zone)) {
					trackerCustomizer.modifiedZone(zone, propertyName, oldValue, newValue);
				}
			}

		};
	}

	/**
	 * Zone filter which always match.
	 */
	class DefaultTrueFilter implements ZoneFilter {

		@Override
		public boolean match(Zone zone) {
			return true;
		}
	}

	/**
	 * ZoneFilter with mandatory properties.
	 */
	class MandatoryVariableFilter implements ZoneFilter {

		private String[] _mandatoryProperties;

		public MandatoryVariableFilter(String[] mandatoryProps) {
			_mandatoryProperties = mandatoryProps;
		}

		@Override
		public boolean match(Zone zone) {
			Set<String> variableNames = zone.getVariableNames();
			for (String prop : _mandatoryProperties) {
				if (!variableNames.contains(prop))
					return false;
			}
			return true;
		}
	}
}
