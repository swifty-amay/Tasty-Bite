package com.tastybite.service;

/** Problem with a login or registration. The message is safe to show to the person. */
public class AuthException extends Exception {
    public AuthException(String message) { super(message); }
}
