package aspa.java;

import java.io.IOException;
import java.io.RandomAccessFile;

public class StrippedCode {
  private static final int NORMAL_MODE            = 0;
  private static final int BLANK_MODE             = 1;
  private static final int PRE_COMMENT_MODE       = 2;
  private static final int COMMENT_LINE_MODE      = 3;
  private static final int COMMENT_BLOCK_MODE     = 4;
  private static final int COMMENT_BLOCK_END_MODE = 5;
  private static final int STRING_MODE            = 6;
  private static final int STRING_ESCAPE_MODE     = 7;
  private static final int CHAR_MODE              = 8;
  private static final int CHAR_ESCAPE_MODE       = 9;

  public static void strip(String fileIn, String fileOut) throws IOException {
    RandomAccessFile in = new RandomAccessFile(fileIn, "r");
    RandomAccessFile out = new RandomAccessFile(fileOut, "rw");

    int c = in.read();
    int mode = BLANK_MODE;

    while (c != -1) {
      boolean echo = true;
      boolean advance = true;

      // System.out.printf("%d %d\n", c, mode);

      switch (mode) {
        case NORMAL_MODE:
          if (c == '\n' || Character.isSpaceChar(c)) {
            mode = BLANK_MODE;
            if (c != '\n')
              c = ' ';
            echo = true;
          } else if (c == '/') {
            mode = PRE_COMMENT_MODE;
            echo = false;
          } else if (c == '\"') {
            mode = STRING_MODE;
          } else if (c == '\'') {
            mode = CHAR_MODE;
          }
          break;
        case BLANK_MODE:
          echo = false;
          if (c != '\n' && !Character.isSpaceChar(c)) {
            advance = false;
            mode = NORMAL_MODE;
          }
          break;
        case PRE_COMMENT_MODE:
          echo = false;
          if (c == '/') {
            mode = COMMENT_LINE_MODE;
          } else if (c == '*') {
            mode = COMMENT_BLOCK_MODE;
            echo = false;
          } else {
            mode = NORMAL_MODE;
            advance = false;
            out.write('/');
          }
          break;
        case COMMENT_LINE_MODE:
          if (c == '\n') {
            mode = NORMAL_MODE;
            echo = false;
          }
          break;
        case COMMENT_BLOCK_MODE:
          echo = false;
          if (c == '*')
            mode = COMMENT_BLOCK_END_MODE;
          break;
        case COMMENT_BLOCK_END_MODE:
          echo = false;
          if (c == '/')
            mode = NORMAL_MODE;
          break;
        case STRING_MODE:
          if (c == '\"')
            mode = NORMAL_MODE;
          else if (c == '\\')
            mode = STRING_ESCAPE_MODE;
          break;
        case STRING_ESCAPE_MODE:
          mode = STRING_MODE;
          break;
        case CHAR_MODE:
          if (c == '\'')
            mode = NORMAL_MODE;
          else if (c == '\\')
            mode = CHAR_ESCAPE_MODE;
          break;
        case CHAR_ESCAPE_MODE:
          mode = CHAR_MODE;
          break;
        default:
          throw new RuntimeException("internal error");
      }
      // System.out.printf("%d %d %b %b\n", c, mode, echo, advance);
      if (echo)
        out.write(c);
      if (advance)
        c = in.read();
    }
    out.setLength(out.getFilePointer());
    out.close();
    in.close();
  }

  public static void main(String[] args) throws IOException {

    strip(args[0], args[1]);
  }
}
