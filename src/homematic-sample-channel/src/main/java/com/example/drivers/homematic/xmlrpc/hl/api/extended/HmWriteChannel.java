package com.example.drivers.homematic.xmlrpc.hl.api.extended;

import org.ogema.core.model.Resource;
import org.ogema.core.model.simple.SingleValueResource;

public abstract class HmWriteChannel<T extends Resource> {

    public float convertOutput(float v) {
        return v;
    }
    
    public abstract SingleValueResource getResource(T thermos);
    public abstract String getName();
}
