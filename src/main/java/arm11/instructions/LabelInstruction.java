package arm11.instructions;

public final class LabelInstruction implements Instruction {
  // Override print for labels

  private final String label;

  public LabelInstruction(String label) {
    this.label = label;
  }

  @Override
  public String print() {
    return label + ":";
  }
}
