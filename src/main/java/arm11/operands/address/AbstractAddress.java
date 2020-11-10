package arm11.operands.address;

import arm11.operands.snd_operand.immValue.ImmValue;
import arm11.operands.snd_operand.immValue.IntImmValue;
import arm11.operands.snd_operand.register.Register;
import arm11.operands.snd_operand.register.shifter_register.ShiftType;

public abstract class AbstractAddress implements Address {
  // An abstract class which represents an address. An address can either be written in pre-index or post-index form

  // ShiftedRegister includes Post-index and Pre-index.
  private final Register first; // Only the first register is required in all cases, others are optional.
  private final Register second;
  private final ShiftType shiftType;
  private final ImmValue immValue;
  // If the value of the second register needs to be changed, there is a "!" at the end.
  private final boolean change;

  public AbstractAddress(
      Register first, Register second, ImmValue immValue, ShiftType shiftType, boolean change) {
    this.first = first;
    this.second = second;
    this.immValue = immValue;
    this.shiftType = shiftType;
    this.change = change;
  }

  public AbstractAddress(Register first) {
    this(first, null, null, null, false);
  }

  public AbstractAddress(Register first, ImmValue immValue) {
    this(first, null, immValue, null, false);
  }

  public AbstractAddress(Register first, IntImmValue intImmValue) {
    this.first = first;
    if (intImmValue.getVal() == 0) {
      this.immValue = null;
    } else {
      this.immValue = intImmValue;
    }
    this.second = null;
    this.shiftType = null;
    this.change = false;
  }

  public AbstractAddress(Register first, ImmValue immValue, boolean change) {
    this(first, null, immValue, null, change);
  }

  public AbstractAddress(Register first, Register second) {
    this(first, second, null, null, false);
  }

  public AbstractAddress(Register first, Register second, boolean change) {
    this(first, second, null, null, change);
  }

  public AbstractAddress(Register first, Register second, ImmValue immValue, ShiftType shiftType) {
    this(first, second, immValue, shiftType, false);
  }

  public Register getFirst() {
    return first;
  }

  public Register getSecond() {
    return second;
  }

  public ShiftType getShiftType() {
    return shiftType;
  }

  public ImmValue getImmValue() {
    return immValue;
  }

  public boolean isChange() {
    return change;
  }

  @Override
  public abstract String print();
}
