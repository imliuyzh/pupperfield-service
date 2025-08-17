package com.pupperfield.backend.constant;

import lombok.NoArgsConstructor;

/**
 * Constants for the authentication endpoints.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AuthConstants {
    /**
     * A list of paths that are exempt from authentication.
     */
    public static final String[] ALLOWLIST = {
        "/api-docs", "/auth/login", "/status", "/swagger-ui"
    };

    /**
     * Name of the cookie that has the authorization token.
     */
    public static final String COOKIE_NAME = "fetch-access-token";

    /**
     * The path to the login endpoint.
     */
    public static final String LOGIN_PATH = "/auth/login";

    /**
     * The path to the logout endpoint.
     */
    public static final String LOGOUT_PATH = "/auth/logout";
}
