package aspa.general;

import java.io.IOException;

import aspa.core.Context;
import aspa.core.LCS;
import aspa.core.Stream;
import aspa.core.Root;
import aspa.core.Symbol;

public class BinaryData implements Root {
  
  private byte[] data;

  @Override
  public int diff(Symbol source, Stream out, Context ctx)
      throws IOException {
    BinaryData bdSource = (BinaryData) source;
    return LCS.diff(out, bdSource.data, data);
  }

  @Override
  public void patch(Stream in, Context ctx) throws IOException {
    data = LCS.patch(in, data);
  }

  @Override
  public void read(Stream in, Context ctx) throws IOException {
    data = new byte[(int) in.length()];
    in.read(data);
  }

  @Override
  public void write(Stream out, Context ctx) throws IOException {
    out.write(data);
  }

}
