package aspa.core;

import aspa.jar.JarArchive;

public class App {

  private App() {

  }

//  public static void main(String[] args) {
//    
//    if (args.length != 3) {
//      System.out.println("Usage: java aspa.core.App file1 file2 patchFile");
//    }
//    
//    Symbol a = File
//    JarArchive ja1 = new JarArchive(args[1]);
//    JarArchive ja2 = new JarArchive(args[2]);
//    Patch diff = new Patch("diff.bin", "rw");
//    ja2.diff(ja1, diff, null);
//
//    Patch unpatched = new Patch("unpatched.jar", "rw");
//    ja1.write(unpatched, null);
//    unpatched.close();
//    diff.close();
//    diff = new Patch("diff.bin", "r");
//    // diff.seek(0);
//    ja1.patch(diff, null);
//    diff.close();
//    Patch patched = new Patch("patched.jar", "rw");
//    ja1.write(patched, null);
//    patched.close();
//  }
}
