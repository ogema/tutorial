package com.example.drivers.homematic.xmlrpc.hl.api.extended;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.FloatResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.simple.StringResource;
import org.ogema.drivers.homematic.xmlrpc.hl.api.AbstractDeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEvent;
import org.ogema.drivers.homematic.xmlrpc.ll.api.HmEventListener;
import org.ogema.drivers.homematic.xmlrpc.ll.api.ParameterDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * 
 * @author dnestle
 *
 * @param <T> parent resource type
 */
public abstract class HmAbstractReadHandler<T extends Resource> extends AbstractDeviceHandler {

    public Logger logger = LoggerFactory.getLogger(getClass());

    public HmAbstractReadHandler(HomeMaticConnection conn) {
		super(conn);
	}
	
    /**Provide the ReadChannelDefinitions here*/
	protected abstract HmReadChannel<T>[] getReadChannels();
	
	/**This is called during setup*/
	protected abstract T getOrCreateParentResource(HmDevice parent, DeviceDescription desc);
	protected abstract void setup(T thermo, Map<String, SingleValueResource> resources, final String deviceAddress,
			HmDevice parent, DeviceDescription desc);

    protected HmReadChannel<T> getParam(String name) {
        for (HmReadChannel<T> p: getReadChannels()) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
    	return null;
    }
    
    protected class WeatherEventListener implements HmEventListener {

        public final Map<String, SingleValueResource> resources;
        protected final String address;

        public WeatherEventListener(Map<String, SingleValueResource> resources, String address) {
            this.resources = resources;
            this.address = address;
        }
        
        @Override
        public void event(List<HmEvent> events) {
            for (HmEvent e: events) {
                if (!address.equals(e.getAddress())) {
                    continue;
                }
                SingleValueResource res = resources.get(e.getValueKey());
                if (res == null) {
                    continue;
                }
                HmReadChannel<T> p = getParam(e.getValueKey());
                if(p != null) {
                	if(res instanceof FloatResource)
                		((FloatResource) res).setValue(p.convertInput(e.getValueFloat()));
                	else if(res instanceof BooleanResource)
                		((BooleanResource) res).setValue((p.convertInput(e.getValueBoolean()?1:0) > 0.5));
                   	else if(res instanceof IntegerResource)
                		((IntegerResource) res).setValue((int)p.convertInput(e.getValueInt()));
                   	else if(res instanceof StringResource)
                		((StringResource) res).setValue(e.getValueString());
                    logger.debug("resource updated: {} = {}", res.getPath(), e.getValue());
                }
            }
        }

    }

    @Override
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets) {
    	setup(parent, desc, paramSets, null);
    }
    public void setup(HmDevice parent, DeviceDescription desc, Map<String, Map<String, ParameterDescription<?>>> paramSets,
    		HmAbstractWriteHandler<T> writeHandler) {
        final String deviceAddress = desc.getAddress();
        logger.debug("setup HM-read handler for address {} type {}", desc.getAddress(), desc.getType());
        Map<String, ParameterDescription<?>> values = paramSets.get(ParameterDescription.SET_TYPES.VALUES.name());
        if (values == null) {
            logger.warn("received no VALUES parameters for device {}", desc.getAddress());
            return;
        }

        T thermos = getOrCreateParentResource(parent, desc); //parent.addDecorator(swName, Thermostat.class);
        conn.registerControlledResource(conn.getChannel(parent, deviceAddress), thermos);
        Map<String, SingleValueResource> resources = new HashMap<>();
        for (Map.Entry<String, ParameterDescription<?>> e : values.entrySet()) {
            HmReadChannel<T> p = getParam(e.getKey());
            if(p == null) continue;
            SingleValueResource reading = p.getResource(thermos);
            if (!reading.exists()) {
                reading.create();
                thermos.activate(true);
            }
            logger.debug("found supported thermostat parameter {} on {}", e.getKey(), desc.getAddress());
            resources.put(e.getKey(), reading);
        }
        
        if(writeHandler != null) writeHandler.setupInternal(parent, desc, paramSets, thermos, resources, deviceAddress);
        
        conn.addEventListener(new WeatherEventListener(resources, desc.getAddress()));
        
        setup(thermos, resources, deviceAddress, parent, desc);
    }

}
