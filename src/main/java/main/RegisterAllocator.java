package main;

import arm11.operands.snd_operand.register.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class RegisterAllocator {
    // Name some registers for specific use. R13 for stack pointer, R14 for link register, and R15 for program counter.

  public static final Register SP =
      new Register(13) {
        @Override
        public String print() {
          return "sp";
        }
      };
  public static final Register LR =
      new Register(14) {
        @Override
        public String print() {
          return "lr";
        }
      };
  public static final Register PC =
      new Register(15) {
        @Override
        public String print() {
          return "pc";
        }
      };
  // R0 - R3 are registers that store function arguments or return information.
  public static final Register R0 = new Register(0);
  public static final Register R1 = new Register(1);
  public static final Register R2 = new Register(2);
  public static final Register R3 = new Register(3);
  public static final Register R10 = new Register(10);
  public static final Register finalReg = new Register(11);

  // R4 - R11 are general purpose registers
  public static final int GENERAL_REG_NUM = 8;

  private List<Register> callerSaveList;
  private PriorityQueue<Register> generalRegList;

  public RegisterAllocator() {
    callerSaveList = new ArrayList<>();
    callerSaveList.add(R0);
    callerSaveList.add(R1);
    callerSaveList.add(R2);
    callerSaveList.add(R3);
    generalRegList =
        new PriorityQueue<>(GENERAL_REG_NUM, Comparator.comparingInt(Register::getNum));
    generalRegList.add(new Register(4));
    generalRegList.add(new Register(5));
    generalRegList.add(new Register(6));
    generalRegList.add(new Register(7));
    generalRegList.add(new Register(8));
    generalRegList.add(new Register(9));
    generalRegList.add(new Register(10));
    generalRegList.add(new Register(11));
  }

  public List<Register> getCallerSaveList() {
    return callerSaveList;
  }

  public PriorityQueue<Register> getGeneralRegList() {
    return generalRegList;
  }

  public Register AllocReg() {
    return generalRegList.poll();
  }

  public void addUnusedReg(Register reg) {
    assert reg.getNum() >= 4 && reg.getNum() <= 11;
    for (Register register : generalRegList) {
      if (register.equals(reg)) {
        throw new RuntimeException();
      }
    }
    generalRegList.add(reg);
  }

  public Register nextReg() {
    return generalRegList.peek();
  }
}
