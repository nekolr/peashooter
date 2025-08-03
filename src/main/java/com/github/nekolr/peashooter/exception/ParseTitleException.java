package com.github.nekolr.peashooter.exception;

import java.io.Serial;

public class ParseTitleException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1;

    public ParseTitleException(String message) {
        super(message);
    }
}
