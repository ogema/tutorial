/**
 * This file is part of OGEMA.
 *
 * OGEMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OGEMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OGEMA. If not, see <http://www.gnu.org/licenses/>.
 */
package com.example.driver.homematic.thermostatextension;

import java.util.HashMap;
import java.util.Map;

import org.ogema.core.model.Resource;
import org.ogema.core.model.ResourceList;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.resourcemanager.ResourceStructureEvent;
import org.ogema.core.resourcemanager.ResourceStructureListener;
import org.ogema.core.resourcemanager.ResourceValueListener;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.drivers.homematic.xmlrpc.hl.types.HmDevice;
import org.ogema.drivers.homematic.xmlrpc.ll.api.DeviceDescription;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.ogema.model.sensors.TemperatureSensor;
import org.ogema.tools.resource.util.ResourceUtils;

import com.example.drivers.homematic.xmlrpc.hl.api.extended.HmAbstractWriteHandler;
import com.example.drivers.homematic.xmlrpc.hl.api.extended.HmReadChannel;
import com.example.drivers.homematic.xmlrpc.hl.api.extended.HmWriteChannel;

/**
 *
 * @author jlapp
 */
public class ThermostatChannel extends HmAbstractWriteHandler<Thermostat> {
    
    public static final String PARAM_TEMPERATUREFALL_MODUS = "TEMPERATUREFALL_MODUS";
    /**
     * Name ({@value}) of the decorator linking to the TempSens to be used instead
     * of the internal temperature sensor.
     */
    public static final String LINKED_TEMP_SENS_DECORATOR = "linkedTempSens";

    public ThermostatChannel(HomeMaticConnection conn) {
        super(conn);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    protected HmReadChannel<Thermostat>[] getReadChannels() {
    	return new HmReadChannel[] {new HmReadChannel<Thermostat>() {
	            @Override
	            public float convertInput(float v) {
	                return v + 273.15f;
	            }
	
	        	@Override
	        	public SingleValueResource getResource(Thermostat thermos) {
	        		return thermos.temperatureSensor().deviceFeedback().setpoint();
	        	}
	
				@Override
				public String getName() {
					return "SET_TEMPERATURE";
				}
			},
	    	new HmReadChannel<Thermostat>() {
	            @Override
	            public float convertInput(float v) {
	                return v + 273.15f;
	            }
	
	        	@Override
	        	public SingleValueResource getResource(Thermostat thermos) {
	        		return thermos.temperatureSensor().reading();
	        	}
				@Override
				public String getName() {
					return "ACTUAL_TEMPERATURE";
				}
			},
	    	new HmReadChannel<Thermostat>() {
	            @Override
	            public float convertInput(float v) {
	                return v / 100f;
	            }
	
	        	@Override
	        	public SingleValueResource getResource(Thermostat thermos) {
	        		return thermos.valve().setting().stateFeedback();
	        	}
				@Override
				public String getName() {
					return "VALVE_STATE";
				}
			},
	    	new HmReadChannel<Thermostat>() {
	        	@Override
	        	public SingleValueResource getResource(Thermostat thermos) {
	        		return thermos.battery().internalVoltage().reading();
	        	}
				@Override
				public String getName() {
					return "BATTERY_STATE";
				}
			}
    	};
    }

	@SuppressWarnings("unchecked")
	@Override
	protected HmWriteChannel<Thermostat>[] getWriteChannels() {
    	return new HmWriteChannel[] {new HmWriteChannel<Thermostat>() {
            @Override
            public float convertOutput(float v) {
                return v - 273.15f;
            }

        	@Override
        	public SingleValueResource getResource(Thermostat thermos) {
        		return thermos.temperatureSensor().settings().setpoint();
        	}

			@Override
			public String getName() {
				return "SET_TEMPERATURE";
			}
		}
    	};
	}

    @Override
    public boolean accept(DeviceDescription desc) {
        //System.out.println("parent type = " + desc.getParentType());
        return ("HM-CC-RT-DN".equalsIgnoreCase(desc.getParentType()) && "CLIMATECONTROL_RT_TRANSCEIVER".equalsIgnoreCase(desc.getType()))
                || "THERMALCONTROL_TRANSMIT".equalsIgnoreCase(desc.getType());
    }

    /**Here we do special things that are not supported by standard Read/WriteHandlers*/
    @Override
    public void setup(Thermostat thermos, Map<String, SingleValueResource> resources, final String deviceAddress,
    		HmDevice parent, DeviceDescription desc) {
        
        setupHmParameterValues(thermos, parent.address().getValue());
        setupTempSensLinking(thermos);
    }
    
    class ParameterListener implements ResourceValueListener<SingleValueResource> {
        
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
    
    private void setupHmParameterValues(Thermostat thermos, String address) {
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
        IntegerResource tf_modus = masterParameters.getSubResource(PARAM_TEMPERATUREFALL_MODUS, IntegerResource.class);
        ParameterListener l = new ParameterListener(address);
        if (tf_modus.isActive()) { //send active parameter on startup
            l.resourceChanged(tf_modus);
        }
        tf_modus.addValueListener(l, true);
    }
    
    private void linkTempSens(Thermostat thermos, TemperatureSensor tempSens) {
        HmDevice thermostatChannel = conn.findControllingDevice(thermos);
        if (thermostatChannel == null) {
            logger.error("cannot find HomeMatic channel for Thermostat {}", thermos);
            return;
        }
        HmDevice thermostatDevice = conn.getToplevelDevice(thermostatChannel);
        HmDevice tempSensChannel = conn.findControllingDevice(tempSens);
        if (tempSensChannel == null) {
            logger.warn("cannot find HomeMatic channel for TemperatureSensor {}", tempSens);
            return;
        }
        if (!tempSensChannel.type().getValue().startsWith("WEATHER")) {
            logger.warn(
                    "HomeMatic channel controlling TemperatureSensor {} is not a WEATHER channel (type is {}). Cannot link",
                    tempSens, tempSensChannel.type().getValue());
            return;
        }
        //XXX: address mangling (find WEATHER_RECEIVER channel instead?)
        String thermosAddress = thermostatDevice.address().getValue() + ":1";
        String weatherAddress = tempSensChannel.address().getValue();
        logger.info("HomeMatic weather channel for TempSens {}: {}", tempSens, weatherAddress);
        conn.performAddLink(weatherAddress, thermosAddress, "TempSens", "external temperature sensor");
    }
    
    private void setupTempSensLinking(final Thermostat thermos) {
        TemperatureSensor tempSens = thermos.getSubResource(LINKED_TEMP_SENS_DECORATOR, TemperatureSensor.class);

        ResourceStructureListener l = new ResourceStructureListener() {
            @Override
            public void resourceStructureChanged(ResourceStructureEvent event) {
                if (event.getType() == ResourceStructureEvent.EventType.SUBRESOURCE_ADDED) {
                    Resource added = event.getChangedResource();
                    if (added.getName().equals(LINKED_TEMP_SENS_DECORATOR) && added instanceof TemperatureSensor) {
                        linkTempSens(thermos, (TemperatureSensor) added);
                    }
                }

            }
        };
        thermos.addStructureListener(l);
        if (tempSens.isActive()) {
            linkTempSens(thermos, tempSens);
        }
        
    }

	@Override
	protected Thermostat getOrCreateParentResource(HmDevice parent, DeviceDescription desc) {
        String swName = ResourceUtils.getValidResourceName("THERMOSTAT" + desc.getAddress());
		return parent.addDecorator(swName, Thermostat.class);
	}
}
