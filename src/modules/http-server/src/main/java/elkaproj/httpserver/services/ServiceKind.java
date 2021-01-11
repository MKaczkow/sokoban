package elkaproj.httpserver.services;

/**
 * Determines the kind of service.
 */
public enum ServiceKind {
    /**
     * The service is a singleton service.
     */
    SINGLETON,

    /**
     * The service is a transient service.
     */
    TRANSIENT
}
