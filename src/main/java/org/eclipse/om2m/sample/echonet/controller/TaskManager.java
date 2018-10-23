package org.eclipse.om2m.sample.echonet.controller;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.sample.echonet.echonetObjects.EchonetLiteDevice;
import org.eclipse.om2m.sample.echonet.echonetObjects.NodeProfileObject;

import echowand.common.EOJ;
import echowand.monitor.Monitor;
import echowand.monitor.MonitorListener;
import echowand.net.Inet4Subnet;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.object.EchonetObjectException;
import echowand.service.Core;
import echowand.service.Service;

public class TaskManager {
	private static Log logger = LogFactory.getLog(TaskManager.class);
	private static Core core = null;
	public static Service service = null;
	private static NetworkInterface nif = null;
	private static ArrayList<EchonetLiteDevice> echonetDevices;

	
	public static void start() {
		echonetDevices = new ArrayList<EchonetLiteDevice>();
		initEchonetInterface();	
	}
	public static void stop() {
		try {
			core.stopService();
		} catch (SubnetException e) {
			logger.error("Can not stop the subnet: " + e.toString());
		}
	}
	
	private static void initEchonetInterface() {
		try {
			nif = NetworkInterface.getByName("vpn_ihouse");
		} catch (SocketException e) {
			logger.error("Can not init network interface: " + e.toString());
		}
		if(nif != null) {
			try {
				core = new Core(Inet4Subnet.startSubnet(nif));
			} catch (SubnetException e) {
				logger.error("Can not init subnet : " + e.toString());
			}
		}
		if(core != null) {
			service = new Service(core);
			Monitor monitor = new Monitor(core);
			monitor.addMonitorListener(new MonitorListener() {
				
				@Override
				public void detectEOJsJoined(Monitor monitor, Node node, List<EOJ> eojs) {
					logger.info("Echonet Interface: EOJ joined " + node + ":" +eojs);
					EchonetLiteDevice eDevice = new EchonetLiteDevice(node);
	                NodeProfileObject profile = null;
	                for(EOJ eoj :  eojs) {
	                	if(eoj.isProfileObject()) {
	                		profile = new NodeProfileObject(node, eoj);
	                		profile.ParseProfileObjectFromEPC(service);
	                		eDevice.setProfileObj(profile);
	                	} else if(eoj.isDeviceObject()) {
	                		try {
								eDevice.parseDataObject(eoj,node,service);
							} catch (EchonetObjectException e) {
								logger.error("Can not parse device object " + e.toString());
								}
	                		}
	                	}
	                echonetDevices.add(eDevice);              
	            }
				
				@Override
				public void detectEOJsExpired(Monitor monitor, Node node, List<EOJ> eojs) {
					logger.info("Echonet Interface: EOJ expired " + node + ":" +eojs);
				}
			});
		}
		
		
	}

}
