package com.koyomiji.asmweaver;

/**
 * This exception is thrown when there is a conflict between patches, such as two patches trying to modify the same instruction in incompatible ways.
 */
public class ConflictException extends Exception {
  public ConflictException(String message) {
    super(message);
  }
}
