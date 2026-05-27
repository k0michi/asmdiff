package aspa.jvm.cp;

import aspa.jvm.JVMFormatException;

@SuppressWarnings("serial")
public class ConstantPoolException extends JVMFormatException {
  public ConstantPoolException(String message) {
    super(message);
  }
}
