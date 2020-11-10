package arm11.operands.address.expression;

import arm11.operands.address.Address;

public final class StringExpression implements Address {
  // A string expression, in the form of "=s", where s is a string

  private final String label;

  public StringExpression(String label) {
    this.label = label;
  }

  @Override
  public String print() {
    return "=" + label;
  }
}
