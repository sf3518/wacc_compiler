package arm11.instructions;

public final class BranchInstruction implements Instruction {

  private final String label;
  // B{L}{cond} <expression>, expression can only be label
  // The flag that indicates whether "L" is present
  private final boolean flag;
  // Conditional strings, including GT, LT, EQ and so on.
  private final String condition;

  public BranchInstruction(boolean flag, String condition, String label) {
    this.flag = flag;
    this.condition = condition;
    this.label = label;
  }

  public BranchInstruction(String condition, String label) {
    this(false, condition, label);
  }

  public BranchInstruction(boolean flag, String label) {
    this(flag, "", label);
  }

  public BranchInstruction(String label) {
    this(false, "", label);
  }

  @Override
  public String print() {
    return "B" + (flag ? "L" : "") + condition + " " + label;
  }
}
