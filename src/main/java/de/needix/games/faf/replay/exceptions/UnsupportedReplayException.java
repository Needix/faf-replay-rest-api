package de.needix.games.faf.replay.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedReplayException extends RuntimeException {
    public UnsupportedReplayException(String message) {
        super(message);
    }
}
