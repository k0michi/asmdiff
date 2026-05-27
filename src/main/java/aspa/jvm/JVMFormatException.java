package aspa.jvm;

@SuppressWarnings("serial")
public class JVMFormatException extends RuntimeException {

  private static final String DEFAULT_MESSAGE = "JVM format error";
  public JVMFormatException() {
    super(DEFAULT_MESSAGE);
  }

  public JVMFormatException(String message) {
    super(message);
  }

  public JVMFormatException(Throwable cause) {
    super(DEFAULT_MESSAGE,cause);
  }

  public JVMFormatException(String message, Throwable cause) {
    super(message, cause);
  }

}
