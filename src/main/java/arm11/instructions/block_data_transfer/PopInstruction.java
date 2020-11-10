package arm11.instructions.block_data_transfer;

import arm11.instructions.Instruction;
import arm11.operands.snd_operand.register.Register;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PopInstruction implements Instruction {
  // The list of registers to pop
  private List<Register> registers;

  public PopInstruction(Register... rs) {
    this.registers = new ArrayList<>();
    this.registers.addAll(Arrays.asList(rs));
  }

  @Override
  public String print() {
    StringBuilder builder = new StringBuilder();
    builder.append("POP {");
    String prefix = "";
    for (Register register : registers) {
      builder.append(prefix);
      prefix = ", ";
      builder.append(register.print());
    }
    builder.append("}");
    return builder.toString();
  }
}
