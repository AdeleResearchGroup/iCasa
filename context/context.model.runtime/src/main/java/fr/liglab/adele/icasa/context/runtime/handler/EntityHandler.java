package fr.liglab.adele.icasa.context.runtime.handler;

import fr.liglab.adele.icasa.context.annotation.EntityType;
import fr.liglab.adele.icasa.context.ipojo.module.ApplyFieldVisitor;
import fr.liglab.adele.icasa.context.ipojo.module.ContextEntityVisitor;
import fr.liglab.adele.icasa.context.ipojo.module.PullFieldVisitor;
import fr.liglab.adele.icasa.context.ipojo.module.StateVariableFieldVisitor;
import fr.liglab.adele.icasa.context.model.ContextEntity;
import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.annotations.Handler;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceController;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Handler(name ="entity" ,namespace = "fr.liglab.adele.icasa.context.runtime.handler.EntityHandler")
@Provides(specifications = ContextEntity.class)
public class EntityHandler extends PrimitiveHandler implements ContextEntity  {

    @ServiceController(value=false, specification=ContextEntity.class)
    private boolean controller;

    /**
     * Component Management
     */
    private ProvidedServiceHandler m_providedServiceHandler;

    private InstanceManager m_instanceManager;

    private String m_componentName;


    /**
     * State Field
     */
    private final List<String> m_stateSpecifications = new ArrayList<>();

    private final Map<String,Function> m_setFunction = new HashMap<>();

    private final Map<String,String> m_setFunctionField = new HashMap<>();

    private final Map<String,ScheduledFunction> m_pullFunction = new HashMap<>();

    private final Map<String,ScheduledFunctionConfiguration> m_pullFunctionField = new HashMap<>();

    private final Map<String,String> m_pushMethod = new HashMap<>();

    private final Map<String,Object> m_stateValue = new HashMap<>();

    private final Object m_stateLock = new Object();

    /**
     * State Field Interceptor
     */
    private final StateFieldWithNoDirectAccessInterceptor m_stateFieldWithNoDirectAccessInterceptor = new StateFieldWithNoDirectAccessInterceptor();

    private final StateFieldWithDirectAccessInterceptor m_stateFieldWithDirectAccessInterceptor = new StateFieldWithDirectAccessInterceptor();

    /**
     * Wisdom Scheduler dependency
     */
    @Requires(specification = ManagedScheduledExecutorService.class,id="scheduler",proxy = false)
    public ManagedScheduledExecutorService scheduler;

    private void extractStateFromInterface(Class interfaz){
        Annotation[] entityTypeAnnotations = interfaz.getDeclaredAnnotationsByType(EntityType.class);
        for(Annotation entityTypeAnnotation : entityTypeAnnotations){
            EntityType cast = (EntityType)entityTypeAnnotation;
            String[] statesInEntityType = cast.states();
            //TODO : Test if state Variable are not already defined
            m_stateSpecifications.addAll(Arrays.asList(statesInEntityType));
        }
        for (Class superInterfaz : interfaz.getInterfaces()){
            extractStateFromInterface(superInterfaz);
        }
    }

