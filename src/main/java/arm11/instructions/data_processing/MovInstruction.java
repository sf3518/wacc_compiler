package arm11.instructions.data_processing;

import arm11.instructions.Instruction;
import arm11.operands.snd_operand.SndOperand;
import arm11.operands.snd_operand.register.Register;

public final class MovInstruction implements Instruction {
  // MOV{cond}{S} Rd, Operand2

  // Conditional strings, including GT, LT, EQ and so on.
  private final String condition;
  // Decide whether an "S" is added after "S"
  private final boolean flag;
  private final Register rd;
  private final SndOperand secondOp;

  public MovInstruction(String condition, boolean flag, Register rd, SndOperand secondOp) {
    this.condition = condition;
    this.flag = flag;
    this.rd = rd;
    this.secondOp = secondOp;
  }

  public MovInstruction(Register rd, SndOperand secondOp, boolean flag) {
    this("", flag, rd, secondOp);
  }

  public MovInstruction(Register rd, SndOperand secondOp) {
    this("", false, rd, secondOp);
  }

  @Override
  public String print() {
    return "MOV" + condition + (flag ? "S " : " ") + rd.print() + ", " + secondOp.print();
  }
}
