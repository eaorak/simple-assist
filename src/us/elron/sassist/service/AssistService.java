package us.elron.sassist.service;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

import javassist.ClassPool;
import javassist.NotFoundException;
import us.elron.sassist.Advice;
import us.elron.sassist.IAdviceBuilder;
import us.elron.sassist.IAdviceListener;
import us.elron.sassist.IAssistService;

public class AssistService implements IAssistService {

    private final ClassPool      pool      = ClassPool.getDefault();
    private final Class<?>[]     imports   = new Class<?>[] { Logger.class, Arrays.class };
    private static String        LOG_LEVEL = "debug";

    private static AssistService INSTANCE;

    public static IAssistService initialize(String osgiPath) throws NotFoundException {
        if (INSTANCE == null) {
            synchronized (AssistService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AssistService(osgiPath);
                }
            }
        }
        return INSTANCE;
    }

    private AssistService(String osgiPath) throws NotFoundException {
        this.pool.appendClassPath(osgiPath + File.pathSeparator + "*");
        for (final Class<?> c : this.imports) {
            this.pool.importPackage(c.getPackage().getName());
        }
    }

    @Override
    public IAdviceBuilder createProxyFor(final Object target, final boolean extend) throws Exception {
        return new AdviceBuilder(this.pool, target, extend);
    }

    @Override
    public Object addLoggingTo(final IAdviceBuilder builder) throws Exception {
        //
        final StringBuilder startAdvice = new StringBuilder();
        startAdvice.append("long start = System.currentTimeMillis();").append(IAdviceBuilder.NLT);
        startAdvice.append("try {").append(IAdviceBuilder.NLT);
        //
        final StringBuilder catchAdvice = new StringBuilder();
        catchAdvice.append("} catch (Throwable e) {").append(IAdviceBuilder.NLT);
        catchAdvice.append("_$logger.error(\"Error ! :: \", e);").append(IAdviceBuilder.NLT);
        catchAdvice.append("throw e;");
        //
        final StringBuilder finallyAdvice = new StringBuilder();
        finallyAdvice.append("} finally {").append(IAdviceBuilder.NLT);
        log(finallyAdvice, "\"[Delta:\" + (System.currentTimeMillis() - start) + \"ms][Params:\"+Arrays.toString($args)+\"]\"");
        finallyAdvice.append("}").append(IAdviceBuilder.NLT);
        //
        return builder.addImport(Logger.class, Arrays.class)
                      .addField("private Logger _$logger = Logger.getLogger(" + builder.getTarget().getClass().getName() + ".class);")
                      .register(Advice.BEFORE, startAdvice.toString())
                      .register(Advice.AFTER, catchAdvice.toString(), new IAdviceListener() {
                          @Override
                          public boolean addFor(final Advice advice, final Method m, final String code) {
                              return !m.toString().contains("throws");
                          }
                      })
                      .register(Advice.AFTER, finallyAdvice.toString())
                      .generate();
    }

    private static void log(final StringBuilder body, final String code) {
        final String level = cap(LOG_LEVEL);
        body.append("if (_$logger.is" + level + "Enabled()) {").append(IAdviceBuilder.NLT);
        body.append("_$logger." + LOG_LEVEL + "(").append(code).append(");").append(IAdviceBuilder.NLT);
        body.append("}").append(IAdviceBuilder.NLT);
    }

    private static String cap(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
