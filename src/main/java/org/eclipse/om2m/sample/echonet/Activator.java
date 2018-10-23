package org.eclipse.om2m.sample.echonet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.om2m.core.service.CseService;
import org.eclipse.om2m.interworking.service.InterworkingService;
import org.eclipse.om2m.sample.echonet.controller.Controller;
import org.eclipse.om2m.sample.echonet.controller.TaskManager;
import org.eclipse.om2m.sdt.Domain;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class Activator  implements BundleActivator{
	 private static Log logger = LogFactory.getLog(Activator.class);
	 private ServiceTracker<Object, Object> cseServiceTracker;
	 public static Domain domain;
	 public static BundleContext context;
	 

	@Override
	public void start(BundleContext bundleContext) throws Exception {
        logger.info("Register ECHONET Service..");
        bundleContext.registerService(InterworkingService.class.getName(),new RequestRouter(), null);
        context = bundleContext;
		domain = new Domain("ECHONET Lite");
        logger.info("ECHONET Service is registered.");
        
        cseServiceTracker = new ServiceTracker<Object, Object>(bundleContext, CseService.class.getName(), null) {
            public void removedService(ServiceReference<Object> reference, Object service) {
                logger.info("CseService removed");
            }

            public Object addingService(ServiceReference<Object> reference) {
                logger.info("CseService discovered");
                CseService cseService = (CseService) this.context.getService(reference);
                Controller.setCse(cseService);
                new Thread(){
                    public void run(){
                        try {
                        	TaskManager.start();
                        } catch (Exception e) {
                            logger.error("IpeMonitor Sample error", e);
                        }
                    }
                }.start();
                return cseService;
            }
        };
        cseServiceTracker.open();
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		logger.info("Stop the Echonet Service GateWay");
        try {
        	TaskManager.stop();
        } catch (Exception e) {
            logger.error("Stop the Echonet Service GateWay error", e);
        }
	}

}
