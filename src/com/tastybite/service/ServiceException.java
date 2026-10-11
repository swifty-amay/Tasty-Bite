package com.tastybite.service;

/** A problem while loading or saving data. The message is safe to show to the person. */
public class ServiceException extends Exception {

    public ServiceException(String message) { super(message); }

    public ServiceException(String message, Throwable cause) { super(message, cause); }
}
