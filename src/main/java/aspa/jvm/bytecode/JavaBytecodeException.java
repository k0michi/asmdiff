package aspa.jvm.bytecode;

import aspa.jvm.JVMFormatException;

@SuppressWarnings("serial")
public final class JavaBytecodeException extends JVMFormatException {

  public JavaBytecodeException() {
  }

  public JavaBytecodeException(String message) {
    super(message);
  }

  public JavaBytecodeException(Throwable cause) {
    super(cause);
  }

  public JavaBytecodeException(String message, Throwable cause) {
    super(message, cause);
  }

}
