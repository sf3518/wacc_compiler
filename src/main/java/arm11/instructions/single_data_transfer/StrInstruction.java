package arm11.instructions.single_data_transfer;

import arm11.instructions.Instruction;
import arm11.operands.address.Address;
import arm11.operands.snd_operand.register.Register;

public final class StrInstruction implements Instruction {

  // LDR{type}{cond} Rd, <Address>
  // Type may be B or SB
  private final LoadStoreType type;
  // Conditional strings, including GT, LT, EQ and so on.
  private final String condition;
  private final Register rd;
  private final Address address;

  public StrInstruction(LoadStoreType type, String condition, Register rd, Address address) {
    this.type = type;
    this.condition = condition;
    this.rd = rd;
    this.address = address;
  }

  public StrInstruction(String condition, Register rd, Address address) {
    this(null, condition, rd, address);
  }

  public StrInstruction(LoadStoreType type, Register rd, Address address) {
    this(type, "", rd, address);
  }

  public StrInstruction(Register rd, Address address) {
    this(null, "", rd, address);
  }

  @Override
  public String print() {
    return "STR"
        + (type != null ? type.toString() : "")
        + condition
        + " "
        + rd.print()
        + ", "
        + address.print();
  }
}
