package com.example.app.evaluationofflinecontrol.gui.complex;

import org.ogema.serialization.jaxb.Resource;

import de.iwes.timeseries.eval.garo.api.base.GaRoSuperEvalResult;

public class GaRoSuperEvalResultDeser extends GaRoSuperEvalResult<Resource, GaRoMultiResultDeser> {
	//constructor for de-serialization
	public GaRoSuperEvalResultDeser() {
		super(null, 0, null);
	}

}
