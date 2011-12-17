package us.elron.sassist;

import java.lang.reflect.Method;

public interface IAdviceListener {

    boolean addFor(Advice advice, Method m, String code);

}
