package us.elron.sassist.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import javassist.compiler.Javac.CtFieldWithInit;
import us.elron.sassist.Advice;
import us.elron.sassist.IAdviceBuilder;
import us.elron.sassist.IAdviceListener;

class MethodCode {

    String signature;
    String body;

    String complete() {
        return this.signature + this.body;
    }

    @Override
    public String toString() {
        return this.complete();
    }

}

/**
 * @author Ender Aydin Orak
 * @see elron.us :)
 */
public class AdviceBuilder implements IAdviceBuilder {

    // Variable declarations
    private static String                       TARGET      = "_$_target";
    private static String                       RESULT      = "_$_result";
    private static String                       AROUND      = "_$_around";
    // Around specific tags
    private static String                       AROUND_FLAG = "//#AROUND_FLAG#";
    private static String                       AROUND_USED = AROUND + " = true;";
    //
    private static String                       CLS_SUFFIX  = "$_";
    //
    private final ClassPool                     pool;
    private final CtClass                       ctClass;
    private final Map<Advice, List<AdviceInfo>> adviceMap   = new HashMap<Advice, List<AdviceInfo>>();

    private final Set<Method>                   methodList  = new HashSet<Method>();
    private final ClassLoader                   loader;
    private final Object                        target;
    private final boolean                       extend;

    public AdviceBuilder(final ClassPool pool, final Object target, final boolean extend) throws Exception {
        this.pool = pool;
        final LoaderClassPath loaderClassPath = new LoaderClassPath(target.getClass().getClassLoader());
        pool.appendClassPath(loaderClassPath);
        //
        this.target = target;
        this.extend = extend;
        this.loader = this.getClass().getClassLoader();
        this.ctClass = this.createProxy(target.getClass(), extend);
        this.prepareProxy(target, extend);
        for (final Advice adv : Advice.values()) {
            this.adviceMap.put(adv, new ArrayList<AdviceInfo>());
        }
    }

    private CtClass createProxy(final Class<?> target, final boolean extend) throws Exception {
        CtClass ctTarget = null;
        if (extend) {
            ctTarget = this.pool.makeClass(target.getName() + CLS_SUFFIX, this.pool.get(target.getName()));
        } else {
            ctTarget = this.pool.makeClass(target.getName() + CLS_SUFFIX);
        }
        return ctTarget;
    }

    private void prepareProxy(final Object impl, final boolean extend) throws Exception, NotFoundException {
        this.addImport(impl.getClass());
        this.addField("private " + impl.getClass().getName() + " " + TARGET + ";");
        this.doAddInterface(false, _IAssisted_.class);
        this.addMethod("public void setTarget(Object target) { " + TARGET + " = (" + impl.getClass().getName() + ") target;}");
        if (extend) {
            this.addMethodsOf(impl.getClass());
        }
    }

    @Override
    public Object getTarget() {
        return this.target;
    }

    @Override
    public IAdviceBuilder addImport(final Class<?>... classes) {
        for (final Class<?> c : classes) {
            try {
                this.pool.get(c.getName());
            } catch (final NotFoundException e) {
                throw new RuntimeException(e);
            }
            this.pool.importPackage(c.getPackage().getName());
        }
        return this;
    }

    @Override
    public IAdviceBuilder addField(final String definition) throws Exception {
        final CtField fieldSrvInst = CtFieldWithInit.make(definition, this.ctClass);
        this.ctClass.addField(fieldSrvInst);
        return this;
    }

    @Override
    public IAdviceBuilder addMethod(final String body) throws Exception {
        final CtMethod method = CtNewMethod.make(body, this.ctClass);
        this.ctClass.addMethod(method);
        return this;
    }

    @Override
    public IAdviceBuilder addInterface(final Class<?>... interfaces) throws Exception {
        this.doAddInterface(true, interfaces);
        return this;
    }

    @Override
    public IAdviceBuilder register(final Advice advice, final String code) {
        return this.register(advice, code, null);
    }

