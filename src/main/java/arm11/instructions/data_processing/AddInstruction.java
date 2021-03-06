package arm11.instructions.data_processing;

import arm11.instructions.Instruction;
import arm11.operands.snd_operand.SndOperand;
import arm11.operands.snd_operand.register.Register;

public final class AddInstruction implements Instruction {
  // Instruction in the form ADD{S}{cond} {Rd}, Rn, Operand2

  // Conditional strings, including GT, LT, EQ and so on.
  private final String condition;
  // Decide whether an "S" is added after "S"
  private final boolean flag;
  private final Register rd;
  private final Register rn;
  private final SndOperand secondOP;

  public AddInstruction(
      String condition, boolean flag, Register rd, Register rn, SndOperand secondOP) {
    this.condition = condition;
    this.flag = flag;
    this.rd = rd;
    this.rn = rn;
    this.secondOP = secondOP;
  }

  public AddInstruction(
      boolean flag, Register rd, Register rn, SndOperand secondOP) {
    this("", flag, rd, rn, secondOP);
  }

  public AddInstruction(String condition, Register rd, Register rn, SndOperand secondOP) {
    this(condition, false, rd, rn, secondOP);
  }

  public AddInstruction(Register rd, Register rn, SndOperand secondOp) {
    this("", false, rd, rn, secondOp);
  }

  @Override
  public String print() {
    return "ADD"
        + condition
        + (flag ? "S " : " ")
        + rd.print()
        + ", "
        + rn.print()
        + ", "
        + secondOP.print();
  }
}
