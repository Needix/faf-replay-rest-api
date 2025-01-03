package de.needix.games.faf.replay.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ReplayNotFoundException extends RuntimeException {
    public ReplayNotFoundException(String message) {
        super(message);
    }
}