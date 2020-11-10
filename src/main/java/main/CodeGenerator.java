package main;

import arm11.data.Data;
import arm11.instructions.BranchInstruction;
import arm11.instructions.Instruction;
import arm11.instructions.LabelInstruction;
import arm11.instructions.block_data_transfer.PopInstruction;
import arm11.instructions.block_data_transfer.PushInstruction;
import arm11.instructions.data_processing.AddInstruction;
import arm11.instructions.data_processing.CmpInstruction;
import arm11.instructions.data_processing.MovInstruction;
import arm11.instructions.single_data_transfer.LdrInstruction;
import arm11.operands.address.PreIndex;
import arm11.operands.address.expression.StringExpression;
import arm11.operands.snd_operand.immValue.IntImmValue;
import arm11.operands.snd_operand.register.Register;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import static arm11.data.err_data_builders.ErrArrIndexOutOfBoundData.ERR_ARR_INDEX_OUT_OF_BOUND_DATA;
import static arm11.data.err_data_builders.ErrArrNegIndexData.ERR_ARR_NEG_INDEX_DATA;
import static arm11.data.err_data_builders.ErrDivisionData.ERR_DIVISION_DATA;
import static arm11.data.err_data_builders.ErrNullRefData.ERR_NULL_REF_DATA;
import static arm11.data.err_data_builders.ErrOverflowData.ERR_OVERFLOW_DATA;
import static arm11.data.io_data_builders.IOCharData.IO_CHAR_DATA;
import static arm11.data.io_data_builders.IOFalseData.IO_FALSE_DATA;
import static arm11.data.io_data_builders.IOIntData.IO_INT_DATA;
import static arm11.data.io_data_builders.IOLineData.IO_LINE_DATA;
import static arm11.data.io_data_builders.IOPointerData.IO_POINTER_DATA;
import static arm11.data.io_data_builders.IOStringData.IO_STRING_DATA;
import static arm11.data.io_data_builders.IOTrueData.IO_TRUE_DATA;
import static main.RegisterAllocator.*;

public class CodeGenerator {

  public static final String INDENTATION = "\t\t";

  // .data section:
  private List<Data> dataList; // String data.
  // .global main section:
  private List<List<Instruction>> globalMainList;
  // Flags set:
  private LinkedHashSet<UtilsToken> flagSet;
  // Branch number:
  private int branchNum;

  public CodeGenerator() {
    // Initialisation:
    this.dataList = new ArrayList<>();
    this.globalMainList = new ArrayList<>();
    this.flagSet = new LinkedHashSet<>();
    this.branchNum = 0;
  }

  public int getBranchNum() {
    return branchNum;
  }

  /* Flag setters */
  public void setPrint_bool() {
    flagSet.add(UtilsToken.PRINT_BOOL);
  }

  public void setPrint_line() {
    flagSet.add(UtilsToken.PRINT_LINE);
  }

  public void setPrint_int() {
    flagSet.add(UtilsToken.PRINT_INT);
  }

  public void setPrint_pointer() {
    flagSet.add(UtilsToken.PRINT_POINTER);
  }

  public void setPrint_string() {
    flagSet.add(UtilsToken.PRINT_STRING);
  }

  public void setRead_char() {
    flagSet.add(UtilsToken.READ_CHAR);
  }

  public void setRead_int() {
    flagSet.add(UtilsToken.READ_INT);
  }

  // Set corresponding flags and call other set functions
  public void setErr_array_index() {
    flagSet.add(UtilsToken.ERR_ARRAY);
    setPrint_string();
    setErr();
  }

  public void setErr_division() {
    flagSet.add(UtilsToken.ERR_DIVISION);
    setPrint_string();
    setErr();
  }

  public void setErr_nullRef() {
    flagSet.add(UtilsToken.ERR_NULLREF);
    setPrint_string();
    setErr();
  }

  public void setErr_overflow() {
    flagSet.add(UtilsToken.ERR_OVERFLOW);
    setPrint_string();
    setErr();
  }

  private void setErr() {
    flagSet.add(UtilsToken.ERR);
  }

  public void setFree() {
    flagSet.add(UtilsToken.FREE);
    setPrint_string();
    setErr();
  }

  /* List helpers */

  // Add data:
  public int addData(Data instruction) {
    dataList.add(instruction);
    return dataList.size() - 1;
  }

