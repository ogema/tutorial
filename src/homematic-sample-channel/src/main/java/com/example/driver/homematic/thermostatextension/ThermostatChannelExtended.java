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

import java.util.Arrays;
import java.util.Map;

import org.ogema.core.model.simple.BooleanResource;
import org.ogema.core.model.simple.IntegerResource;
import org.ogema.core.model.simple.SingleValueResource;
import org.ogema.core.model.units.TemperatureResource;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandler;
import org.ogema.drivers.homematic.xmlrpc.hl.api.DeviceHandlerFactory;
import org.ogema.drivers.homematic.xmlrpc.hl.api.HomeMaticConnection;
import org.ogema.model.devices.buildingtechnology.Thermostat;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import com.example.drivers.homematic.xmlrpc.hl.api.extended.HmReadChannel;
import com.example.drivers.homematic.xmlrpc.hl.api.extended.HmWriteChannel;

/**
 *
 * @author jlapp
 */
@Component(service = {DeviceHandlerFactory.class}, property = {Constants.SERVICE_RANKING + ":Integer=2"})
public class ThermostatChannelExtended extends ThermostatChannel implements DeviceHandlerFactory {

	
	enum PARAMS_WRITE_EXT {
		MANU_MODE
	}
	
	@Override
	/**Parameters for reading data from homematic*/
	protected HmReadChannel<Thermostat>[] getReadChannels() {
		HmReadChannel<Thermostat>[] orgRes = super.getReadChannels();
		HmReadChannel<Thermostat>[] result = Arrays.copyOf(orgRes, orgRes.length+3);
		result[orgRes.length] = new HmReadChannel<Thermostat>() {

			@Override
			public SingleValueResource getResource(Thermostat thermos) {
				return thermos.getSubResource("controlMode", IntegerResource.class);
			}

			@Override
			public String getName() {
				return "CONTROL_MODE";
			}
			
		};
		result[orgRes.length+1] = new HmReadChannel<Thermostat>() {

			@Override
			public SingleValueResource getResource(Thermostat thermos) {
				return thermos.getSubResource("faultReporting", IntegerResource.class);
			}

			@Override
			public String getName() {
				return "FAULT_REPORTING";
			}
			
		};
		result[orgRes.length+2] = new HmReadChannel<Thermostat>() {

			@Override
			public SingleValueResource getResource(Thermostat thermos) {
				return thermos.getSubResource("boostState", IntegerResource.class);
			}

			@Override
			public String getName() {
				return "BOOST_STATE";
			}
			
		};
		return result;
	}
	
	@Override
	protected HmWriteChannel<Thermostat>[] getWriteChannels() {
		HmWriteChannel<Thermostat>[] orgRes = super.getWriteChannels();
		HmWriteChannel<Thermostat>[] result = Arrays.copyOf(orgRes, orgRes.length+2);
		result[orgRes.length] = new HmWriteChannel<Thermostat>() {

			@Override
			public SingleValueResource getResource(Thermostat thermos) {
				return thermos.getSubResource("setManuMode", TemperatureResource.class);
			}

            @Override
            public float convertOutput(float v) {
                return v - 273.15f;
            }

            @Override
			public String getName() {
				return "MANU_MODE";
			}
			
		};
		result[orgRes.length+1] = new HmWriteChannel<Thermostat>() {

			@Override
			public SingleValueResource getResource(Thermostat thermos) {
				return thermos.getSubResource("setBoostMode", BooleanResource.class);
			}

			@Override
			public String getName() {
				return "BOOST_MODE";
			}
			
		};
		return result;
	}
	
    @Override
    public DeviceHandler createHandler(HomeMaticConnection connection) {
        return new ThermostatChannelExtended(connection);
    }
    
    public ThermostatChannelExtended() { //service factory constructor called by SCR
        super(null);
    }

    public ThermostatChannelExtended(HomeMaticConnection conn) {
        super(conn);
    }
    
    class WeatherEventListenerExt extends WeatherEventListener {
    	final Thermostat thermostat;
    	
		public WeatherEventListenerExt(Map<String, SingleValueResource> resources, String address,
				Thermostat thermostat) {
			super(resources, address);
			this.thermostat = thermostat;
		}
    }
}
