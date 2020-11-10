package arm11.operands.snd_operand.register.shifter_register;

import arm11.operands.snd_operand.immValue.IntImmValue;
import arm11.operands.snd_operand.register.Register;

public class Shift {

  /* A shift includes a shift type and either a int immediate value or a register Rs. */

  private final ShiftType shiftType;
  private final IntImmValue offsetIntImmValue;
  private final Register rs;

  public Shift(ShiftType shiftType, IntImmValue offsetIntImmValue) {
    this.shiftType = shiftType;
    this.offsetIntImmValue = offsetIntImmValue;
    this.rs = null;
  }

  public Shift(ShiftType shiftType, Register rs) {
    this.shiftType = shiftType;
    this.offsetIntImmValue = null;
    this.rs = rs;
  }

  public String print() {
    String start = shiftType.toString();
    if (offsetIntImmValue != null) {
      return start + " " + offsetIntImmValue.print();
    } else {
      return start + " " + rs.print();
    }
  }
}
