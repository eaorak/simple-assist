package us.elron.sassist;

public interface IAssistService {

    IAdviceBuilder createProxyFor(Object target, boolean extend) throws Exception;

    Object addLoggingTo(IAdviceBuilder builder) throws Exception;

}
