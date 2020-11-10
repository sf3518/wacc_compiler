package arm11.data.io_data_builders;

import arm11.data.Data;

public final class IOStringData extends Data {
  // Output a string data

  public static final IOStringData IO_STRING_DATA = new IOStringData();

  private IOStringData() {
    super("%.*s\\0");
  }
}
