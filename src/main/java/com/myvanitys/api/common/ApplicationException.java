package com.myvanitys.api.common;

public class ApplicationException extends RuntimeException {

  protected ApplicationException(String message) {
    super(message);
  }

  protected ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }

}
