package aspa.programs;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.ArrayList;

import aspa.core.LCS;
import aspa.core.Stream;
import aspa.util.SString;

public class diffText {
  static SString[] readFile(BufferedReader f) throws Exception {
    ArrayList<SString> l = new ArrayList<SString>();

    while (true) {
      String line = f.readLine();
      if (line == null)
        break;
      l.add(new SString(line));
    }

    return l.toArray(new SString[l.size()]);
  }

  public static void main(String[] args) throws Exception {
    BufferedReader fa = new BufferedReader(new FileReader(args[0]));
    BufferedReader fb = new BufferedReader(new FileReader(args[1]));

    SString[] a = readFile(fa);
    SString[] b = readFile(fb);
    Stream stream = new Stream(args[2], "rw");

    LCS.diff(stream, SString.class, null, a, b);
    stream.close();
  }

};
