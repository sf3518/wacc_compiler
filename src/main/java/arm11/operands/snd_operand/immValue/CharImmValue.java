package arm11.operands.snd_operand.immValue;

public final class CharImmValue implements ImmValue {
  // A char immediate value

  private final char character;

  public CharImmValue(char character) {
    this.character = character;
  }

  public int getCharacter() {
    return character;
  }

  // Modify the string generated from ANTLR to ensure correct strings are printed in assembly files
  @Override
  public String print() {
    String result = "" + character;
    result = result.replaceAll("\0", "\\\\0");
    result = result.replaceAll("\b", "\\\\b");
    result = result.replaceAll("\t", "\\\\t");
    result = result.replaceAll("\n", "\\\\n");
    result = result.replaceAll("\f", "\\\\f");
    result = result.replaceAll("\r", "\\\\r");
    result = result.replaceAll("\\\\", "\\\\\\\\");
    result = result.replaceAll("\"", "\\\\\"");
    result = result.replaceAll("'", "\\\\'");
    result = "#'" + result;
    result += "'";
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CharImmValue) {
      return ((CharImmValue) obj).getCharacter() == character;
    }
    return false;
  }
}
