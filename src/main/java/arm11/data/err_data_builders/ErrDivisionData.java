package arm11.data.err_data_builders;

import arm11.data.Data;

public class ErrDivisionData extends Data {
  // Division by zero error

  public static final ErrDivisionData ERR_DIVISION_DATA = new ErrDivisionData();

  private ErrDivisionData() {
    super("DivideByZeroError: divide or modulo by zero\\n\\0");
  }
}
