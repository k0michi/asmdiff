package com.koyomiji.asmweaver.io;

import java.io.IOException;
import java.util.Collection;

public class TextWriter implements CustomDataOutput {
  private final Appendable out;
  // ★ 前方スペースを制御するための状態フラグ
  private boolean needSpace = false;

  public TextWriter(Appendable out) {
    this.out = out;
  }

  // ★ トークン出力の直前にスペースを入れるか判定するヘルパー
  private void prepareToken() throws IOException {
    if (needSpace) {
      out.append(" ");
    }
    needSpace = true; // 一度何かを出力したら、次はスペースが必要
  }

  // --- ヘルパーメソッド：前方スペースを考慮して出力 ---
  private void append(String str) throws IOException {
    prepareToken();
    out.append(str);
  }

  @Override
  public void write(int b) throws IOException {
    append(String.format("0x%02X", b & 0xFF));
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    prepareToken(); // 文字列トークン全体の前にスペースを判定
    out.append("\"");
    for (int i = off; i < off + len; i++) {
      int v = b[i] & 0xFF;
      if (v >= 32 && v <= 126 && v != '"' && v != '\\') {
        out.append((char) v);
      } else {
        out.append(String.format("\\%02x", v));
      }
    }
    out.append("\""); // 末尾のスペースを削除
  }

  @Override
  public void writeBoolean(boolean v) throws IOException {
    append(Boolean.toString(v));
  }

  @Override
  public void writeByte(int v) throws IOException {
    append(Integer.toString(v));
  }

  @Override
  public void writeShort(int v) throws IOException {
    append(Integer.toString(v));
  }

  @Override
  public void writeChar(int v) throws IOException {
    append(Integer.toString(v));
  }

  @Override
  public void writeInt(int v) throws IOException {
    append(Integer.toString(v));
  }

  @Override
  public void writeLong(long v) throws IOException {
    append(Long.toString(v));
  }

  @Override
  public void writeFloat(float v) throws IOException {
    append(Float.toHexString(v));
  }

  @Override
  public void writeDouble(double v) throws IOException {
    append(Double.toHexString(v));
  }

  @Override
  public void writeBytes(String s) throws IOException {
    byte[] bytes = new byte[s.length()];
    for (int i = 0; i < s.length(); i++) {
      bytes[i] = (byte) s.charAt(i);
    }
    write(bytes);
  }

  @Override
  public void writeChars(String s) throws IOException {
    prepareToken();
    out.append("\"");
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      out.append(String.format("\\%02x\\%02x", (c >> 8) & 0xFF, c & 0xFF));
    }
    out.append("\""); // 末尾のスペースを削除
  }

  @Override
  public void writeUTF(String s) throws IOException {
    write(s.getBytes("UTF-8"));
  }

  @Override
  public void beginList(String name) throws IOException {
    prepareToken(); // 開始カッコの前にスペースが必要なら入れる
    out.append("(").append(name); // 末尾のスペースを削除
  }

  @Override
  public void endList() throws IOException {
    out.append(")"); // 閉じカッコの前には絶対にスペースを入れない
    needSpace = true; // この閉じカッコの直後に別の要素（兄弟要素）が来たらスペースが必要
  }

  @Override
  public <T> void writeList(String name, Collection<T> collection, ElementWriter<T> writer) throws IOException {
    beginList(name);
    for (T element : collection) {
      writer.write(element);
    }
    endList();
  }

  @Override
  public <T> void writeNullable(T element, ElementWriter<T> writer) throws IOException {
    if (element == null) {
      append("null");
    } else {
      writer.write(element);
    }
  }

  @Override
  public <T> void writeVariant(String name, int id, T element, ElementWriter<T> writer) throws IOException {
    beginList(name);
    writer.write(element);
    endList();
  }
}
