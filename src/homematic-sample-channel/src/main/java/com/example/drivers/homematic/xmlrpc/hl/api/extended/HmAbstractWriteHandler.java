package com.example.drivers.homematic.xmlrpc.hl.api.extended;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.ogema.model.devices.buildingtechnology.Thermostat;

/** Note: We assume that every device has at least one channel to read, so an AbstractWriteHandler
 * always extends ReadHandler
 * 
 * @author dnestle
 *
 * @param <T> parent resource type
 */
public abstract class HmAbstractWriteHandler<T extends Resource> extends HmAbstractReadHandler<T> {

    public HmAbstractWriteHandler(HomeMaticConnection conn) {
		super(conn);
	}
	
    /**Provide the ReadChannelDefinitions here*/
	protected abstract HmWriteChannel<T>[] getWriteChannels();
	
	@Override
	public void setup(HmDevice parent, DeviceDescription desc,
			Map<String, Map<String, ParameterDescription<?>>> paramSets) {
		super.setup(parent, desc, paramSets, this);
	}
	
	public void setupInternal(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets,
    		T thermos, Map<String, SingleValueResource> resources, final String deviceAddress) {
        super.setup(parent, desc, paramSets);
        
        for(HmWriteChannel<T> writeChan: getWriteChannels()) {
	    	SingleValueResource setpoint = writeChan.getResource(thermos);
	        setpoint.create();
	        thermos.activate(true);
	        
	        setpoint.addValueListener(new ResourceValueListener<SingleValueResource>() {
	            @Override
	            public void resourceChanged(SingleValueResource resource) {
	            	Object value;
	            	if(resource instanceof FloatResource)
		                //XXX fails without the String conversion...
	            		value = String.format(Locale.ENGLISH, "%.1f", writeChan.convertOutput(((FloatResource)resource).getValue()));
	            	else if(resource instanceof BooleanResource)
	            		value = new Boolean(writeChan.convertOutput(((BooleanResource)resource).getValue()?1:0)>0.5f);
	            	else if(resource instanceof IntegerResource)
	            		value = String.format(Locale.ENGLISH, "%d", writeChan.convertOutput((int)((IntegerResource)resource).getValue()));
	            	else if(resource instanceof StringResource)
	            		value =((StringResource)resource).getValue();
	            	else
	            		throw new IllegalStateException("Resource type of "+resource.getLocation()+" not supported!");
	                conn.performSetValue(deviceAddress, writeChan.getName(), value);
	            }            
	        }, true);
        }
    }
	
    public class ParameterListener implements ResourceValueListener<SingleValueResource> {
        
        final String address;

        public ParameterListener(String address) {
            this.address = address;
        }        

        @Override
        public void resourceChanged(SingleValueResource resource) {
            String paramName = resource.getName();
            
            Object resourceValue = null;
            if (resource instanceof IntegerResource) {
                resourceValue = ((IntegerResource) resource).getValue();
            } else {
                logger.warn("unsupported parameter type: " + resource);
            }
            
            Map<String, Object> parameterSet = new HashMap<>();
            parameterSet.put(paramName, resourceValue);
            conn.performPutParamset(address, "MASTER", parameterSet);
            logger.info("Parameter set 'MASTER' updated for {}: {}", address, parameterSet);
        }
        
    };
    
    protected <S extends SingleValueResource> void setupHmParameterValue(Thermostat thermos, String address,
    		String parameterName, Class<S> valueResourceType) {
        //XXX address mangling (parameters are set on device, not channel)
        if (address.lastIndexOf(":") != -1) {
            address = address.substring(0, address.lastIndexOf(":"));
        }
        @SuppressWarnings("unchecked")
        ResourceList<SingleValueResource> masterParameters = thermos.addDecorator("HmParametersMaster", ResourceList.class);
        if (!masterParameters.exists()) {
            masterParameters.setElementType(SingleValueResource.class);
            masterParameters.create();
        }
        S tf_modus = masterParameters.getSubResource(parameterName, valueResourceType);
        ParameterListener l = new ParameterListener(address);
        if (tf_modus.isActive()) { //send active parameter on startup
            l.resourceChanged(tf_modus);
        }
        tf_modus.addValueListener(l, true);
    }

}
