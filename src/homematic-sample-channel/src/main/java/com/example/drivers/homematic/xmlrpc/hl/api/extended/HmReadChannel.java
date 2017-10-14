package com.example.drivers.homematic.xmlrpc.hl.api.extended;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;

/**  Note: If a channel shall be read and written a HmReadChannel and a HmWriteChannel should
 * be set up.
 * 
 * @author dnestle
 *
 * @param <T>
 */
public abstract class HmReadChannel<T extends Resource> {
    public float convertInput(float v) {
        return v;
    }

    public abstract SingleValueResource getResource(T thermos);
    public abstract String getName();
}
