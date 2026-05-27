package aspa.core;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Patch2 extends Stream {
  private FileChannel channel;
  private MappedByteBuffer mmBuf;
  private long fPos;
  private static final long MMBLKSIZE = 1 << 20;  // 1 megabyte
  
  public Patch2(File file, String mode) throws IOException {
    super(file, mode);
    channel = getChannel();
    initBuffer(0L);
  }
  
  public Patch2(String file, String mode) throws IOException {
    super(file, mode);
    channel = getChannel();
    initBuffer(0L);
  }
  
  private void initBuffer(long pos) throws IOException {
    FileChannel.MapMode mm = readOnly ? FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE; 
    long size = readOnly ? channel.size() - pos : MMBLKSIZE;
    fPos = pos;
    mmBuf = channel.map(mm, pos, size);
    if (readOnly)
      mmBuf.load();
    dump("init");
    
  }

  public void dump(String s) throws IOException {
    System.out.printf("%s %d %d %d %d\n",s, (int) fPos, mmBuf.position(), mmBuf.capacity(), (int) channel.size());
  }
  
  @Override
  public int read() throws IOException {
    if (mmBuf.remaining() == 0)  {
      initBuffer(fPos + mmBuf.capacity());
    }

    int b = mmBuf.get();
    dump("read() -> "+ String.format("%02X",b < 0 ? 256+b : b));
    return b;
  }
  
  @Override
  public int read(byte[] buf) throws IOException {
    return read(buf, 0, buf.length);
  }
  
 
  
  
  @Override
  public int read(byte[] b, int pos, int length) throws IOException {
    if (mmBuf.remaining() < length) {
      initBuffer(fPos + mmBuf.position());
    }
    int n = Math.min(length, mmBuf.capacity() - mmBuf.position());
    mmBuf.get(b, pos, n);
    dump("read(byte[])" + length + " "+ n);
    return n;
  }
  
  @Override
  public void write(byte[] buf) throws IOException {
    write(buf, 0, buf.length);
  }
  
  @Override
  public void write(byte[] b, int pos, int length) throws IOException {
    if (mmBuf.remaining() < length) {
      initBuffer(fPos + mmBuf.position());
    }
    mmBuf.put(b, pos, length);
  }
  
  @Override 
  public void write(int b) throws IOException {
    if (mmBuf.remaining() == 0) {
      initBuffer(fPos + mmBuf.capacity());
    }
    mmBuf.put((byte) b);
  }
  
  @Override 
  public long getFilePointer() throws IOException {
    dump("FP  "+ (fPos+mmBuf.position()));
    return fPos + mmBuf.position();
  }
  
  @Override
  public int skipBytes(int n) throws IOException {
    dump("skipBytes");
    if ( n < 0)
      return 0;
    
    if (mmBuf.remaining() >= n) {
      mmBuf.position(mmBuf.position() + n);
      return n;
    }
    
    initBuffer(fPos + n);
    return n;
  }
  
  @Override 
  public long length() throws IOException {
    dump("length");
    if (!readOnly)
      mmBuf.force();
    return channel.size();
  }
  
  @Override
  public void setLength(long length) throws IOException {
    if (!readOnly) {
      mmBuf.force();
    }
    channel.truncate(length);
  }
  
  @Override
  public void seek(long pos) throws IOException {
    dump("seek");
    if (pos >= fPos && pos <= fPos + mmBuf.capacity()) {
      mmBuf.position((int)(pos - fPos));
    } else {
      initBuffer(pos);
    }
  }
  
  @Override
  public void close() throws IOException {
    if (!channel.isOpen())
      return;
    
    if (!readOnly) {
      mmBuf.force();
    }
    System.out.println("2 closing");
    super.close();  
    System.out.println("2 closed");
  }
  
}
