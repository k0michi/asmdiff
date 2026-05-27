package aspa.core;

import java.io.IOException;

public interface Symbol {
  int diff(Symbol old, Stream out, Context ctx) throws IOException;

  void patch(Stream in, Context ctx) throws IOException;

  void read(Stream in, Context ctx) throws IOException;

  void write(Stream out, Context ctx) throws IOException;
}
