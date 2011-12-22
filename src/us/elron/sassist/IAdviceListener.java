package us.elron.sassist;

import java.lang.reflect.Method;

public interface IAdviceListener {

    boolean applyAdviceFor(Advice advice, Method m, String code);

}
