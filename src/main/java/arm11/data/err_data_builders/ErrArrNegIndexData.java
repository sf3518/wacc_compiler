package arm11.data.err_data_builders;

import arm11.data.Data;

public final class ErrArrNegIndexData extends Data {
  // Array index negative error

  public static final ErrArrNegIndexData ERR_ARR_NEG_INDEX_DATA = new ErrArrNegIndexData();

  private ErrArrNegIndexData() {
    super("ArrayIndexOutOfBoundsError: negative index\\n\\0");
  }
}
