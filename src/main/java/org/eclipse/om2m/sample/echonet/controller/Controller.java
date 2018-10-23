package org.eclipse.om2m.sample.echonet.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;

import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.object.EchonetObjectException;
import echowand.object.ObjectData;
import echowand.object.RemoteObject;
import echowand.service.Service;

public class Controller {
	private static Log logger = LogFactory.getLog(Controller.class);
	public static CseService CSE;
	public static Service service;
	protected static String AE_ID;
	
	public static void setCse(CseService cse){
		CSE = cse;
	}
	public static void setEchonetService(Service sv) {
		service = sv;
	}
	public synchronized boolean updateDeviceAttribute(String nodeIP, EOJ eoj, EPC epc, ObjectData value){
		logger.info(String.format("[UPDATE] Start updating device: IP: %s" + ", EOJ:%s, EPC:%s, Value: %s",
				nodeIP, eoj.toString(), epc.toString(), value.toString()));
		
		Node node = null;
		try {
			node = service.getRemoteNode(nodeIP);
		} catch (SubnetException e) {
			logger.error("Can not find node: " +e.toString());
		}
		service.registerRemoteEOJ(node, eoj);
        
        RemoteObject remoteObject = service.getRemoteObject(node, eoj);
		try {
			if (remoteObject.setData(epc, value)) {
				return true;
			}
		} catch (EchonetObjectException e) {
			logger.error("Can not find object: " +e.toString());
		}
		return false;
	}


}
