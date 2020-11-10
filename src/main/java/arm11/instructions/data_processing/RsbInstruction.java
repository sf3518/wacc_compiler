package arm11.instructions.data_processing;

import arm11.instructions.Instruction;
import arm11.operands.snd_operand.SndOperand;
import arm11.operands.snd_operand.register.Register;

public class RsbInstruction implements Instruction {
    // Instruction in the form RSB{S}{cond} {Rd}, Rn, Operand2

    // Conditional strings, including GT, LT, EQ and so on.
    private final String condition;
    // Decide whether an "S" is added after "S"
    private final boolean flag;
    private final Register rd;
    private final Register rn;
    private final SndOperand secondOP;

    public RsbInstruction(
            String condition, boolean flag, Register rd, Register rn, SndOperand secondOP) {
        this.condition = condition;
        this.flag = flag;
        this.rd = rd;
        this.rn = rn;
        this.secondOP = secondOP;
    }

    public RsbInstruction(
            boolean flag, Register rd, Register rn, SndOperand secondOP) {
        this("", flag, rd, rn, secondOP);
    }

    public RsbInstruction(String condition, Register rd, Register rn, SndOperand secondOP) {
        this(condition, false, rd, rn, secondOP);
    }

    public RsbInstruction(Register rd, Register rn, SndOperand secondOp) {
        this("", false, rd, rn, secondOp);
    }

    @Override
    public String print() {
        return "RSB"
                + condition
                + (flag ? "S " : " ")
                + rd.print()
                + ", "
                + rn.print()
                + ", "
                + secondOP.print();
    }

}
