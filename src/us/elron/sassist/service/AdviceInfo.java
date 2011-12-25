package us.elron.sassist.service;

import java.lang.reflect.Method;

import us.elron.sassist.Advice;
import us.elron.sassist.IAdviceBuilder;
import us.elron.sassist.IAdviceListener;

/**
 * @author Ender Aydin Orak
 * @see elron.us :)
 */
public class AdviceInfo {

    private final Advice    advice;
    private final String    code;
    private IAdviceListener listener;

    public AdviceInfo(final Advice advice, final String code) {
        this.advice = advice;
        this.code = code + IAdviceBuilder.NLT + advice.id();
    }

    public Advice advice() {
        return this.advice;
    }

    public String code() {
        return this.code;
    }

    public AdviceInfo setListener(final IAdviceListener listener) {
        this.listener = listener;
        return this;
    }

    public IAdviceListener listener() {
        return this.listener;
    }

    boolean call(final Method m) {
        return this.listener == null ? true : this.listener.applyAdviceFor(this.advice, m, this.code);
    }

    @Override
    public String toString() {
        return this.advice.name() + " : " + this.code;
    }

}