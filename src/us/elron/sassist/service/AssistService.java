package us.elron.sassist.service;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import us.elron.sassist.IAdviceBuilder;
import us.elron.sassist.IAssistService;

public class AssistService implements IAssistService {

    private static final String  PROP_OSGI_PATH  = "osgi.install.area";
    private static final String  PROP_CLASS_PATH = "java.class.path";
    //
    private final ClassPool      pool            = ClassPool.getDefault();
    private final Class<?>[]     imports         = new Class<?>[] { Logger.class, Arrays.class, AssistService.class };

    private static AssistService INSTANCE;
    private static boolean       initialized     = false;

    public static IAssistService getInstance() throws Exception {
        if (!initialized) {
            initialize();
        }
        return INSTANCE;
    }

    private static IAssistService initialize() throws Exception {
        String path = System.getProperty(PROP_OSGI_PATH);
        if (path == null) {
            System.err.println("Warning: " + PROP_OSGI_PATH + " could not be found. Using classpath instead.");
            path = System.getProperty(PROP_CLASS_PATH);
        }
        return createInstance(path);
    }

    private static IAssistService createInstance(String path) throws Exception {
        if (INSTANCE == null) {
            synchronized (AssistService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AssistService(path);
                    initialized = true;
                }
            }
        }
        return INSTANCE;
    }

    private AssistService(String classPath) throws NotFoundException {
        this.pool.appendClassPath(classPath + File.pathSeparator + "*");
        for (final Class<?> c : this.imports) {
            this.pool.importPackage(c.getPackage().getName());
        }
        final LoaderClassPath loaderClassPath = new LoaderClassPath(this.getClass().getClassLoader());
        this.pool.appendClassPath(loaderClassPath);
    }

    @Override
    public IAdviceBuilder createProxyFor(final Object target, final boolean extend) throws Exception {
        return new AdviceBuilder(this.pool, target, extend);
    }

}
