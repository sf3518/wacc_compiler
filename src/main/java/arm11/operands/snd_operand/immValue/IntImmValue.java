package arm11.operands.snd_operand.immValue;

public final class IntImmValue implements ImmValue {
  // Int immediate value

  private final int val;

  public IntImmValue(int val) {
    this.val = val;
  }

  public int getVal() {
    return val;
  }

  @Override
  public String print() {
    return "#" + val;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IntImmValue) {
      return ((IntImmValue) obj).getVal() == val;
    }
    return false;
  }
}