    @Override
    public IAdviceBuilder register(final Advice advice, final String code, final IAdviceListener listener) {
        final AdviceInfo info = new AdviceInfo(advice, code).setListener(listener);
        this.adviceMap.get(advice).add(info);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T generate(final Class<T> inf) throws Exception {
        for (final Method m : this.methodList) {
            final MethodCode methodCode = this.createMethodBody(m);
            for (final Advice advice : Advice.values()) {
                methodCode.body = this.injectAdviceCode(advice, methodCode, m);
            }
            if (this.extend) {
                final CtMethod superMethod = this.getCtMethod(m);
                if (superMethod != null) {
                    final CtMethod ctMethod = CtNewMethod.copy(superMethod, this.ctClass, null);
                    this.ctClass.addMethod(ctMethod);
                    ctMethod.setBody(methodCode.body);
                }
            } else {
                this.addMethod(methodCode.complete());
            }
        }
        final Class<?> proxyClass = this.ctClass.toClass(this.loader, null);
        final Object proxy = proxyClass.newInstance();
        ((_IAssisted_) proxy).setTarget(this.target);
        return (T) proxy;
    }

    @Override
    public Object generate() throws Exception {
        return this.generate(Object.class);
    }

    //

    private CtMethod getCtMethod(final Method m) throws NotFoundException {
        final Class<?>[] params = m.getParameterTypes();
        final CtClass[] ctParams = new CtClass[m.getParameterTypes().length];
        for (int i = 0; i < params.length; i++) {
            ctParams[i] = this.pool.get(params[i].getName());
        }
        final String descriptor = Descriptor.ofMethod(this.pool.get(m.getReturnType().getName()), ctParams);
        return this.ctClass.getMethod(m.getName(), descriptor);
    }

    private void doAddInterface(final boolean instrument, final Class<?>... interfaces) throws NotFoundException {
        final List<Method> methods = new ArrayList<Method>();
        for (final Class<?> c : interfaces) {
            this.ctClass.addInterface(this.pool.get(c.getName()));
            if (instrument) {
                for (final Method m : c.getMethods()) {
                    methods.add(m);
                }
            }
        }
        this.methodList.addAll(methods);
    }

    private void addMethodsOf(final Class<?> clazz) {
        for (final Method m : clazz.getDeclaredMethods()) {
            this.methodList.add(m);
        }
    }

    private String injectAdviceCode(final Advice advice, final MethodCode code, final Method m) throws CannotCompileException {
        final List<AdviceInfo> adviceList = this.adviceMap.get(advice);
        if (adviceList.size() == 0) {
            return code.body;
        }
        String newCode = code.body;
        for (final AdviceInfo info : adviceList) {
            if (info.call(m)) {
                if (advice == Advice.AROUND) {
                    newCode = newCode.replace(AROUND_FLAG, AROUND_USED);
                }
                newCode = newCode.replace(advice.id(), info.code());
            }
        }
        return newCode;
    }

    private MethodCode createMethodBody(final Method m) throws Exception {
        final MethodCode code = new MethodCode();
        code.signature = getSignatureOf(m);
        final Class<?> returnType = m.getReturnType();
        final boolean noReturn = returnType.equals(void.class);
        final StringBuilder body = new StringBuilder(" { ").append(NLT);
        if (!noReturn) {
            final String defVal = String.valueOf(Primitives.primitiveValueOrNullFor(returnType));
            body.append(m.getReturnType().getName() + " " + RESULT + " = ").append(defVal).append(";").append(NLT);
        }
        body.append("boolean " + AROUND + " = false;").append(NLT);
        body.append(AROUND_FLAG).append(NLT);

        // >>--------- BEFORE ADVICE ---------<<
        body.append(Advice.BEFORE.id()).append(NLT);

        // >>--------- AROUND ADVICE ---------<<
        body.append("if (!" + AROUND + ") {").append(NLT);
        if (noReturn) {
            body.append(TARGET + "." + m.getName() + "($$);").append(NLT);
        } else {
            body.append(RESULT + " = " + TARGET + "." + m.getName() + "($$);").append(NLT);
        }
        body.append("} else {").append(NLT);
        body.append(Advice.AROUND.id()).append(NLT);
        body.append("}").append(NLT);

        // >>--------- AFTER ADVICE ---------<<
        body.append(Advice.AFTER.id()).append(NLT);
        body.append(noReturn ? "" : "return " + RESULT + ";").append("\n");
        body.append("}");
        //
        code.body = body.toString();
        return code;
    }

    @SuppressWarnings("unused")
    private String block(final String code) {
        return "{" + NLT + code + NLT + "}";
    }

    private static String getSignatureOf(final Method m) {
        final String sign = m.toString();
        String method = sign.substring(0, sign.indexOf("(")).replace(" abstract ", " ");
        method = method.substring(0, method.lastIndexOf(" ") + 1) + m.getName();
        final String params = sign.substring(sign.indexOf("(") + 1, sign.indexOf(")"));
        final StringBuilder newParams = new StringBuilder();
        if (params != null) {
            final String[] paramVals = params.split(",");
            if ((paramVals.length > 0) && !paramVals[0].equals("")) {
                for (int i = 0; i < paramVals.length; i++) {
                    newParams.append(paramVals[i] + " " + "p" + i);
                    if (i != (paramVals.length - 1)) {
                        newParams.append(",");
                    }
                }
            }
        }
        return method + "(" + newParams.toString() + ")";
    }

}
