package arm11.data;

public class Data {
  // This class prints all the data appears in the program

  private final String message;

  public Data(String message) {
    this.message = message;
  }

  public String printWordLength() {
    int length = message.length();
    // Chars such as "\0" should only be considered as length one, so total length should subtract
    // the number of '\\' in it
    for (char c : message.toCharArray()) {
      if (c == '\\') {
        length--;
      }
    }
    return ".word " + length;
  }

  public String printMessage() {
    return ".ascii  \"" + message + "\"\n";
  }
}
