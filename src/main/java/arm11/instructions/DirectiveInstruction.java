package arm11.instructions;

public final class DirectiveInstruction implements Instruction {
  // Override print for directives

  private final String directive;

  public DirectiveInstruction(String directive) {
    this.directive = directive;
  }

  @Override
  public String print() {
    return "." + directive;
  }
}
