package arm11.data.err_data_builders;

import arm11.data.Data;

public final class ErrNullRefData extends Data {
  // Null pointer dereference error

  public static final ErrNullRefData ERR_NULL_REF_DATA = new ErrNullRefData();

  private ErrNullRefData() {
    super("NullReferenceError: dereference a null reference\\n\\0");
  }
}
