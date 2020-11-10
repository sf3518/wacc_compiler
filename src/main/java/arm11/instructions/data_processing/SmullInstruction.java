package arm11.instructions.data_processing;

import arm11.instructions.Instruction;
import arm11.operands.snd_operand.SndOperand;
import arm11.operands.snd_operand.register.Register;

public class SmullInstruction implements Instruction {
  // SMULL{S}{cond} RdLo, RdHi, Rm, Rs

  // Conditional strings, including GT, LT, EQ and so on.
  private final String condition;
  // Decide whether an "S" is added after "S"
  private final boolean flag;
  private final Register rdLo;
  private final Register rdHi;
  private final Register rm;
  private final Register rs;

  public SmullInstruction(
          String condition, boolean flag, Register rdLo,Register rdHi, Register rm, Register rs) {
    assert !(rdHi.equals(rdLo));
    this.condition = condition;
    this.flag = flag;
    this.rdLo = rdLo;
    this.rdHi = rdHi;
    this.rm = rm;
    this.rs = rs;
  }

  @Override
  public String print() {
    return "SMULL"
            + condition
            + (flag ? "S " : " ")
            + rdLo.print()
            + ", "
            + rdHi.print()
            + ", "
            + rm.print()
            + ", "
            + rs.print();
  }
}
