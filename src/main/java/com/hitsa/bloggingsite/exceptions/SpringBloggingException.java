package com.hitsa.bloggingsite.exceptions;

public class SpringBloggingException extends RuntimeException {
    public SpringBloggingException(String exMessage, Exception exception) {
        super(exMessage, exception);
    }

    public SpringBloggingException(String exMessage) {
        super(exMessage);
    }
}
