package us.elron.sassist;

/**
 * @author Ender Aydin Orak
 * @see elron.us :)
 */
public enum Advice {

    BEFORE("//#ADV_BEFORE#"), //
    AROUND("//#ADV_AROUND#"), //
    AFTER("//#ADV_AFTER#"); //

    private String id;

    private Advice(String code) {
        this.id = code;
    }

    public String id() {
        return this.id;
    }

}