    @Override
    public void configure(Element element, Dictionary dictionary) throws ConfigurationException {

        m_instanceManager = getInstanceManager();
        m_componentName = m_instanceManager.getInstanceName();

        /**
         * Check if dictionnary contains context entity id
         */
        if (dictionary.get(CONTEXT_ENTITY_ID) == null){
            throw new ConfigurationException("Try to instantiate a context entity without and context.entity.id element");
        }else {
            m_stateValue.put(CONTEXT_ENTITY_ID,dictionary.get(CONTEXT_ENTITY_ID));
        }
        /**
         * Introspect Interface Implemented by the component POJO and construct the
         * state specification of the entitytype ( basically a set of state variable)
         */
        Class clazz = getInstanceManager().getClazz();
        Class[] interfaces = clazz.getInterfaces();

        for(Class interfaz : interfaces){
            extractStateFromInterface(interfaz);
        }

        /**
         * Parse the manifest and compare if all the state variable declared in the specification is
         * referenced in the implementation.
         * Construct also the different map of function
         */
        Element[] entityElements = element.getElements(ContextEntityVisitor.CONTEXT_ENTITY_ELEMENT);

        if(entityElements != null) {
            for (String state : m_stateSpecifications) {
                for (Element entityElement : entityElements) {
                    Element[] stateVariableElements = entityElement.getElements(StateVariableFieldVisitor.STATE_VARIABLE_ELEMENT);
                    if (stateVariableElements != null) {
                        boolean findInEntity = false;
                        for (Element stateVariableElement : stateVariableElements) {
                            if(stateVariableElement.getAttribute(StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_NAME) != null) {
                                if (stateVariableElement.getAttribute(StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_NAME).equals(state)) {
                                    findInEntity = true;

                                    /**
                                     * No Direct Access Case : Register synchro Function
                                     */
                                    if (!Boolean.valueOf(stateVariableElement.getAttribute(StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_DIRECT_ACCESS))) {
                                        String pullField = stateVariableElement.getAttribute(PullFieldVisitor.STATE_VARIABLE_ATTRIBUTE_PULL);
                                        if (pullField != null) {
                                            /**
                                             * Defaut Config for the moment
                                             */
                                            m_pullFunctionField.put(state, new ScheduledFunctionConfiguration(pullField, -1L, TimeUnit.SECONDS));
                                        }

                                        String setField = stateVariableElement.getAttribute(ApplyFieldVisitor.STATE_VARIABLE_ATTRIBUTE_SET);
                                        if (setField != null) {
                                            m_setFunctionField.put(state, setField);
                                        }

                                        String pushMethod = stateVariableElement.getAttribute(ApplyFieldVisitor.STATE_VARIABLE_ATTRIBUTE_SET);
                                        if (pushMethod != null) {
                                            m_setFunctionField.put(state, pushMethod);
                                            MethodMetadata methodMetadata = getPojoMetadata().getMethod(stateVariableElement.getAttribute(StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_FIELD));
                                            m_instanceManager.register(methodMetadata, new PushMethodInterceptor(state));
                                        }

                                        /**
                                         * Add a field interceptor for No direct Access State Variable
                                         */
                                        m_instanceManager.register(getPojoMetadata().getField(stateVariableElement.getAttribute(StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_FIELD)), m_stateFieldWithNoDirectAccessInterceptor);
                                    } else {
                                        /**
                                         * Add a field interceptor for direct Access State Variable
                                         */
                                        m_instanceManager.register(getPojoMetadata().getField(stateVariableElement.getAttribute(StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_FIELD)), m_stateFieldWithDirectAccessInterceptor);

                                    }

                                    /**
                                     * Default Value Affectation
                                     */
                                    String defaultValue = stateVariableElement.getAttribute(StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_VALUE);

                                    if (defaultValue != null){
                                        /**
                                         * Init always with a string, TODO must introspect type and try to create the appropriate Value
                                         */
                                        m_stateValue.put(state,defaultValue);
                                    }
                                }
                            }else {
                                throw new ConfigurationException("Malformed Manifest : a " + StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_FIELD + " is declared with no " + StateVariableFieldVisitor.STATE_VARIABLE_ATTRIBUTE_NAME + " attribute");
                            }
                        }
                        if (!findInEntity) {
                            throw new ConfigurationException("State variable " + state + " is defined in entityType but never referenced in " + m_componentName);
                        }
                    }else {
                        throw new ConfigurationException("State variable " + state + " is defined in entityType but never referenced in " + m_componentName +" reason : no " + StateVariableFieldVisitor.STATE_VARIABLE_ELEMENT + " element in entity ");
                    }
                }
            }
        } else {
            throw new ConfigurationException("Entity Handler cannot be attached to a component with no " + ContextEntityVisitor.CONTEXT_ENTITY_ELEMENT + " element");
        }

        /**
         * Initialisation value are put in the buffer by default. Can change in future.
         */
        if (dictionary.get("context.entity.init") != null){
            Map<String,Object> initialStateValue = (Map<String, Object>) dictionary.get("context.entity.init");
            for (String key : initialStateValue.keySet()){
                if (m_stateSpecifications.contains(key)){
                    /**
                     * Maybe need to check if type is equal
                     */
                    m_stateValue.put(key,initialStateValue.get(key));
                }else{
                    warn(" State " + key +" is not defined in " + m_componentName + " , so it cannot be used to configured initial value");
                }

            }
        }
    }

    public void onCreation(Object instance) {

        for (String state: m_setFunctionField.keySet()){
            Function setFunction = (Function) m_instanceManager.getFieldValue(m_setFunctionField.get(state));
            m_setFunction.put(state, setFunction);
        }

        for (String state : m_pullFunctionField.keySet()) {
            ScheduledFunctionConfiguration config =  m_pullFunctionField.get(state);
            Function pullFunction = (Function) m_instanceManager.getFieldValue(config.getFieldName());
            ScheduledFunction scheduledFunction = new ScheduledPullFunctionImpl(pullFunction,config.getPeriod(),config.getUnit(),state,this);
            m_pullFunction.put(state, scheduledFunction);
        }

    }

