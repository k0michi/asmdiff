package aspa.jvm.attr;

import aspa.jvm.JVMFormatException;

@SuppressWarnings("serial")
public final class InvalidAttributeLengthException extends JVMFormatException {

  public InvalidAttributeLengthException() {
    super("invalid attribute length");
  }
}
