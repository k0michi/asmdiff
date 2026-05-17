package com.koyomiji.asmweaver.io;

import com.koyomiji.asmweaver.ListHelper;
import com.koyomiji.asmweaver.util.tuple.Triplet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BinaryReader implements CustomDataInput {
  private final DataInputStream in;

  public BinaryReader(InputStream in) {
    this.in = new DataInputStream(in);
  }

  @Override
  public void consumeBeginList(String name) throws IOException {
    // ★ バイナリモードではS式の開始境界（カッコとタグ）は存在しないため、何もしない（NOP）
  }

  @Override
  public void consumeEndList() throws IOException {
    // ★ バイナリモードではS式の終了境界（閉じカッコ）は存在しないため、何もしない（NOP）
  }

  @Override
  public <T> List<T> readList(String name, ListHelper.ElementReader<T> reader) throws IOException {
    // BinaryWriter.writeList がストリームの先頭に刻印した「要素数」を回収
    int size = in.readInt();
    List<T> list = new ArrayList<>(size);

    // 確定したサイズ分だけ、渡されたデシリアライズロジック（ラムダ）をループで回す
    for (int i = 0; i < size; i++) {
      list.add(reader.read(this));
    }
    return list;
  }

  @Override
  public <T> T readNullable(ListHelper.ElementReader<T> reader) throws IOException {
    // BinaryWriter.writeNullable が落とした「存在フラグ（プレゼンスビット）」を読み出す
    boolean isPresent = in.readBoolean();
    if (isPresent) {
      return reader.read(this); // true なら中身のインスタンスを復元
    } else {
      return null;          // false なら要素は不在のため、そのまま Java の null を返す
    }
  }

  @Override
  public <T> T readVariant(Triplet<String, Integer, ListHelper.ElementReader<? extends T>>... cases) throws IOException {
    int id = in.readUnsignedByte();

    // 2. IDが一致するケースを探索
    for (Triplet<String, Integer, ListHelper.ElementReader<? extends T>> c : cases) {
      if (c.second == id) {
        return c.third.read(this);
      }
    }

    throw new IOException("Unknown binary variant ID: " + id);
  }

// --- 以下のプリミティブメソッド群は、DataInputStream へ 100% 透過的に委譲 ---

  @Override
  public void readFully(byte[] b) throws IOException {
    in.readFully(b);
  }

  @Override
  public void readFully(byte[] b, int off, int len) throws IOException {
    in.readFully(b, off, len);
  }

  @Override
  public int skipBytes(int n) throws IOException {
    return in.skipBytes(n);
  }

  @Override
  public boolean readBoolean() throws IOException {
    return in.readBoolean();
  }

  @Override
  public byte readByte() throws IOException {
    return in.readByte();
  }

  @Override
  public int readUnsignedByte() throws IOException {
    return in.readUnsignedByte();
  }

  @Override
  public short readShort() throws IOException {
    return in.readShort();
  }

  @Override
  public int readUnsignedShort() throws IOException {
    return in.readUnsignedShort();
  }

  @Override
  public char readChar() throws IOException {
    return in.readChar();
  }

  @Override
  public int readInt() throws IOException {
    return in.readInt();
  }

  @Override
  public long readLong() throws IOException {
    return in.readLong();
  }

  @Override
  public float readFloat() throws IOException {
    return in.readFloat();
  }

  @Override
  public double readDouble() throws IOException {
    return in.readDouble();
  }

  @Deprecated
  @Override
  public String readLine() throws IOException {
    return in.readLine();
  }

  @Override
  public String readUTF() throws IOException {
    // ★ クラスファイルの CONSTANT_Utf8_info (u2 length + Modified UTF-8) をそのまま最速で復元
    return in.readUTF();
  }
}