    @Override
    public synchronized void stop() {
        m_providedServiceHandler = null;
    }

    @Override
    public synchronized void start() {
        m_providedServiceHandler = (ProvidedServiceHandler) getHandler(HandlerFactory.IPOJO_NAMESPACE + ":provides");

    }

    @Override
    public synchronized void stateChanged(int state) {

        /**
         * When component become Valid , Entity handler calls all the available pull function
         */
        if (state == InstanceManager.VALID) {

            /**
             * Managed Context Entity Service Exposition
             */
            controller = true;

            /**
             * Initialise with State Default Value
             */
            addStateServiceProperties(new Hashtable<>(m_stateValue));


            for (String stateId : m_stateSpecifications){
                if (m_pullFunction.containsKey(stateId)) {
                    ScheduledFunction getFunction = m_pullFunction.get(stateId);
                    Object returnObj = getFunction.apply(stateId);
                    update(stateId,returnObj);

                    if (getFunction.getPeriod() > 0){
                        ManagedScheduledFutureTask futur = scheduler.scheduleAtFixedRate(getFunction, getFunction.getPeriod(), getFunction.getPeriod(), getFunction.getUnit());
                        getFunction.submitted(futur);

                        /**   ManagedFutureTask.SuccessCallback<Object> onSucces =  (ManagedFutureTask<Object> var1, Object var2) -> {
                         LOG.info("On success called on  " + stateId + " with  " + var2);
                         if (var2 != null) {
                         synchronized (m_stateLock) {
                         if (var2.equals(m_stateValue.get(stateId))) {

                         } else {
                         m_stateValue.replace(stateId, var2);
                         updateState(stateId, var2);
                         }
                         }
                         } else {
                         LOG.error("Pull fonction " + stateId + " return null Object ! ");
                         }
                         };
                         futur.onSuccess(onSucces);
                         **/
                    }

                }
            }
        }

        if (state == InstanceManager.INVALID) {

            /**
             * Managed Context Entity Service Exposition
             */
            controller = false;

            for (String stateId : m_stateSpecifications){
                if (m_pullFunction.containsKey(stateId)) {
                    ScheduledFunction getFunction = m_pullFunction.get(stateId);
                    if (getFunction.getPeriod() > 0){
                        getFunction.task().cancel(true);
                        getFunction.submitted(null);
                    }
                }
            }
        }
    }

    /**
     * Management of Property exposed by the service
     *
     */

    private void addStateServiceProperties(Dictionary<String,Object> properties){
        if (m_providedServiceHandler != null){
            m_providedServiceHandler.addProperties(properties);
        }
    }
    private void addStateServiceProperty(String propertyId,Object value){
        Hashtable<String,Object> hashtable = new Hashtable();
        hashtable.put(propertyId, value);
        if (m_providedServiceHandler != null){
            m_providedServiceHandler.addProperties(hashtable);
        }
    }

    private void updateStateServiceProperty(String propertyId,Object value){
        Hashtable<String,Object> hashtable = new Hashtable();
        hashtable.put(propertyId, value);
        if (m_providedServiceHandler != null){
            m_providedServiceHandler.reconfigure(hashtable);
        }
    }

    public void update(String stateId,Object value){
        if (stateId != null) {
            if (value != null) {
                if (m_stateSpecifications.contains(stateId)) {
                    synchronized (m_stateLock) {
                        if (m_stateValue.containsKey(stateId)) {
                            if (!value.equals(m_stateValue.get(stateId))) {
                                m_stateValue.put(stateId, value);
                                updateStateServiceProperty(stateId, value);
                            }
                        } else {
                            m_stateValue.put(stateId, value);
                            addStateServiceProperty(stateId, value);
                        }
                    }
                }
            } else {
                error(" Cannot apply push for " + stateId + ", value is null ! ");
            }
        }
    }

    /**
     * Push Method Interceptor
     */
    private class PushMethodInterceptor implements MethodInterceptor {

        private final String m_name;

        PushMethodInterceptor(String name){
            m_name = name;
        }

        @Override
        public void onEntry(Object pojo, Member method, Object[] args) {

        }

        @Override
        public void onExit(Object pojo, Member method, Object returnedObj) {
            if (returnedObj != null){
                synchronized (m_stateLock) {
                    update(m_name,returnedObj);
                }
            }
        }

        @Override
        public void onError(Object pojo, Member method, Throwable throwable) {

        }

        @Override
        public void onFinally(Object pojo, Member method) {

        }
    }

    /**
     * State Field With No Direct Access Interceptor
     */
    private class StateFieldWithNoDirectAccessInterceptor implements FieldInterceptor {