  // Create new function:
  public void addNewFunction(String label) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction(label));
    globalMainList.add(instructions);
  }

  // Add new instruction to the last function defined:
  public void addInstruction(Instruction instruction) {
    globalMainList.get(globalMainList.size() - 1).add(instruction);
  }

  // Add new Function:
  public void addInstructionList(List<Instruction> instructions) {
    globalMainList.add(instructions);
  }

  // Open a branch:
  public void openBranch() {
    addNewFunction("L" + this.branchNum);
    this.branchNum++;
  }

  public void openBranch(int branchNum) {
    addNewFunction("L" + branchNum);
    if (this.branchNum <= branchNum) {
      this.branchNum = branchNum + 1;
    }
  }

  // Generate assembly code in the file with name @param fileName:
  public void generateCode(String fileName) {
    updateUtilsData();
    try {
      FileWriter myWriter = new FileWriter(fileName);
      // Data section:
      if (dataList.size() != 0) {
        myWriter.write(".data\n\n");
      }
      for (int i = 0; i < dataList.size(); i++) {
        myWriter.write("msg_" + i + ":\n");
        Data data = dataList.get(i);
        myWriter.write(INDENTATION + data.printWordLength() + "\n");
        myWriter.write(INDENTATION + data.printMessage() + "\n");
      }
      // Text section:
      myWriter.write(".text\n\n");
      // Global main section:
      myWriter.write(".global main\n");
      for (List<Instruction> instructionList : globalMainList) {
        myWriter.write(instructionList.get(0).print() + "\n");
        for (int i = 1; i < instructionList.size(); i++) {
          myWriter.write(INDENTATION + instructionList.get(i).print() + "\n");
        }
      }
      myWriter.close();
    } catch (IOException e) {
      System.out.println("An error occurred when writing to the assembly file.");
      e.printStackTrace();
    }
  }

  // Update the data list for utils:
  private void updateUtilsData() {
    Iterator<UtilsToken> iterator = flagSet.iterator();
    while (iterator.hasNext()) {
      UtilsToken utilsToken = iterator.next();
      switch (utilsToken) {
        case ERR_ARRAY:
          int dataPosition1 = addData(ERR_ARR_NEG_INDEX_DATA);
          int dataPosition2 = addData(ERR_ARR_INDEX_OUT_OF_BOUND_DATA);
          addCheckArray(dataPosition1, dataPosition2);
          break;
        case ERR_DIVISION:
          int dataPosition = addData(ERR_DIVISION_DATA);
          addCheckDivision(dataPosition);
          break;
        case ERR_NULLREF:
          dataPosition = addData(ERR_NULL_REF_DATA);
          addCheckNullRef(dataPosition);
          break;
        case ERR_OVERFLOW:
          dataPosition = addData(ERR_OVERFLOW_DATA);
          setPrint_string();
          addThrowOverflow(dataPosition);
          break;
        case ERR:
          addError();
          break;
        case FREE:
          dataPosition = addData(ERR_NULL_REF_DATA);
          addFree(dataPosition);
          break;
        case PRINT_BOOL:
          dataPosition1 = addData(IO_TRUE_DATA);
          dataPosition2 = addData(IO_FALSE_DATA);
          addPrintBool(dataPosition1, dataPosition2);
          break;
        case PRINT_LINE:
          dataPosition = addData(IO_LINE_DATA);
          addPrintLine(dataPosition);
          break;
        case PRINT_INT:
          dataPosition = addData(IO_INT_DATA);
          addPrintInt(dataPosition);
          break;
        case PRINT_POINTER:
          dataPosition = addData(IO_POINTER_DATA);
          addPrintReference(dataPosition);
          break;
        case PRINT_STRING:
          dataPosition = addData(IO_STRING_DATA);
          addPrintString(dataPosition);
          break;
        case READ_CHAR:
          dataPosition = addData(IO_CHAR_DATA);
          addReadChar(dataPosition);
          break;
        case READ_INT:
          dataPosition = addData(IO_INT_DATA);
          addReadInt(dataPosition);
          break;
      }
    }
  }

  /* Generate I/O instruction sets with given @param dataPosition. */
  private void addPrintString(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_print_string"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new LdrInstruction(R1, new PreIndex(R0)));
    instructions.add(new AddInstruction(R2, R0, new IntImmValue(4)));
    instructions.add(new LdrInstruction(R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new AddInstruction(R0, R0, new IntImmValue(4)));
    instructions.add(new BranchInstruction(true, "printf"));
    instructions.add(new MovInstruction(R0, new IntImmValue(0)));
    instructions.add(new BranchInstruction(true, "fflush"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  private void addPrintLine(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_print_ln"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new LdrInstruction(R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new AddInstruction(R0, R0, new IntImmValue(4)));
    instructions.add(new BranchInstruction(true, "puts"));
    instructions.add(new MovInstruction(R0, new IntImmValue(0)));
    instructions.add(new BranchInstruction(true, "fflush"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  private void addPrintBool(int dataPosition1, int dataPosition2) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_print_bool"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new CmpInstruction(R0, new IntImmValue(0)));
    instructions.add(new LdrInstruction("NE", R0, new StringExpression("msg_" + dataPosition1)));
    instructions.add(new LdrInstruction("EQ", R0, new StringExpression("msg_" + dataPosition2)));
    instructions.add(new AddInstruction(R0, R0, new IntImmValue(4)));
    instructions.add(new BranchInstruction(true, "printf"));
    instructions.add(new MovInstruction(R0, new IntImmValue(0)));
    instructions.add(new BranchInstruction(true, "fflush"));
    instructions.add(new PopInstruction(PC));
    addInstructionList(instructions);
  }

  private void addPrintInt(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_print_int"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new MovInstruction(R1, R0));
    instructions.add(new LdrInstruction(R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new AddInstruction(R0, R0, new IntImmValue(4)));
    instructions.add(new BranchInstruction(true, "printf"));
    instructions.add(new MovInstruction(R0, new IntImmValue(0)));
    instructions.add(new BranchInstruction(true, "fflush"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  private void addPrintReference(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_print_reference"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new MovInstruction(R1, R0));
    instructions.add(new LdrInstruction(R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new AddInstruction(R0, R0, new IntImmValue(4)));
    instructions.add(new BranchInstruction(true, "printf"));
    instructions.add(new MovInstruction(R0, new IntImmValue(0)));
    instructions.add(new BranchInstruction(true, "fflush"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  private void addReadChar(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_read_char"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new MovInstruction(R1, R0));
    instructions.add(new LdrInstruction(R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new AddInstruction(R0, R0, new IntImmValue(4)));
    instructions.add(new BranchInstruction(true, "scanf"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  private void addReadInt(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_read_int"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new MovInstruction(R1, R0));
    instructions.add(new LdrInstruction(R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new AddInstruction(R0, R0, new IntImmValue(4)));
    instructions.add(new BranchInstruction(true, "scanf"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  /* Generate error instruction sets with given @param dataPosition. */
  private void addCheckArray(int dataPosition1, int dataPosition2) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_check_array_bounds"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new CmpInstruction(R0, new IntImmValue(0)));
    instructions.add(new LdrInstruction("LT", R0, new StringExpression("msg_" + dataPosition1)));
    instructions.add(new BranchInstruction(true, "LT", "p_throw_runtime_error"));
    instructions.add(new LdrInstruction(R1, new PreIndex(R1)));
    instructions.add(new CmpInstruction(R0, R1));
    instructions.add(new LdrInstruction("CS", R0, new StringExpression("msg_" + dataPosition2)));
    instructions.add(new BranchInstruction(true, "CS", "p_throw_runtime_error"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  public void addCheckDivision(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_check_divide_by_zero"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new CmpInstruction(R1, new IntImmValue(0)));
    instructions.add(new LdrInstruction("EQ", R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new BranchInstruction(true, "EQ", "p_throw_runtime_error"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  private void addCheckNullRef(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_check_null_pointer"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new CmpInstruction(R0, new IntImmValue(0)));
    instructions.add(new LdrInstruction("EQ", R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new BranchInstruction(true, "EQ", "p_throw_runtime_error"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }

  private void addThrowOverflow(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_throw_overflow_error"));
    instructions.add(
        new LdrInstruction(new Register(0), new StringExpression("msg_" + dataPosition)));
    instructions.add(new BranchInstruction(true, "p_throw_runtime_error"));
    globalMainList.add(instructions);
  }

  private void addError() {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_throw_runtime_error"));
    instructions.add(new BranchInstruction(true, "p_print_string"));
    instructions.add(new MovInstruction(R0, new IntImmValue(-1)));
    instructions.add(new BranchInstruction(true, "exit"));
    globalMainList.add(instructions);
  }

  private void addFree(int dataPosition) {
    List<Instruction> instructions = new ArrayList<>();
    instructions.add(new LabelInstruction("p_free_pair"));
    instructions.add(new PushInstruction(LR));
    instructions.add(new CmpInstruction(R0, new IntImmValue(0)));
    instructions.add(new LdrInstruction("EQ", R0, new StringExpression("msg_" + dataPosition)));
    instructions.add(new BranchInstruction("EQ", "p_throw_runtime_error"));
    instructions.add(new PushInstruction(R0));
    instructions.add(new LdrInstruction(R0, new PreIndex(R0)));
    instructions.add(new BranchInstruction(true, "free"));
    instructions.add(new LdrInstruction(R0, new PreIndex(SP)));
    instructions.add(new LdrInstruction(R0, new PreIndex(R0, new IntImmValue(4))));
    instructions.add(new BranchInstruction(true, "free"));
    instructions.add(new PopInstruction(R0));
    instructions.add(new BranchInstruction(true, "free"));
    instructions.add(new PopInstruction(PC));
    globalMainList.add(instructions);
  }
}
