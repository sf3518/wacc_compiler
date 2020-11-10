import arm11.instructions.BranchInstruction;
import arm11.instructions.DirectiveInstruction;
import arm11.instructions.Instruction;
import arm11.instructions.LabelInstruction;
import arm11.instructions.block_data_transfer.PopInstruction;
import arm11.instructions.block_data_transfer.PushInstruction;
import arm11.instructions.data_processing.AddInstruction;
import arm11.instructions.data_processing.CmpInstruction;
import arm11.instructions.data_processing.MovInstruction;
import arm11.instructions.single_data_transfer.LdrInstruction;
import arm11.operands.address.PreIndex;
import arm11.operands.address.expression.IntExpression;
import arm11.operands.snd_operand.immValue.IntImmValue;
import arm11.operands.snd_operand.register.Register;
import org.junit.Test;

import static main.RegisterAllocator.*;
import static org.junit.Assert.assertEquals;

public class ArmTest {

  @Test
  public void branchInstructionTest() {
    Instruction instruction = new BranchInstruction(true, "printf");
    assertEquals("BL printf", instruction.print());
    instruction = new BranchInstruction("printf");
    assertEquals("B printf", instruction.print());
  }

  @Test
  public void directiveInstructionTest() {
    Instruction instruction = new DirectiveInstruction("ltorg");
    assertEquals(".ltorg", instruction.print());
  }

  @Test
  public void LabelInstructionTest() {
    Instruction instruction = new LabelInstruction("main");
    assertEquals("main:", instruction.print());
  }

  @Test
  public void PopInstructionTest() {
    Instruction instruction = new PopInstruction(SP);
    assertEquals("POP {sp}", instruction.print());
  }

  @Test
  public void PushInstructionTest() {
    Instruction instruction = new PushInstruction(LR);
    assertEquals("PUSH {lr}", instruction.print());
  }

  @Test
  public void MovInstructionTest() {
    Instruction instruction = new MovInstruction(R0, new Register(4));
    assertEquals("MOV r0, r4", instruction.print());
  }

  @Test
  public void CmpInstructionTest() {
    Instruction instruction = new CmpInstruction(R0, new Register(4));
    assertEquals("CMP r0, r4", instruction.print());
  }

  @Test
  public void arithInstructionTest() {
    Instruction instruction = new AddInstruction(R0, R1, new IntImmValue(4));
    assertEquals("ADD r0, r1, #4", instruction.print());
  }

  @Test
  public void LdrInstructionTest() {
    Instruction instruction = new LdrInstruction(new Register(4), new PreIndex(SP, new IntImmValue(4)));
    assertEquals("LDR r4, [sp, #4]", instruction.print());
  }
}
