package arm11.data.io_data_builders;

import arm11.data.Data;

public final class IOLineData extends Data {
  // Output a line data

  public static final IOLineData IO_LINE_DATA = new IOLineData();

  private IOLineData() {
    super("\\0");
  }
}
