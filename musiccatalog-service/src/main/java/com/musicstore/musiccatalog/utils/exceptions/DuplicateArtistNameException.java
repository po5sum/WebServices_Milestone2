package com.musicstore.musiccatalog.utils.exceptions;

public class DuplicateArtistNameException extends RuntimeException {

    public DuplicateArtistNameException(){}

    public DuplicateArtistNameException(String message) {
        super(message);
    }

    public DuplicateArtistNameException(Throwable cause) {
      super(cause);
    }

    public DuplicateArtistNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
