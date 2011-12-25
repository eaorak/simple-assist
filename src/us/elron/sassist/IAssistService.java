package us.elron.sassist;

/**
 * @author Ender Aydin Orak
 * @see elron.us :)
 */
public interface IAssistService {

    IAdviceBuilder createProxyFor(Object target, boolean extend) throws Exception;

}
