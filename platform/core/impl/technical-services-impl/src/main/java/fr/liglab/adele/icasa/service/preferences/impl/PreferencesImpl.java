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
/**
 * 
 */
package fr.liglab.adele.icasa.service.preferences.impl;

import fr.liglab.adele.icasa.service.preferences.PreferenceChangeListener;
import fr.liglab.adele.icasa.service.preferences.Preferences;
import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.sharedprefs.SharedPreferences;
import org.ow2.chameleon.sharedprefs.SharedPreferences.OnSharedPreferenceChangeListener;
import org.ow2.chameleon.sharedprefs.SharedPreferencesService;

import java.util.*;

/**
 * 
 */
@Component(name = "iCasaPreferences")
@Provides
@Instantiate
public class PreferencesImpl implements Preferences {

	private static final String GLOBAL_PREFIX = "Platform-";

	private static final String USER_PREFIX = "User-";

	private static final String APP_PREFIX = "App-";

	@Requires
	private SharedPreferencesService preferenceService;

    private OnSharedPreferenceChangeListener _globalPropListener;

    /* @GardedBy(_globalPropListeners) */
    private List<PreferenceChangeListener> _globalPropListeners;

    /* @GardedBy(_userPropListener) */
    private Map<String /* userId */, OnSharedPreferenceChangeListener> _userPropListener;

    /* @GardedBy(_userPropListener) */
    private Map<String /* userId */, List<PreferenceChangeListener>> _userPropListeners;

    /* @GardedBy(_appPropListener) */
    private Map<String /* application id */, OnSharedPreferenceChangeListener> _appPropListener;

    /* @GardedBy(_appPropListener) */
    private Map<String /* application id */, List<PreferenceChangeListener>> _appPropListeners;

    public PreferencesImpl() {
        _globalPropListeners = new ArrayList<PreferenceChangeListener>();

        _userPropListener = new HashMap<String, OnSharedPreferenceChangeListener>();
        _userPropListeners = new HashMap<String, List<PreferenceChangeListener>>();

        _appPropListener = new HashMap<String, OnSharedPreferenceChangeListener>();
        _appPropListeners = new HashMap<String, List<PreferenceChangeListener>>();
    }

