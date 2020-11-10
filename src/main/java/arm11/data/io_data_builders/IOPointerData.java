package arm11.data.io_data_builders;

import arm11.data.Data;

public class IOPointerData extends Data {
  // Output a pointer data

  public static final IOPointerData IO_POINTER_DATA = new IOPointerData();

  private IOPointerData() {
    super("%p\\0");
  }
}
