package nl.ing.api.contacting.conf.domain;

public enum InputTypeJava {
    RADIO,
    DROPDOWN,
    TEXTBOX;

    public static boolean isValidatable(InputTypeJava type) {
        return switch (type) {
            case RADIO, DROPDOWN -> true;
            case TEXTBOX -> false;
        };
    }
}