        @Override
        public void onSet(Object pojo, String fieldName, Object value) {

            if(m_setFunction.containsKey(fieldName)){
                Function setFunction = m_setFunction.get(fieldName);
                setFunction.apply(value);
            }

        }

        @Override
        public Object onGet(Object pojo, String fieldName, Object value) {
            Object returnObj = null;
            if (m_stateSpecifications.contains(fieldName)){
                if (m_pullFunction.containsKey(fieldName)){
                    Function getFunction = m_pullFunction.get(fieldName);
                    returnObj = getFunction.apply(fieldName);
                    update(fieldName,returnObj);
                }

                /** Check if have a bufferised value in cas of null**/
                if (returnObj == null){
                    synchronized (m_stateLock) {
                        if (m_stateValue.containsKey(fieldName)) {
                            returnObj = m_stateValue.get(fieldName);
                        }
                    }
                }
            }
            return returnObj;
        }
    }

    /**
     * State Field With Direct Access Interceptor
     */
    private class StateFieldWithDirectAccessInterceptor implements FieldInterceptor {

        @Override
        public void onSet(Object pojo, String fieldName, Object value) {
            if (m_stateSpecifications.contains(fieldName)) {
                synchronized (m_stateValue) {
                    update(fieldName, value);
                }
            }
        }

        @Override
        public Object onGet(Object pojo, String fieldName, Object value) {
            Object returnObj = null;
            if (m_stateSpecifications.contains(fieldName)){
                /** Check if have a bufferised value in cas of null**/
                synchronized (m_stateLock) {
                    if (m_stateValue.containsKey(fieldName)) {
                        returnObj = m_stateValue.get(fieldName);
                    }
                }
            }
            return returnObj;
        }
    }

    /**
     * Utility class to create Scheduled Function
     */
    private class ScheduledFunctionConfiguration {
        private final String m_field;

        private final Long m_period;

        private final TimeUnit m_unit;

        ScheduledFunctionConfiguration(String field,Long period,TimeUnit unit){
            m_field = field;
            m_period = period;
            m_unit = unit;
        }

        String getFieldName(){
            return m_field;
        }

        Long getPeriod(){
            return m_period;
        }

        TimeUnit getUnit(){
            return m_unit;
        }
    }

    /**
     *
     * Context Entity Implementation
     *
     */
    @Override
    public String getId() {
        synchronized (m_stateLock){
            return (String) m_stateValue.get(CONTEXT_ENTITY_ID);
        }
    }

    @Override
    public Object getStateValue(String property) {
        if (property != null){
            synchronized (m_stateLock){
                return m_stateValue.get(property);
            }
        }
        return null;
    }

    @Override
    public Set<String> getStates() {
        return new HashSet<>(m_stateSpecifications);
    }


    /**
     * HANDLER DESCRIPTION
     */

    private class EntityHandlerDescription extends HandlerDescription {
        public EntityHandlerDescription(PrimitiveHandler h) { super(h); }

        // Method returning the custom description of this handler.
        public Element getHandlerInfo() {
            // Needed to get the root description element.
            Element elem = super.getHandlerInfo();

            for (String stateId : m_stateSpecifications){
                Element stateElement = new Element("State property","");
                stateElement.addAttribute(new Attribute("Name",stateId));
                if(m_pullFunction.containsKey(stateId)){
                    Function pull = m_pullFunction.get(stateId);
                    Object returnPull = pull.apply(stateId);
                    if (returnPull != null){
                        stateElement.addAttribute(new Attribute("Value",returnPull.toString()));
                        stateElement.addAttribute(new Attribute("Pull function","registered"));
                    }else{
                        stateElement.addAttribute(new Attribute("Value","Value return by Pull Function is null"));
                        stateElement.addAttribute(new Attribute("PullFunction","registered"));
                    }
                }
                else {
                    synchronized (m_stateLock) {
                        if (m_stateValue.containsKey(stateId)) {
                            stateElement.addAttribute(new Attribute("Value", m_stateValue.get(stateId).toString()));
                            stateElement.addAttribute(new Attribute("Pull function", "unregistered"));
                        } else {
                            stateElement.addAttribute(new Attribute("Value", "No Value"));
                            stateElement.addAttribute(new Attribute("Pull function", "unregistered"));
                        }
                    }
                }

                if (m_setFunction.containsKey(stateId)){
                    stateElement.addAttribute(new Attribute("Set function","registered"));
                }
                else {
                    stateElement.addAttribute(new Attribute("Set function","unregistered"));
                }

                elem.addElement(stateElement);

            }

            return elem;
        }
    }
}
