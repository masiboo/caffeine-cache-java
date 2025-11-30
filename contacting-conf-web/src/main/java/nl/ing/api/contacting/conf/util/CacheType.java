package nl.ing.api.contacting.conf.util;

public enum CacheType {

    HAZELCAST_MAP("hazelcast-map"),
    DATABASE_ALL("database-all"),
    DATABASE_BY_ID("database-by-id"),
    ATTRIBUTES_BY_ACCOUNT("attributes-by-account"),
    ORACLE_UP("oracle-up"),
    PLATFORM_ACCOUNT_SETTINGS("pf-as-by-account"),
    ACTIVE_CONNECTION("active-connection-all"),
    CONNECTING_PERMISSION("bf-by-account");

    private final String cacheName;

    // Constructor to initialize the cacheName field
    CacheType(String cacheName) {
        this.cacheName = cacheName;
    }

    /**
     * Returns the cache name for the given cache type.
     */
    public String cacheName() {
        return cacheName;
    }
}
