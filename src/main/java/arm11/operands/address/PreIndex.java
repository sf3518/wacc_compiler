package arm11.operands.address;

import arm11.operands.snd_operand.immValue.ImmValue;
import arm11.operands.snd_operand.immValue.IntImmValue;
import arm11.operands.snd_operand.register.Register;
import arm11.operands.snd_operand.register.shifter_register.ShiftType;

public final class PreIndex extends AbstractAddress {
  // Print in pre-index form. Example: [R0, #4]

  public PreIndex(Register first) {
    super(first);
  }

  public PreIndex(Register first, ImmValue immValue) {
    super(first, immValue);
  }

  public PreIndex(Register first, IntImmValue intImmValue) {super(first, intImmValue);}

  public PreIndex(Register first, Register second) {
    super(first, second);
  }

  public PreIndex(Register first, ImmValue immValue, boolean change) {
    super(first, immValue, change);
  }

  public PreIndex(Register first, Register second, boolean change) {
    super(first, second, change);
  }

  public PreIndex(Register first, Register second, ImmValue immValue, ShiftType shiftType) {
    super(first, second, immValue, shiftType);
  }

  public PreIndex(
      Register first, Register second, ImmValue immValue, ShiftType shiftType, boolean change) {
    super(first, second, immValue, shiftType, change);
  }

  @Override
  public String print() {
    String prefix = "[";
    String suffix = "]";
    StringBuilder middle = new StringBuilder(getFirst().print());
    if (getImmValue() != null && getSecond() == null && getShiftType() == null) {
      middle.append(", ").append(getImmValue().print());
    } else if (getImmValue() == null && getSecond() != null) {
      middle.append(", ").append(getSecond().print());
    } else if (getImmValue() != null && getSecond() != null && getShiftType() != null) {
      middle
          .append(", ")
          .append(getSecond().print())
          .append(", ")
          .append(getShiftType().toString())
          .append(" ")
          .append(getImmValue().print());
    }
    return prefix + middle.toString() + suffix + (isChange() ? "!" : "");
  }
}
