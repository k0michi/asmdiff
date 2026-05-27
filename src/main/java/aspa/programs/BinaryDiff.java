package aspa.programs;

import java.io.FileInputStream;

import aspa.core.LCS;
import aspa.core.Stream;

public class BinaryDiff {
  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.err.println("Usage: BinaryDiff from to patch");
      return;
    }

    FileInputStream from = new FileInputStream(args[0]);
    FileInputStream to = new FileInputStream(args[1]);
    Stream patch = new Stream(args[2], "rw");

    byte[] a = new byte[from.available()];
    byte[] b = new byte[to.available()];

    from.read(a);
    to.read(b);

    LCS.diff(patch, a, b);

    from.close();
    to.close();
    patch.close();
  }
};
