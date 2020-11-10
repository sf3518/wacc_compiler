package arm11.data.io_data_builders;

import arm11.data.Data;

public final class IOFalseData extends Data {
  // Output false

  public static final IOFalseData IO_FALSE_DATA = new IOFalseData();

  private IOFalseData() {
    super("false\\0");
  }
}
