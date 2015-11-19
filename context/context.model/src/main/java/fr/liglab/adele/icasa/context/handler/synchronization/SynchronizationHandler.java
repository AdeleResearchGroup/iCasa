package fr.liglab.adele.icasa.context.handler.synchronization;

import org.apache.felix.ipojo.*;
import org.apache.felix.ipojo.architecture.HandlerDescription;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.apache.felix.ipojo.parser.FieldMetadata;
import org.apache.felix.ipojo.parser.MethodMetadata;
import org.apache.felix.ipojo.parser.ParseUtils;
import org.apache.felix.ipojo.parser.PojoMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.concurrent.ManagedScheduledExecutorService;
import org.wisdom.api.concurrent.ManagedScheduledFutureTask;

import java.lang.reflect.Member;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

//TODO : SYnchro on different map access
//TODO : More defensive programming when function of synchro is call
public class SynchronizationHandler extends PrimitiveHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SynchronizationHandler.class);

    public static final String SYNCHRONISATION_HANDLER_NAMESPACE = "fr.liglab.adele.icasa.context.handler.synchronization";

    ManagedScheduledExecutorService scheduler;

    private final SetStateMethodInterceptor m_setStateMethodInterceptor = new SetStateMethodInterceptor();

    private final GetStateMethodInterceptor m_getStateMethodInterceptor = new GetStateMethodInterceptor();

    private final PushStateMethodInterceptor m_pushStateMethodInterceptor = new PushStateMethodInterceptor();

    private final List<String> m_statesId = new ArrayList<>();

    private final Map<String,Function> m_setFunction = new HashMap<>();

    private final Map<String,ScheduledFunction> m_pullFunction = new HashMap<>();

    private final Map<String,Object> m_stateValue = new HashMap<>();

    private final Object m_stateLock = new Object();

    private ProvidedServiceHandler m_providedServiceHandler;

    private InstanceManager m_instanceManager;

    @Override
    public void configure(Element metadata, Dictionary configuration) throws ConfigurationException {
        m_instanceManager = getInstanceManager();
        PojoMetadata pojoMetadata = getPojoMetadata();

        MethodMetadata[] methodMetadatas = pojoMetadata.getMethods();

        for (MethodMetadata method : methodMetadatas){
            if (method.getMethodName().equals("setState")){
                m_instanceManager.register(method, m_setStateMethodInterceptor);
            }

            if (method.getMethodName().equals("pushState")){
                m_instanceManager.register(method, m_pushStateMethodInterceptor);
            }

            if (method.getMethodName().equals("getStateValue")){
                m_instanceManager.register(method,m_getStateMethodInterceptor);
            }
        }

        FieldMetadata injectedState = pojoMetadata.getField("injectedState");
        m_instanceManager.register(injectedState,this);

        Element[] stateElements = getInstanceManager().getFactory().getComponentMetadata().getElements("State", SYNCHRONISATION_HANDLER_NAMESPACE);
        String[] statesId =  ParseUtils.parseArrays(stateElements[0].getAttribute("states"));
        for (String stateId : statesId){
            m_statesId.add(stateId);
        }


    }

    public void onCreation(Object instance) {
        PojoMetadata pojoMetadata = getPojoMetadata();

        Element[] stateElements = getInstanceManager().getFactory().getComponentMetadata().getElements("State", SYNCHRONISATION_HANDLER_NAMESPACE);

        Element[] setElements = stateElements[0].getElements("Set", SYNCHRONISATION_HANDLER_NAMESPACE);
        if (setElements != null ) {
            for (Element element : setElements) {
                String stateId = element.getAttribute("state");
                FieldMetadata setFunctionField = pojoMetadata.getField(element.getAttribute("field"));
                Function setFunction = (Function) getInstanceManager().getFieldValue(setFunctionField.getFieldName());
                m_setFunction.put(stateId, setFunction);
            }
        }

        Element[] pullElements = stateElements[0].getElements("Pull", SYNCHRONISATION_HANDLER_NAMESPACE);
        if (pullElements !=null) {
            for (Element element : pullElements) {
                String stateId = element.getAttribute("state");
                if (stateId != null){

                    Long stateUpdateRate = -1L;

                    String updateRate = element.getAttribute("period");
                    if (updateRate != null) {
                        try {
                            stateUpdateRate = Long.valueOf(updateRate);
                        } catch (NumberFormatException e) {
                            LOG.error("update rate of " + stateId + "attribute isn't valid");
                        }
                    }

                    TimeUnit stateTimeUnit = TimeUnit.SECONDS;

                    String timeUnit = element.getAttribute("unit");
                    if (timeUnit != null) {
                        try {
                            stateTimeUnit = TimeUnit.valueOf(timeUnit);
                        }catch (IllegalArgumentException e){
                            LOG.error("Unit of" + stateId + "attribute isn't valid , use default instead");
                        }
                    }

                    FieldMetadata setFunctionField = pojoMetadata.getField(element.getAttribute("field"));
                    Function pullFunction = (Function) getInstanceManager().getFieldValue(setFunctionField.getFieldName());
                    ScheduledFunction scheduledFunction = new ScheduledPullFunctionImpl(pullFunction,stateUpdateRate,stateTimeUnit,stateId,this);
                    m_pullFunction.put(stateId, scheduledFunction);
                }

            }
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
    public void stateChanged(int state) {
        if (state == InstanceManager.VALID) {
            for (String stateId : m_statesId){
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
            for (String stateId : m_statesId){
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
                if (m_statesId.contains(stateId)) {
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
                LOG.error(" Cannot apply push for " + stateId + ", value is null ! ");
            }
        }
    }

    public Object onGet(Object pojo, String fieldName, Object value){
        return new HashMap<>(m_stateValue);
    }

    private class SetStateMethodInterceptor implements MethodInterceptor{

        @Override
        public void onEntry(Object pojo, Member method, Object[] args) {
            String stateId = (String)args[0];
            Object value = args[1];

            if(m_setFunction.containsKey(stateId)){
                Function setFunction = m_setFunction.get(stateId);
                setFunction.apply(value);
            }
        }

        @Override
        public void onExit(Object pojo, Member method, Object returnedObj) {

        }

        @Override
        public void onError(Object pojo, Member method, Throwable throwable) {

        }

        @Override
        public void onFinally(Object pojo, Member method) {

        }
    }

    private class GetStateMethodInterceptor implements MethodInterceptor{

        @Override
        public void onEntry(Object pojo, Member method, Object[] args) {
            String stateId = (String)args[0];

            if (m_statesId.contains(stateId)){
                if (m_pullFunction.containsKey(stateId)){
                    Function getFunction = m_pullFunction.get(stateId);
                    Object returnObj = getFunction.apply(stateId);
                    update(stateId,returnObj);
                }
            }
        }

        @Override
        public void onExit(Object pojo, Member method, Object returnedObj) {

        }

        @Override
        public void onError(Object pojo, Member method, Throwable throwable) {

        }

        @Override
        public void onFinally(Object pojo, Member method) {

        }
    }

    private class PushStateMethodInterceptor implements MethodInterceptor{

        @Override
        public void onEntry(Object pojo, Member method, Object[] args) {
            String stateId = (String)args[0];
            Object value = args[1];
            update(stateId,value);
        }

        @Override
        public void onExit(Object pojo, Member method, Object returnedObj) {

        }

        @Override
        public void onError(Object pojo, Member method, Throwable throwable) {

        }

        @Override
        public void onFinally(Object pojo, Member method) {

        }
    }

    @Override
    public HandlerDescription getDescription() {
        return new SynchronizationHandlerDescription(this);
    }

    private class SynchronizationHandlerDescription extends HandlerDescription {
        public SynchronizationHandlerDescription(PrimitiveHandler h) { super(h); }

        // Method returning the custom description of this handler.
        public Element getHandlerInfo() {
            // Needed to get the root description element.
            Element elem = super.getHandlerInfo();

            for (String stateId : m_statesId){
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
