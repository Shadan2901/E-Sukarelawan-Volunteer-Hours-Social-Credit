package com.mycompany.e.sukarelawan.resources;

public class EVolunteerStoreFactory {
    private static final String DEFAULT_DB_URL = "jdbc:mysql://localhost:3307/esukarelawan?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "";
    private static final String DEFAULT_REQUIRE_DB = "true";

    private EVolunteerStoreFactory() {
    }

    public static EVolunteerStore create() {
        String url = setting("ESUKARELAWAN_DB_URL", DEFAULT_DB_URL);
        String user = setting("ESUKARELAWAN_DB_USER", DEFAULT_DB_USER);
        String password = setting("ESUKARELAWAN_DB_PASSWORD", DEFAULT_DB_PASSWORD);
        boolean requireDatabase = Boolean.parseBoolean(setting("ESUKARELAWAN_REQUIRE_DB", DEFAULT_REQUIRE_DB));

        if (isLocalNetBeansDatabase(url, user) && System.getProperty("ESUKARELAWAN_DB_PASSWORD") == null) {
            password = DEFAULT_DB_PASSWORD;
        }

        if (!url.isBlank() && !user.isBlank()) {
            try {
                return new JdbcEVolunteerStore(url, user, password);
            } catch (RuntimeException exception) {
                if (requireDatabase) {
                    throw exception;
                }
                System.err.println("Database store unavailable. Falling back to in-memory store: " + exception.getMessage());
                exception.printStackTrace(System.err);
            }
        }
        if (requireDatabase) {
            throw new IllegalStateException("Database settings are required. Set ESUKARELAWAN_REQUIRE_DB=false only for demo mode.");
        }
        return new InMemoryEVolunteerStore();
    }

    private static boolean isLocalNetBeansDatabase(String url, String user) {
        return url != null
                && url.startsWith("jdbc:mysql://localhost:3307/esukarelawan")
                && DEFAULT_DB_USER.equals(user);
    }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = System.getProperty(key);
        }
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
