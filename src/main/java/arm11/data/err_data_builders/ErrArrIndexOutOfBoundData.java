package arm11.data.err_data_builders;

import arm11.data.Data;

public final class ErrArrIndexOutOfBoundData extends Data {
  // Array index out of bounds error
  public static final ErrArrIndexOutOfBoundData ERR_ARR_INDEX_OUT_OF_BOUND_DATA =
      new ErrArrIndexOutOfBoundData();

  private ErrArrIndexOutOfBoundData() {
    super("ArrayIndexOutOfBoundsError: index too large\\n\\0");
  }
}
