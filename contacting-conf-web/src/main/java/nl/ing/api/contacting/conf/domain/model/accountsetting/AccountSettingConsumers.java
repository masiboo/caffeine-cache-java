package nl.ing.api.contacting.conf.domain.model.accountsetting;

import nl.ing.api.contacting.conf.exception.Errors;

import java.util.Arrays;

public enum AccountSettingConsumers {
    CUSTOMER(0, "customer"),
    EMPLOYEE(1, "employee"),
    API(2, "api");

    private final int id;
    private final String value;

    AccountSettingConsumers(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public int id() {
        return id;
    }

    public String value() {
        return value;
    }

    public static AccountSettingConsumers fromValue(String value) {
        return Arrays.stream(AccountSettingConsumers.values())
                .filter(consumer -> consumer.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> Errors.unexpected( String.format("Unknown AccountSettingConsumer value: %s", value)));
    }
}
