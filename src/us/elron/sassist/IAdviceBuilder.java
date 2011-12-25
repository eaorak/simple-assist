package us.elron.sassist;

/**
 * @author Ender Aydin Orak
 * @see elron.us :)
 */
public interface IAdviceBuilder {

    String NL  = "\n";     // New line
    String NLT = NL + "\t"; // New line and tab

    IAdviceBuilder addImport(Class<?>... classes);

    IAdviceBuilder addInterface(Class<?>... interfaces) throws Exception;

    IAdviceBuilder addField(String initializer) throws Exception;

    IAdviceBuilder addMethod(String body) throws Exception;

    IAdviceBuilder register(Advice advice, String code);

    IAdviceBuilder register(Advice advice, String code, IAdviceListener listener);

    <T> T generate(Class<T> inf) throws Exception;

    Object generate() throws Exception;

    Object getTarget();

}
