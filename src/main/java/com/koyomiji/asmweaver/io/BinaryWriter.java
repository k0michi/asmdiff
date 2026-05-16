package com.koyomiji.asmweaver.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public class BinaryWriter implements CustomDataOutput {
  private final DataOutputStream out;

  public BinaryWriter(OutputStream out) {
    this.out = new DataOutputStream(out);
  }

  public BinaryWriter(DataOutputStream out) {
    this.out = out;
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
  }

  @Override
  public void writeBoolean(boolean v) throws IOException {
    out.writeBoolean(v);
  }

  @Override
  public void writeByte(int v) throws IOException {
    out.writeByte(v);
  }

  @Override
  public void writeShort(int v) throws IOException {
    out.writeShort(v);
  }

  @Override
  public void writeChar(int v) throws IOException {
    out.writeChar(v);
  }

  @Override
  public void writeInt(int v) throws IOException {
    out.writeInt(v);
  }

  @Override
  public void writeLong(long v) throws IOException {
    out.writeLong(v);
  }

  @Override
  public void writeFloat(float v) throws IOException {
    out.writeFloat(v);
  }

  @Override
  public void writeDouble(double v) throws IOException {
    out.writeDouble(v);
  }

  @Override
  public void writeBytes(String s) throws IOException {
    out.writeBytes(s);
  }

  @Override
  public void writeChars(String s) throws IOException {
    out.writeChars(s);
  }

  @Override
  public void writeUTF(String s) throws IOException {
    // ★ JVMの CONSTANT_Utf8_info の仕様 (u2 length + Modified UTF-8) に100%完全一致
    out.writeUTF(s);
  }

  @Override
  public void beginList(String name) throws IOException {
    // ★ バイナリモードでは構造の境界表現（カッコ）は不要なため、美しく「何もしない(NOP)」
  }

  @Override
  public void endList() throws IOException {
    // ★ 何もしない(NOP)
  }

  @Override
  public <T> void writeList(String name, Collection<T> collection, ElementWriter<T> writer) throws IOException {
    // ★ テキストのカッコの代わりに、最初に「要素数」を埋め込む
    // クラスファイルの各配列カウント（interfaces_count, methods_count 等）は基本的に u2（2バイト）です。
    // ここでは汎用的に writeInt としていますが、ドメイン（JVM）に完全準拠させる場合は out.writeShort に変更してください。
    out.writeInt(collection.size());
    for (T element : collection) {
      writer.write(element);
    }
  }

  @Override
  public <T> void writeNullable(T element, ElementWriter<T> writer) throws IOException {
    if (element == null) {
      // 存在しないことを示すフラグ（false）を落とす
      out.writeBoolean(false);
    } else {
      // 存在することを示すフラグ（true）を落としてから、中身を書き込む
      out.writeBoolean(true);
      writer.write(element);
    }
  }

  public void flush() throws IOException {
    out.flush();
  }
}
