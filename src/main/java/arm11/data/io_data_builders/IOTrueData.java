package arm11.data.io_data_builders;

import arm11.data.Data;

public final class IOTrueData extends Data {
  // Output true data

  public static final IOTrueData IO_TRUE_DATA = new IOTrueData();

  private IOTrueData() {
    super("true\\0");
  }
}
