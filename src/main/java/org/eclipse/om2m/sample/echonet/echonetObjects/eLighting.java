package org.eclipse.om2m.sample.echonet.echonetObjects;


import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.sample.echonet.Activator;
import org.eclipse.om2m.sample.echonet.controller.TaskManager;
import org.eclipse.om2m.sample.echonet.utils.EchonetDataConverter;
import org.eclipse.om2m.sdt.datapoints.BooleanDataPoint;
import org.eclipse.om2m.sdt.datapoints.IntegerDataPoint;
import org.eclipse.om2m.sdt.exceptions.DataPointException;
import org.eclipse.om2m.sdt.home.devices.Light;
import org.eclipse.om2m.sdt.home.driver.Utils;
import org.eclipse.om2m.sdt.home.modules.BinarySwitch;
import org.eclipse.om2m.sdt.home.modules.Brightness;
import org.eclipse.om2m.sdt.home.types.DatapointType;
import org.osgi.framework.ServiceRegistration;

import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.net.Node;
import echowand.net.SubnetException;
import echowand.object.EchonetObjectException;
import echowand.object.ObjectData;
import echowand.object.RemoteObject;
import echowand.service.Service;
import echowand.service.result.GetListener;
import echowand.service.result.GetResult;
import echowand.service.result.ResultData;
import echowand.service.result.ResultFrame;

public class eLighting extends eDataObject{
	private static Log logger = LogFactory.getLog(eLighting.class);
	private List<ServiceRegistration> serviceRegistrations;
	
	public Light lightSDT;
	private int illuminateLevel;
	private EOJ eoj;
	private Node node;
	private boolean operationStatus;
	Timer timer;
	
	
	public int getIlluminateLevel() {
		return illuminateLevel;
	}
	public void setIlluminateLevel(int illuminateLevel) {
		if(this.getIlluminateLevel() != illuminateLevel) {
			this.illuminateLevel = illuminateLevel;
			
		}
	}
	
	public boolean isOperationStatus() {
		return operationStatus;
	}
	public void setOperationStatus(boolean operationStatus) {
		if(operationStatus != isOperationStatus()) {
			this.operationStatus = operationStatus;
		}
	}
	public eLighting() {
		super();
		this.groupCode= (byte)0x02;
		this.classCode=(byte)0x90;
	}
	public eLighting(EOJ eoj, Node node) {
		super(node,eoj);
		this.groupCode= (byte) 0x02;
		this.classCode = (byte) 0x90;
		this.instanceCode = eoj.getInstanceCode();
		this.eoj = eoj;
		this.node = node;
		lightSDT = new Light(node.getNodeInfo().toString()+ "_"+this.instanceCode, "SERIAL_" +getManufacturerCode() , Activator.domain);
		lightSDT.addModule(new BinarySwitch("binarySwitch", Activator.domain, new BooleanDataPoint(DatapointType.powerState) {

			@Override
			protected Boolean doGetValue() throws DataPointException {
				return new Boolean(operationStatus);
			}

			@Override
			protected void doSetValue(Boolean value) throws DataPointException {
				if(value.booleanValue() == operationStatus) {
					logger.info("Status is the same! ..Nothing to do");
				} else {
					ObjectData data = null;
					if(value.booleanValue() == true) {
						data = new ObjectData((byte) 0x30);
					} else {
						data = new ObjectData((byte) 0x31);
					}
					if(executeCommand(EPC.x80,data )) {
						setOperationStatus(value.booleanValue());
					} else {
						logger.error("Can not set status!!");
					}
				}
				
			}		
		}));
		
		lightSDT.addModule(new Brightness("brighness", Activator.domain, new IntegerDataPoint(DatapointType.brightness) {
			
			@Override
			protected Integer doGetValue() throws DataPointException {
				return new Integer(illuminateLevel);
			}
			@Override
			protected void doSetValue(Integer value) throws DataPointException {
				if(value != null) {
					ObjectData data = new ObjectData(value.byteValue());
					if(executeCommand(EPC.xB0,data )) {
						setIlluminateLevel(value.intValue());
					} else {
						logger.error("Can not set status!!");
					}
				}
			}
			
		}));
		
		lightSDT.setCountry("Japan");
		lightSDT.setDeviceManufacturer(getManufacturerCode());
		lightSDT.setDeviceFirmwareVersion(getStandardVersionInfo());
		lightSDT.setDeviceName("Lighting Device");
		lightSDT.setDeviceModelName(getIdentificationNumber());
		lightSDT.setDateOfManufacture(getProductDate());
		lightSDT.setLocation(getInstallLocation());
		lightSDT.setSystemTime(getCurrentDateSetting());
		
		if (! ((serviceRegistrations == null) || serviceRegistrations.isEmpty())) {
			return;
		}
		serviceRegistrations = Utils.register(lightSDT, Activator.context);
	}
	
	@Override
	public String ToString() {
		StringBuilder rs = new StringBuilder();
		rs.append("Operation Status: " + (this.isOperationStatus() ? "ON" : "OFF"));
		rs.append("Illuminance level: " + this.illuminateLevel + " %");
		return null;
	}
	public void getData(Service service){
		LinkedList<EPC> epcs = new LinkedList<EPC>();
		epcs.add(EPC.x80);
		epcs.add(EPC.xB0);
		try {
			service.doGet(node, eoj, epcs, 5000, new GetListener() {
				@Override
			    public void receive(GetResult result, ResultFrame resultFrame, ResultData resultData) {
					if (resultData.isEmpty()) {
						return;
					}
					switch (resultData.getEPC()) 
					{
					case x80:
						if(EchonetDataConverter.dataToInteger(resultData) == 48) {
							setOperationStatus(true);
						} else {
							setOperationStatus(false);
						}
						logger.info(String.format("Lighting:%s {EPC:0x80, EDT: 0x%02X}=={OperationStatus:%s}",
								 getNode().getNodeInfo().toString(),resultData.toBytes()[0],isOperationStatus()));	
						break;
					case xB0:
						int level = EchonetDataConverter.dataToInteger(resultData);
						setIlluminateLevel(level);
						logger.info(String.format("Lighting:%s {EPC:0xB0, EDT: 0x%02X}=={Illumination Level = :%d}",
								 getNode().getNodeInfo().toString(),resultData.toBytes()[0],getIlluminateLevel()));	
						break;
						
					default:
						logger.error("Something happended when loading lighting device!!");
						break;
					}	
				}	
			});
		} catch (SubnetException e) {
			e.printStackTrace();
		}
	}
	public boolean executeCommand(EPC epc, ObjectData data) {
		boolean rs = false;
		TaskManager.service.registerRemoteEOJ(this.node, this.eoj);
		RemoteObject remoteObject = TaskManager.service.getRemoteObject(node, eoj);
		try {
			if (remoteObject.setData(epc, data)) {
				rs= true;
			}
		} catch (EchonetObjectException e) {
			logger.error("Can not find object: " +e.toString());
			rs= false;
		}
		return rs;
	}
	public void unregisterDevice() {
		if (serviceRegistrations == null)
			return;
		for (ServiceRegistration reg : serviceRegistrations) {
			reg.unregister();
		}
		serviceRegistrations.clear();
	}
	@Override
	public void ParseDataFromEOJ(Service service){
		timer = new Timer(true);
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				getData(service);
			}
		}, 1000, 10000);	
	}

}
