package com.ubcmmhcsoftware.ubcmmhc_web.Config;

public final class URLConstant {
    public static final String FRONTEND_URL = "http://localhost:3000";
    public static final String REDIRECT_AFTER_LOGIN = FRONTEND_URL + "/dashboard";
    public static final String UNAUTHORIZED_URL = FRONTEND_URL + "/unauthorized";

    private URLConstant() {}
}