    @Validate
    private void start() {
        SharedPreferences preferences = preferenceService.getSharedPreferences(GLOBAL_PREFIX);
        _globalPropListener = new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String propName) {
                synchronized (_globalPropListeners) {
                    for (PreferenceChangeListener listener : _globalPropListeners) {
                        try {
                            listener.changedProperty(propName, null, getGlobalPropertyValue(propName));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        preferences.registerOnSharedPreferenceChangeListener(_globalPropListener);
    }

    @Invalidate
    private void stop() {
        SharedPreferences preferences = preferenceService.getSharedPreferences(GLOBAL_PREFIX);
        if ((preferences != null) && (_globalPropListener != null)) {
            preferences.unregisterOnSharedPreferenceChangeListener(_globalPropListener);
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#getGlobalPropertyValue(java.lang.String)
	 */
	@Override
	public Object getGlobalPropertyValue(String name) {
		return getPropertyValue(name, GLOBAL_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#getUserPropertyValue(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Object getUserPropertyValue(String user, String name) {
		return getPropertyValue(name, USER_PREFIX + user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#getApplicationPropertyValue(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Object getApplicationPropertyValue(String applicationId, String name) {
		return getPropertyValue(name, APP_PREFIX + applicationId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#setGlobalPropertyValue(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void setGlobalPropertyValue(String name, Object value) {
		setPropertyValue(name, value, GLOBAL_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#setUserPropertyValue(java.lang.String,
	 * java.lang.String, java.lang.Object)
	 */
	@Override
	public void setUserPropertyValue(String user, String name, Object value) {
		setPropertyValue(name, value, USER_PREFIX + user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#setApplicationPropertyValue(java.lang.String,
	 * java.lang.String, java.lang.Object)
	 */
	@Override
	public void setApplicationPropertyValue(String applicationId, String name, Object value) {
		setPropertyValue(name, value, APP_PREFIX + applicationId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#getGlobalProperties()
	 */
	@Override
	public Set<String> getGlobalProperties() {
		return getPropertiesNames(GLOBAL_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#getUserProperties(java.lang.String)
	 */
	@Override
	public Set<String> getUserProperties(String user) {
		return getPropertiesNames(USER_PREFIX + user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.liglab.adele.icasa.service.preferences.Preferences#getApplicationProperties(java.lang.String)
	 */
	@Override
	public Set<String> getApplicationProperties(String applicationId) {
		return getPropertiesNames(APP_PREFIX + applicationId);
	}

    @Override
    public void addApplicationPreferenceChangeListener(String applicationId, PreferenceChangeListener listener) {
        createAppListenerIfNotExist(applicationId);
        synchronized (_appPropListener){
            List<PreferenceChangeListener> listeners = getAppPreferencesListeners(applicationId);
            listeners.add(listener);
        }
    }

    @Override
    public void removeApplicationPreferenceChangeListener(String applicationId, PreferenceChangeListener listener) {
        synchronized (_appPropListener){
            List<PreferenceChangeListener> listeners = getAppPreferencesListeners(applicationId);
            listeners.remove(listener);
        }
    }

    @Override
    public void addUserPreferenceChangeListener(String userId, PreferenceChangeListener listener) {
        createUserListenerIfNotExist(userId);
        synchronized (_userPropListeners){
            List<PreferenceChangeListener> listeners = getUserPreferencesListeners(userId);
            listeners.add(listener);
        }
    }

    @Override
    public void removeUserPreferenceChangeListener(String userId, PreferenceChangeListener listener) {
        synchronized (_userPropListeners){
            List<PreferenceChangeListener> listeners = getUserPreferencesListeners(userId);
            listeners.remove(listener);
        }
    }

    @Override
    public void addGlobalPreferenceChangeListener(PreferenceChangeListener listener) {
        synchronized(_globalPropListeners) {
            _globalPropListeners.add(listener);
        }
    }

    @Override
    public void removeGlobalPreferenceChangeListener(PreferenceChangeListener listener) {
        synchronized(_globalPropListeners) {
            _globalPropListeners.remove(listener);
        }
    }

    private Object getPropertyValue(String name, String storeName) {
		SharedPreferences preferences = getSharedPreferences(storeName);
		if (preferences.contains(name))
			return preferences.getAll().get(name);
		return null;
	}

	private Set<String> getPropertiesNames(String storeName) {
		SharedPreferences preferences = getSharedPreferences(storeName);
		Map<String, ?> preferencesMap = preferences.getAll();
		return preferencesMap.keySet();		
	}

    private SharedPreferences getSharedPreferences(String storeName){
        SharedPreferences preferences = preferenceService.getSharedPreferences(storeName);
        return preferences;
    }

    /**
     * Create a OnSharePreferenceChange listener for the user shared preference, if not exist.
     * @param userId
     */
    private void createUserListenerIfNotExist(final String userId) {
        OnSharedPreferenceChangeListener serviceListener =  _userPropListener.get(userId);
        if(serviceListener == null){
            serviceListener = new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    List<PreferenceChangeListener> listeners =  getUserPreferencesListeners(userId);
                    Object value = getUserPropertyValue(userId, key);
                    for (PreferenceChangeListener listener : listeners) {
                        try {
                            listener.changedProperty(key, null, value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            _userPropListener.put(userId, serviceListener);
            SharedPreferences preferences = preferenceService.getSharedPreferences(USER_PREFIX + userId);
            preferences.registerOnSharedPreferenceChangeListener(serviceListener);
        }
    }

    /**
     * Creates a OnSharePreferenceChange listener for the app if not exist.
     * @param appId
     */
    private void createAppListenerIfNotExist(final String appId) {
        OnSharedPreferenceChangeListener serviceListener =  _appPropListener.get(appId);
        if(serviceListener == null){
            serviceListener = new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    List<PreferenceChangeListener> listeners =  getAppPreferencesListeners(appId);
                    Object value = getApplicationPropertyValue(appId, key);
                    for (PreferenceChangeListener listener : listeners) {
                        try {
                            listener.changedProperty(key, null, value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            _appPropListener.put(appId, serviceListener);
            SharedPreferences preferences = preferenceService.getSharedPreferences( APP_PREFIX + appId);
            preferences.registerOnSharedPreferenceChangeListener(serviceListener);
        }
    }

    private List<PreferenceChangeListener> getUserPreferencesListeners(String userId){
        synchronized (_userPropListeners){
            List<PreferenceChangeListener> listeners = _userPropListeners.get(userId);
            if(listeners == null){
                listeners = new ArrayList<PreferenceChangeListener>();
                _userPropListeners.put(userId, listeners);
            }
            return listeners;
        }
    }

    private List<PreferenceChangeListener> getAppPreferencesListeners(String userId){
        synchronized (_appPropListeners){
            List<PreferenceChangeListener> listeners = _appPropListeners.get(userId);
            if(listeners == null){
                listeners = new ArrayList<PreferenceChangeListener>();
                _appPropListeners.put(userId, listeners);
            }
            return listeners;
        }
    }

	private void setPropertyValue(String name, Object value, String storeName) throws IllegalArgumentException{
		SharedPreferences preferences = getSharedPreferences(storeName);
		SharedPreferences.Editor editor = preferences.edit();

		if (value instanceof String) {
			String newValue = (String) value;
			editor.putString(name, newValue);
			editor.commit();
		} else if (value instanceof Boolean) {
			Boolean newValue = (Boolean) value;
			editor.putBoolean(name, newValue);
			editor.commit();
		} else if (value instanceof Float) {
			Float newValue = (Float) value;
			editor.putFloat(name, newValue);
			editor.commit();
		} else if (value instanceof Integer) {
			Integer newValue = (Integer) value;
			editor.putInt(name, newValue);
			editor.commit();
		} else if (value instanceof Long) {
			Long newValue = (Long) value;
			editor.putLong(name, newValue);
			editor.commit();
		} else {
            throw new IllegalArgumentException("Invalid value type. Supported types: String, Boolean, Float, Integer and Long");
        }
	}
}
