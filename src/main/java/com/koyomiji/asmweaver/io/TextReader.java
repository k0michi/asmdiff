package com.koyomiji.asmweaver.io;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TextReader implements CustomDataInput {
  private final StreamTokenizer tokenizer;

  public TextReader(Reader reader) {
    this.tokenizer = new StreamTokenizer(new BufferedReader(reader));
    setupTokenizer();
  }

  public TextReader(InputStream in) {
    this.tokenizer = new StreamTokenizer(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
    setupTokenizer();
  }

  // S式およびWasmのトークン規則に合わせた構文解析器の設定
  private void setupTokenizer() {
    tokenizer.resetSyntax();
    // 単語（シンボルや数値）として認める文字の定義
    tokenizer.wordChars('a', 'z');
    tokenizer.wordChars('A', 'Z');
    tokenizer.wordChars('0', '9');
    // 16進浮動小数の符号やドット、JVM内部形式、識別子用の記号
    tokenizer.wordChars('-', '-');
    tokenizer.wordChars('+', '+');
    tokenizer.wordChars('.', '.');
    tokenizer.wordChars('_', '_');
    tokenizer.wordChars('$', '$');
    tokenizer.wordChars(':', ':');

    // 空白文字の定義（これらを区切り文字として扱う）
    tokenizer.whitespaceChars(0, ' ');
    // 文字列のクォーテーション定義
    tokenizer.quoteChar('"');
  }

  // 次のトークンが単語（シンボル）であることを期待して取得するヘルパー
  private String nextWord() throws IOException {
    int token = tokenizer.nextToken();
    if (token != StreamTokenizer.TT_WORD) {
      throw new IOException("Expected a word/symbol, but got token type: " + token + " at line " + tokenizer.lineno());
    }
    return tokenizer.sval;
  }

  public void consume(String expected) throws IOException {
    String actual = nextWord();
    if (!expected.equals(actual)) {
      throw new IOException("Expected symbol '" + expected + "', but got '" + actual + "' at line " + tokenizer.lineno());
    }
  }

  @Override
  public void consumeBeginList(String name) throws IOException {
    int token = tokenizer.nextToken();
    if (token != '(') {
      throw new IOException("Expected '(', but got token type: " + token + " at line " + tokenizer.lineno());
    }
    consume(name); // カッコの直後は必ずそのリストのタグ（名前）
  }

  @Override
  public void consumeEndList() throws IOException {
    int token = tokenizer.nextToken();
    if (token != ')') {
      throw new IOException("Expected ')', but got token type: " + token + " at line " + tokenizer.lineno());
    }
  }

  @Override
  public <T> List<T> readList(String name, ElementReader<T> reader) throws IOException {
    consumeBeginList(name);
    List<T> list = new ArrayList<>();
    while (true) {
      int token = tokenizer.nextToken();
      if (token == ')') {
        // 閉じカッコに到達したらこのリストのパースは正常終了
        break;
      }
      // 閉じカッコではなかったので、トークンを一旦ストリームに戻して要素を読み込む
      tokenizer.pushBack();
      list.add(reader.read());
    }
    return list;
  }

  @Override
  public <T> T readNullable(ElementReader<T> reader) throws IOException {
    int token = tokenizer.nextToken();
    if (token == StreamTokenizer.TT_WORD && "null".equals(tokenizer.sval)) {
      return null; // "null" という単語が直接書かれていれば Java の null を返す
    }
    // null ではなかったので、トークンを戻して通常のデシリアライズを実行
    tokenizer.pushBack();
    return reader.read();
  }

  // --- プリミティブデータのパース実装 ---

  @Override
  public int readInt() throws IOException {
    String word = nextWord();
    // 16進数（0x...）表記で出力されている場合にも対応
    if (word.startsWith("0x") || word.startsWith("0X")) {
      return Integer.parseInt(word.substring(2), 16);
    }
    return Integer.parseInt(word);
  }

  @Override
  public long readLong() throws IOException {
    String word = nextWord();
    if (word.startsWith("0x") || word.startsWith("0X")) {
      return Long.parseLong(word.substring(2), 16);
    }
    return Long.parseLong(word);
  }

  @Override
  public float readFloat() throws IOException {
    // ★ Float.parseFloat は "0x1.99999ap-4" のような16進浮動小数をネイティブで誤差ゼロ復元できます
    return Float.parseFloat(nextWord());
  }

  @Override
  public double readDouble() throws IOException {
    // ★ 同上。Wasm思想である16進浮動小数の完全ラウンドトリップがここで結実します
    return Double.parseDouble(nextWord());
  }

  @Override
  public boolean readBoolean() throws IOException {
    return Boolean.parseBoolean(nextWord());
  }

  @Override
  public byte readByte() throws IOException {
    return (byte) readInt();
  }

  @Override
  public int readUnsignedByte() throws IOException {
    return readInt() & 0xFF;
  }

  @Override
  public short readShort() throws IOException {
    return (short) readInt();
  }

  @Override
  public int readUnsignedShort() throws IOException {
    return readInt() & 0xFFFF;
  }

  @Override
  public char readChar() throws IOException {
    return (char) readInt();
  }

  @Override
  public String readUTF() throws IOException {
    int token = tokenizer.nextToken();
    if (token != '"') {
      throw new IOException("Expected quoted string for UTF data at line " + tokenizer.lineno());
    }
    // TextWriter.writeUTF が施した16進エスケープを解除し、UTF-8文字列として再構成
    byte[] decodedBytes = unescapeString(tokenizer.sval);
    return new String(decodedBytes, StandardCharsets.UTF_8);
  }

  @Override
  public void readFully(byte[] b) throws IOException {
    readFully(b, 0, b.length);
  }

  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    int token = tokenizer.nextToken();
    if (token != '"') {
      throw new IOException("Expected quoted string for binary bytes at line " + tokenizer.lineno());
    }
    byte[] decoded = unescapeString(tokenizer.sval);
    if (decoded.length < len) {
      throw new IOException("String data too short to fill byte array");
    }
    System.arraycopy(decoded, 0, b, off, len);
  }

  // ★ ヘルパー：TextWriterが書き出した "\xx" (16進2桁) のエスケープ文字列を生のバイト配列へデコードする
  private byte[] unescapeString(String s) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\') {
        if (i + 2 >= s.length()) {
          throw new IOException("Invalid or truncated hex escape sequence");
        }
        // バックスラッシュの後の2文字を16進数としてパース
        String hex = s.substring(i + 1, i + 3);
        try {
          bos.write(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
          throw new IOException("Invalid hex escape sequence: \\" + hex + " at line " + tokenizer.lineno());
        }
        i += 2; // エスケープ消費分進める
      } else {
        bos.write(c);
      }
    }
    return bos.toByteArray();
  }

  @Override
  public int skipBytes(int n) throws IOException {
    throw new UnsupportedOperationException("skipBytes is not supported in text mode");
  }

  @Deprecated
  @Override
  public String readLine() throws IOException {
    throw new UnsupportedOperationException("readLine is deprecated and not supported");
  }
}