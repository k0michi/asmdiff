package com.koyomiji.asmweaver;

public class LineTracker {
  private int currentLine = 0;

  public LineTracker(int currentLine) {
    this.currentLine = currentLine;
  }

  public void add(int delta) {
    this.currentLine += delta;
  }

  public int getCurrentLine() {
    return this.currentLine;
  }

  public int getDelta(int newLine) {
    int delta = newLine - this.currentLine;
    this.currentLine = newLine;
    return delta;
  }
}
