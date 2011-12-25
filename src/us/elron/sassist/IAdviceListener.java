package us.elron.sassist;

import java.lang.reflect.Method;

/**
 * @author Ender Aydin Orak
 * @see elron.us :)
 */
public interface IAdviceListener {

    boolean applyAdviceFor(Advice advice, Method m, String code);

}
