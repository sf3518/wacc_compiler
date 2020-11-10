package arm11.instructions.data_processing;

import arm11.instructions.Instruction;
import arm11.operands.snd_operand.SndOperand;
import arm11.operands.snd_operand.register.Register;

public final class CmpInstruction implements Instruction {
  // CMP{cond} Rn, Operand2

  // Conditional strings, including GT, LT, EQ and so on.
  private final String condition;
  private final Register rn;
  private final SndOperand secondOp;

  public CmpInstruction(String condition, Register rn, SndOperand secondOp) {
    this.condition = condition;
    this.rn = rn;
    this.secondOp = secondOp;
  }

  public CmpInstruction(Register rn, SndOperand secondOp) {
    this("", rn, secondOp);
  }

  @Override
  public String print() {
    return "CMP " + condition + rn.print() + ", " + secondOp.print();
  }
}
