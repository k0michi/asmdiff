package aspa.programs;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import aspa.core.LCS;
import aspa.core.Stream;

public class BinaryPatch {
  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.err.println("Usage: BinaryPatch from patch to");
      return;
    }

    FileInputStream from = new FileInputStream(args[0]);
    Stream patch = new Stream(args[1], "r");
    FileOutputStream to = new FileOutputStream(args[2]);

    byte[] a = new byte[from.available()];
    from.read(a);

    byte[] b = LCS.patch(patch, a);
    to.write(b);

    from.close();
    patch.close();
    to.close();
  }
};
