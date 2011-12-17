package us.elron.sassist.osgi;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import us.elron.sassist.IAssistService;
import us.elron.sassist.service.AssistService;

public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        IAssistService assistService = AssistService.initialize(this.getOsgiPath(context));
        context.registerService(IAssistService.class.getName(), assistService, null);
    }

    public void stop(BundleContext context) throws Exception {
    }

    private String getOsgiPath(BundleContext context) {
        return context.getBundle().getLocation() + File.pathSeparator + "..";
    }

}
