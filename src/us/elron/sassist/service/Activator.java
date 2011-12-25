package us.elron.sassist.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import us.elron.sassist.IAssistService;

/**
 * @author Ender Aydin Orak
 * @see elron.us :)
 */
public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        IAssistService assistService = AssistService.getInstance();
        context.registerService(IAssistService.class.getName(), assistService, null);
    }

    public void stop(BundleContext context) throws Exception {
    }

}
