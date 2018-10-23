package org.eclipse.om2m.sample.echonet.models;

import org.eclipse.om2m.sdt.Domain;
import org.eclipse.om2m.sdt.home.devices.Thermostat;
import org.eclipse.om2m.sdt.home.modules.RunMode;
import org.osgi.framework.BundleContext;

public class TemperatureSensor extends Thermostat{
	public TemperatureSensor(String id, String serial, Domain domain, BundleContext bundleContext) {
		super(id, serial, domain);
	}

}
