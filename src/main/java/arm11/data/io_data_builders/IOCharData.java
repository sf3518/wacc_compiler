package arm11.data.io_data_builders;

import arm11.data.Data;

public final class IOCharData extends Data {
  // Output char data

  public static final IOCharData IO_CHAR_DATA = new IOCharData();

  private IOCharData() {
    super(" %c\\0");
  }
}
