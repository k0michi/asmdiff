package aspa.util;

/**
 * A simple bit mask.
 * 
 * @author Eduardo Marques
 * 
 */
public final class Mask {
  /**
   * Internal buffer representing the bit mask.
   */
  private final byte[] buffer;

  /**
   * Constructor.
   * 
   * @param numberOfBits
   *          number of bits in the mask.
   */
  public Mask(int numberOfBits) {
    buffer = new byte[numberOfBits / 8 + (numberOfBits % 8 == 0 ? 0 : 1)];
  }

  /**
   * Set a bit.
   * 
   * @param bit
   *          The bit to set.
   */
  public void set(int bit) {
    buffer[bit >> 3] |= (1 << (bit & 0x07));
  }

  /**
   * Clear a bit.
   * 
   * @param bit
   *          The bit to clear.
   */
  public void clear(int bit) {
    buffer[bit >> 3] &= ~(1 << (bit & 0x07));
  }

  /**
   * Check if a bit is set.
   * 
   * @param bit
   *          Bit to test.
   * @return <code>true</code> if bit is set, <code>false</code> otherwise.
   */
  public boolean get(int bit) {
    return (buffer[bit >> 3] & (1 << (bit & 0x07))) != 0;
  }

  /**
   * Get reference to underlying data buffer representing the bit mask.
   * 
   * @return The underlying data buffer.
   */
  public byte[] bitBuffer() {
    return buffer;
  }

  @Override
  public String toString() {
    int n = buffer.length << 3;
    StringBuffer sb = new StringBuffer(n);

    for (int bit = 0; bit != n; bit++) {
      sb.append(get(bit) ? '1' : '0');
    }

    return sb.toString();
  }

  /**
   * Test program.
   */
  public static void main(String[] args) {
    Mask mask = new Mask(23);

    mask.set(1);
    mask.set(3);
    mask.set(9);
    mask.set(21);

    for (int i = 0; i < 23; i++)
      if (mask.get(i))
        System.out.printf("bit %d is set\n", i);

    System.out.println(mask.bitBuffer().length);
    System.out.println(mask);
  }
}
