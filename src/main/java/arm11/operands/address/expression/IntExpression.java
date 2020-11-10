package arm11.operands.address.expression;

import arm11.operands.address.Address;

public final class IntExpression implements Address {
  // An int expression, in the form of "=i", where i is an int

  private final int num;

  public IntExpression(int num) {
    this.num = num;
  }

  @Override
  public String print() {
    return "=" + num;
  }
}
