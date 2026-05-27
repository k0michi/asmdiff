package aspa.jvm;

import aspa.core.Stream;

public class BatchTest {

  public static void main(String[] args) throws Exception {
    if (args[0].equals("rw")) {
      ClassFile cf = new ClassFile();
      cf.read(new Stream(args[1], "r"), null);

      Stream out = new Stream(args[2], "rw");
      cf.write(out, null);
      out.close();
    } else if (args[0].equals("p")) {
      ClassFile c1 = new ClassFile(args[1]);
      ClassFile c2 = new ClassFile(args[2]);
      Stream diff = new Stream("diff.bin", "rw");
      c2.diff(c1, diff, c2.getPool());

      Stream unpatched = new Stream("unpatched.class", "rw");
      c1.write(unpatched, null);
      unpatched.close();
      diff.close();
      diff = new Stream("diff.bin", "r");
      // diff.seek(0);
      c1.patch(diff, c1.getPool());
      diff.close();
      Stream patched = new Stream("patched.class", "rw");
      c1.write(patched, null);
      patched.close();
    }
    else if (args[0].equals("derive")) {
      String f = args[1];
      String d1 = args[2];
      String d2 = args[3];
      String out = args[4];
      
      ClassFile c1 = new ClassFile(d1 + "/" + f);
      ClassFile c2 = new ClassFile(d2 + "/" + f);
      Stream p = new Stream(out + "/" + f + ".diff", "rw");
      c2.diff(c1, p, c2.getPool());
      p.close();      
    }
    else if (args[0].equals("apply")) {
      ClassFile c = new ClassFile(args[1]);
      Stream p = new Stream(args[2], "r");
      c.patch(p, c.getPool());
      Stream out = new Stream(args[3],"rw");
      c.write(out, null);
      p.close();  
      out.close();
    }
  }
}
