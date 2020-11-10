package main;

import arm11.data.Data;
import arm11.instructions.BranchInstruction;
import arm11.instructions.DirectiveInstruction;
import arm11.instructions.block_data_transfer.PopInstruction;
import arm11.instructions.block_data_transfer.PushInstruction;
import arm11.instructions.data_processing.*;
import arm11.instructions.single_data_transfer.LdrInstruction;
import arm11.instructions.single_data_transfer.LoadStoreType;
import arm11.instructions.single_data_transfer.StrInstruction;
import arm11.operands.address.PreIndex;
import arm11.operands.address.expression.IntExpression;
import arm11.operands.address.expression.StringExpression;
import arm11.operands.snd_operand.immValue.CharImmValue;
import arm11.operands.snd_operand.immValue.IntImmValue;
import arm11.operands.snd_operand.register.Register;
import arm11.operands.snd_operand.register.shifter_register.Shift;
import arm11.operands.snd_operand.register.shifter_register.ShiftType;
import arm11.operands.snd_operand.register.shifter_register.ShiftedRegister;
import ast.assign.PairelemFstAST;
import ast.assign.PairelemSndAST;
import ast.assign.assignLhs.AssignLhsAST;
import ast.assign.assignLhs.LhsToken;
import ast.assign.assignRhs.ArraylitRhsAST;
import ast.assign.assignRhs.AssignRhsAST;
import ast.assign.assignRhs.CallRhsAST;
import ast.assign.assignRhs.NewpairRhsAST;
import ast.expression.*;
import ast.mixed.ArrayElemAST;
import ast.mixed.IdentifierLeaf;
import ast.statement.*;
import ast.top.FuncAST;
import ast.top.FuncParamAST;
import ast.top.ProgAST;
import symbolTable.SymbolTable;
import types.ArrayType;
import types.PairType;
import types.Type;
import types.TypeToken;

import java.util.List;

import static main.RegisterAllocator.*;

public class CodeGenVisitor {
  // A code generator to add instructions into it
  private CodeGenerator codeGenerator = new CodeGenerator();
  // A register allocator that allocates the next available register
  private RegisterAllocator registerAllocator = new RegisterAllocator();
  // A global symbol table that keeps track of variable/function argument names and types.
  // Also used to get offset related values by calling some utility functions
  private SymbolTable symbolTable;
  // Manage the stack and get the offsets
  private StackManager stackManager;
  public static int MAX_OFFSET = 1024;
  public static int CHAR_BOOL_OFFSET = 1;
  // All offsets for types other than bool and char is 4
  public static int OTHER_OFFSET = 4;

  public CodeGenerator getCodeGenerator() {
    return codeGenerator;
  }

  /* Program */

  public void visitProg(ProgAST progAST) {
    // Set up symbol table and stack manager:
    this.symbolTable = progAST.getSymbolTable();
    this.stackManager = new StackManager(symbolTable.getTotalOffset());
    SymbolTable originalST = this.symbolTable;
    StackManager originalSM = this.stackManager;
    // Visit functions:
    for (FuncAST funcAST : progAST.getFunctions()) {
      codeGenerator.addNewFunction(funcAST.getFunctionName().getText());
      this.symbolTable = funcAST.getSymbolTable();
      int totalOffset = 0;
      for (FuncParamAST funcParamAST : funcAST.getInputParams()) {
        totalOffset += funcParamAST.getType().getOffset();
      }
      this.stackManager = new StackManager(originalSM, totalOffset);
      visitFunction(funcAST);
    }
    // Main function:
    this.symbolTable = originalST;
    this.stackManager = originalSM;
    codeGenerator.addNewFunction("main");
    codeGenerator.addInstruction(new PushInstruction(LR));
    setUpScopeAndVisit(progAST.getStatement(), this.stackManager.getCurrOffset());
    // Finalisation:
    codeGenerator.addInstruction(new LdrInstruction(R0, new IntExpression(0)));
    codeGenerator.addInstruction(new PopInstruction(PC));
    codeGenerator.addInstruction(new DirectiveInstruction("ltorg"));
  }

  /* Function */

  public void visitFunction(FuncAST funcAST) {
    // Set up stack manager for input parameters:
    int currOffset = 0;
    // Iterate through all function ASTs to correctly set offset
    for (FuncParamAST funcParamAST : funcAST.getInputParams()) {
      String varName = funcParamAST.getIdentifier().getText();
      int offset = funcParamAST.getType().getOffset();
      stackManager.addVarOffset(varName, currOffset);
      currOffset += offset;
    }
    this.stackManager.setFuncOffset(
        this.symbolTable.getTotalOffset() - this.stackManager.getCurrOffset());
    this.stackManager.setCurrOffset(this.symbolTable.getTotalOffset() + 4);
    this.stackManager.setAllocOffset(
        this.symbolTable.getTotalOffset() - this.stackManager.getAllocOffset());
    codeGenerator.addInstruction(new PushInstruction(LR));
    // Set up stack offset for variables:
    int totalOffset = this.stackManager.getFuncOffset();
    if (totalOffset != 0) {
      while (totalOffset > MAX_OFFSET) {
        codeGenerator.addInstruction(new SubInstruction(SP, SP, new IntImmValue(MAX_OFFSET)));
        totalOffset -= MAX_OFFSET;
      }
      codeGenerator.addInstruction(new SubInstruction(SP, SP, new IntImmValue(totalOffset)));
    }
    // Visit statements:
    for (StatAST statAST : funcAST.getStatement()) {
      visitStat(statAST);
    }
    // Finalise:
    codeGenerator.addInstruction(new PopInstruction(PC));
    codeGenerator.addInstruction(new DirectiveInstruction("ltorg"));
  }

  /* Statement */

  public void visitStat(StatAST statAST) {
    switch (statAST.getStatType()) {
      case ASSIGN:
        visitAssignStat((AssignStatAST) statAST);
        break;
      case BLOCK:
        visitBlockStat((BlockStatAST) statAST);
        break;
      case CONDITION:
        visitConditionStat((ConditionStatAST) statAST);
        break;
      case DECLARATION:
        visitDeclStat((DeclStatAST) statAST);
        break;
      case LOOP:
        visitLoopStat((LoopStatAST) statAST);
        break;
      case NORMAL:
        visitNormalStat((NormalStatAST) statAST);
        break;
      case READ:
        visitReadStat((ReadStatAST) statAST);
        break;
    }
  }

  public void visitAssignStat(AssignStatAST assignStatAST) {
    //load the register of right hand side
    Register register1 = visitAssignRhs(assignStatAST.getRight());
    int typeOffset = assignStatAST.getLeft().getLhsType().getOffset();
    if (assignStatAST.getLeft().getLhsToken() == LhsToken.IDENT) {
      // case Identifier :
      IdentifierLeaf identifierLeaf = (IdentifierLeaf) assignStatAST.getLeft();
      int varOffset = stackManager.lookupAllOffset(identifierLeaf.getText());
      if (typeOffset == CHAR_BOOL_OFFSET) {
        // store data which only have 1 byte.
        codeGenerator.addInstruction(
            new StrInstruction(
                LoadStoreType.B, register1, new PreIndex(SP, new IntImmValue(varOffset))));
      } else {
        codeGenerator.addInstruction(
            new StrInstruction(register1, new PreIndex(SP, new IntImmValue(varOffset))));
      }
      //free rhs register.
      registerAllocator.addUnusedReg(register1);
      return;
    }
    // load the register of left hand side.
    Register register2 = visitAssignLhs(assignStatAST.getLeft());
    if (typeOffset == CHAR_BOOL_OFFSET) {
      // store data which only have 1 byte.
      codeGenerator.addInstruction(
          new StrInstruction(LoadStoreType.B, register1, new PreIndex(register2)));
    } else {
      codeGenerator.addInstruction(new StrInstruction(register1, new PreIndex(register2)));
    }
    // Free both registers:
    registerAllocator.addUnusedReg(register1);
    registerAllocator.addUnusedReg(register2);
  }

  public void visitBlockStat(BlockStatAST blockStatAST) {
    // Update symbol table and stack manager:
    SymbolTable origin = this.symbolTable;
    this.symbolTable = blockStatAST.getSymbolTable();
    this.stackManager = new StackManager(this.stackManager, this.symbolTable.getTotalOffset());
    setUpScopeAndVisit(blockStatAST.getStatements(), this.stackManager.getCurrOffset());
    // Reset symbol table and stack manager:
    this.stackManager = this.stackManager.getEncStackManager();
    this.symbolTable = origin;
  }

  public void visitConditionStat(ConditionStatAST conditionStatAST) {
    // Evaluate condition:
    Register register = visitExpr(conditionStatAST.getCondition());
    codeGenerator.addInstruction(new CmpInstruction(register, new IntImmValue(0)));
    registerAllocator.addUnusedReg(register);
    // True branch:
    int branchNum = codeGenerator.getBranchNum();
    codeGenerator.addInstruction(new BranchInstruction("EQ", "L" + branchNum));
    // Update symbol table and stack manager:
    SymbolTable origin = this.symbolTable;
    this.symbolTable = conditionStatAST.getTrueBrSymbolTable();
    this.stackManager = new StackManager(this.stackManager, this.symbolTable.getTotalOffset());
    // Visit true block:
    setUpScopeAndVisit(conditionStatAST.getTrueBranch(), this.stackManager.getCurrOffset());
    // Reset symbol table and stack manager:
    this.stackManager = this.stackManager.getEncStackManager();
    this.symbolTable = origin;
    // False branch:
    codeGenerator.addInstruction(new BranchInstruction("L" + (branchNum + 1)));
    codeGenerator.openBranch();
    // Update symbol table and stack manager:
    this.symbolTable = conditionStatAST.getFalseBrSymbolTable();
    this.stackManager = new StackManager(this.stackManager, this.symbolTable.getTotalOffset());
    // Visit false block:
    setUpScopeAndVisit(conditionStatAST.getFalseBranch(), this.stackManager.getCurrOffset());
    // Reset symbol table and stack manager:
    this.stackManager = this.stackManager.getEncStackManager();
    this.symbolTable = origin;
    // Continue to the remaining statements:
    codeGenerator.openBranch();
  }

  public void visitDeclStat(DeclStatAST declStatAST) {
    Type type = declStatAST.getType();
    Register register = visitAssignRhs(declStatAST.getRight());
    //update variable offset in stack manager.
    int currOffset = stackManager.getAllocOffset();
    currOffset -= type.getOffset();
    stackManager.setAllocOffset(currOffset);
    stackManager.addVarOffset(declStatAST.getIdentifier().getText());
    // store data which only have 1 byte.
    if (type.getOffset() == CHAR_BOOL_OFFSET) {
      codeGenerator.addInstruction(
          new StrInstruction(
              LoadStoreType.B, register, new PreIndex(SP, new IntImmValue(currOffset))));
    } else {
      codeGenerator.addInstruction(
          new StrInstruction(register, new PreIndex(SP, new IntImmValue(currOffset))));
    }
    registerAllocator.addUnusedReg(register);
  }

  public void visitLoopStat(LoopStatAST loopStatAST) {
    // Jump to the condition branch:
    int branchNum = codeGenerator.getBranchNum();
    codeGenerator.addInstruction(new BranchInstruction("L" + branchNum));
    // Block branch:
    codeGenerator.openBranch(branchNum + 1);
    // Update symbol table and stack manager:
    SymbolTable origin = this.symbolTable;
    this.symbolTable = loopStatAST.getSymbolTable();
    this.stackManager = new StackManager(this.stackManager, this.symbolTable.getTotalOffset());
    setUpScopeAndVisit(loopStatAST.getStatements(), this.stackManager.getCurrOffset());
    // Reset symbol table and stack manager:
    this.stackManager = this.stackManager.getEncStackManager();
    this.symbolTable = origin;
    // Condition branch and the remaining statements:
    codeGenerator.openBranch(branchNum);
    Register register = visitExpr(loopStatAST.getCondition());
    codeGenerator.addInstruction(new CmpInstruction(register, new IntImmValue(1)));
    registerAllocator.addUnusedReg(register);
    // Jump to the block branch if necessary:
    codeGenerator.addInstruction(new BranchInstruction("EQ", "L" + (branchNum + 1)));
  }

  public void visitNormalStat(NormalStatAST normalStatAST) {
    ExprAST exprAST = normalStatAST.getExpression();
    Register register = visitExpr(exprAST);
    switch (normalStatAST.getNormalStatType()) {
      case FREE:
        codeGenerator.addInstruction(new MovInstruction(R0, register));
        codeGenerator.addInstruction(new BranchInstruction(true, "p_free_pair"));
        codeGenerator.setFree();
        break;
      case EXIT:
        codeGenerator.addInstruction(new MovInstruction(R0, register));
        codeGenerator.addInstruction(new BranchInstruction(true, "exit"));
        break;
      case PRINT:
      case PRINTLINE:
        Type exprType = exprAST.getExprType();
        codeGenerator.addInstruction(new MovInstruction(R0, register));
        switch (exprType.getTypeToken()) {
          case ARRAY:
            ArrayType arrayType = (ArrayType) exprType;
            TypeToken typeToken = arrayType.getElemType().getTypeToken();
            if (typeToken == TypeToken.CHAR) {
              codeGenerator.addInstruction(new BranchInstruction(true, "p_print_string"));
              codeGenerator.setPrint_string();
            } else {
              codeGenerator.addInstruction(new BranchInstruction(true, "p_print_reference"));
              codeGenerator.setPrint_pointer();
            }
            break;
          case NULL:
          case PAIR:
            codeGenerator.addInstruction(new BranchInstruction(true, "p_print_reference"));
            codeGenerator.setPrint_pointer();
            break;
          case BOOL:
            codeGenerator.addInstruction(new BranchInstruction(true, "p_print_bool"));
            codeGenerator.setPrint_bool();
            break;
          case CHAR:
            codeGenerator.addInstruction(new BranchInstruction(true, "putchar"));
            break;
          case INT:
            codeGenerator.addInstruction(new BranchInstruction(true, "p_print_int"));
            codeGenerator.setPrint_int();
            break;
          case STRING:
            codeGenerator.addInstruction(new BranchInstruction(true, "p_print_string"));
            codeGenerator.setPrint_string();
            break;
        }
        if (normalStatAST.getNormalStatType() == NormalStatToken.PRINTLINE) {
          codeGenerator.addInstruction(new BranchInstruction(true, "p_print_ln"));
          codeGenerator.setPrint_line();
        }
        break;
      case RETURN:
        codeGenerator.addInstruction(new MovInstruction(R0, register));
        // Reset stack offset:
        int totalOffset = this.stackManager.getFuncOffset();
        if (totalOffset != 0) {
          while (totalOffset > MAX_OFFSET) {
            codeGenerator.addInstruction(new AddInstruction(SP, SP, new IntImmValue(MAX_OFFSET)));
            totalOffset -= MAX_OFFSET;
          }
          codeGenerator.addInstruction(new AddInstruction(SP, SP, new IntImmValue(totalOffset)));
        }
        codeGenerator.addInstruction(new PopInstruction(PC));
        break;
    }
    // Free register
    registerAllocator.addUnusedReg(register);
  }

  public void visitReadStat(ReadStatAST readStatAST) {
    AssignLhsAST assignLhsAST = readStatAST.getContent();
    Register register = visitAssignLhs(assignLhsAST);
    Type type = assignLhsAST.getLhsType();
    codeGenerator.addInstruction(new MovInstruction(R0, register));
    switch (type.getTypeToken()) {
      case CHAR:
        codeGenerator.addInstruction(new BranchInstruction(true, "p_read_char"));
        codeGenerator.setRead_char();
        break;
      case INT:
        codeGenerator.addInstruction(new BranchInstruction(true, "p_read_int"));
        codeGenerator.setRead_int();
        break;
    }
    // Free register
    registerAllocator.addUnusedReg(register);
  }

  private void setUpScopeAndVisit(List<StatAST> statements, int offset) {
    // Set up stack offset for variables:
    int totalOffset = offset;
    if (totalOffset != 0) {
      while (totalOffset > MAX_OFFSET) {
        codeGenerator.addInstruction(new SubInstruction(SP, SP, new IntImmValue(MAX_OFFSET)));
        totalOffset -= MAX_OFFSET;
      }
      codeGenerator.addInstruction(new SubInstruction(SP, SP, new IntImmValue(totalOffset)));
    }
    // Visit statements:
    for (StatAST statAST : statements) {
      visitStat(statAST);
    }
    // Reset stack offset:
    totalOffset = offset;
    if (totalOffset != 0) {
      while (totalOffset > MAX_OFFSET) {
        codeGenerator.addInstruction(new AddInstruction(SP, SP, new IntImmValue(MAX_OFFSET)));
        totalOffset -= MAX_OFFSET;
      }
      codeGenerator.addInstruction(new AddInstruction(SP, SP, new IntImmValue(totalOffset)));
    }
  }

  /* Expression */

  public Register visitExpr(ExprAST exprAST) {
    switch (exprAST.getExprToken()) {
      case ARRAYELEM:
        return visitArrayElemExpr((ArrayElemAST) exprAST);
      case BINOP:
        return visitBinopExpr((BinopExprAST) exprAST);
      case BOOLLIT:
        return visitBoollitExpr((BoollitExprLeaf) exprAST);
      case CHARLIT:
        return visitCharlitExpr((CharlitExprLeaf) exprAST);
      case IDENTIFIER:
        return visitIdentExpr((IdentifierLeaf) exprAST);
      case INTLIT:
        return visitIntlitExpr((IntlitExprLeaf) exprAST);
      case PAIRLIT:
        return visitPairlitExpr();
      case STRLIT:
        return visitStrlitExpr((StrlitExprLeaf) exprAST);
      case UNOP:
        return visitUnopExpr((UnopExprAST) exprAST);
      default:
        return null;
    }
  }

  public Register visitUnopExpr(UnopExprAST unopExprAST) {
    Register register = visitExpr(unopExprAST.getExpression());
    UnopToken token = unopExprAST.getOperator();
    switch (token) {
      case NOT:
        codeGenerator.addInstruction(new EorInstruction(register, register, new IntImmValue(1)));
        break;
      case NEG:
        codeGenerator.addInstruction(new RsbInstruction(true, register, register, new IntImmValue(0)));
        codeGenerator.addInstruction(new BranchInstruction(true, "VS", "p_throw_overflow_error"));
        codeGenerator.setErr_overflow();
        break;
      case ORD:
      case CHR:
        break;
      case LEN:
        codeGenerator.addInstruction(new LdrInstruction(register, new PreIndex(register)));
        break;
    }
    return register;
  }

  public Register visitBinopExpr(BinopExprAST binopExprAST) {
    BinopToken operator = binopExprAST.getOperator();
    // Get the left and right AST for binop expression AST
    ExprAST left = binopExprAST.getLeft();
    ExprAST right = binopExprAST.getRight();
    // Get the left and right register that stores the result of visiting the left and right hand side of binary expr
    Register register1 = visitExpr(left);
    if (register1.equals(R10)) {
      codeGenerator.addInstruction(new PushInstruction(R10));
      registerAllocator.addUnusedReg(R10);
    }
    Register register2 = visitExpr(right);
    if (register1.equals(R10) && register2.equals(R10)) {
      codeGenerator.addInstruction(new PopInstruction(finalReg));
      register1 = registerAllocator.AllocReg();
    }
    switch (operator) {
      case EQ:
        codeGenerator.addInstruction(new CmpInstruction(register1, register2));
        codeGenerator.addInstruction(
            new MovInstruction("EQ", false, register1, new IntImmValue(1)));
        codeGenerator.addInstruction(
            new MovInstruction("NE", false, register1, new IntImmValue(0)));
        break;
      case NEQ:
        codeGenerator.addInstruction(new CmpInstruction(register1, register2));
        codeGenerator.addInstruction(
            new MovInstruction("NE", false, register1, new IntImmValue(1)));
        codeGenerator.addInstruction(
            new MovInstruction("EQ", false, register1, new IntImmValue(0)));
        break;
      case GT:
        codeGenerator.addInstruction(new CmpInstruction(register1, register2));
        codeGenerator.addInstruction(
            new MovInstruction("GT", false, register1, new IntImmValue(1)));
        codeGenerator.addInstruction(
            new MovInstruction("LE", false, register1, new IntImmValue(0)));
        break;
      case LTE:
        codeGenerator.addInstruction(new CmpInstruction(register1, register2));
        codeGenerator.addInstruction(
            new MovInstruction("LE", false, register1, new IntImmValue(1)));
        codeGenerator.addInstruction(
            new MovInstruction("GT", false, register1, new IntImmValue(0)));
        break;
      case LT:
        codeGenerator.addInstruction(new CmpInstruction(register1, register2));
        codeGenerator.addInstruction(
            new MovInstruction("LT", false, register1, new IntImmValue(1)));
        codeGenerator.addInstruction(
            new MovInstruction("GE", false, register1, new IntImmValue(0)));
        break;
      case GTE:
        codeGenerator.addInstruction(new CmpInstruction(register1, register2));
        codeGenerator.addInstruction(
            new MovInstruction("GE", false, register1, new IntImmValue(1)));
        codeGenerator.addInstruction(
            new MovInstruction("LT", false, register1, new IntImmValue(0)));
        break;
      case ADD:
        codeGenerator.addInstruction(new AddInstruction(true, register1, register1, register2));
        codeGenerator.addInstruction(new BranchInstruction(true, "VS", "p_throw_overflow_error"));
        codeGenerator.setErr_overflow();
        break;
      case OR:
        codeGenerator.addInstruction(new OrrInstruction(register1, register1, register2));
        break;
      case AND:
        codeGenerator.addInstruction(new AndInstruction(register1, register1, register2));
        break;
      case MOD:
        codeGenerator.addInstruction(new MovInstruction(new Register(0), register1));
        codeGenerator.addInstruction(new MovInstruction(new Register(1), register2));
        codeGenerator.addInstruction(new BranchInstruction(true, "p_check_divide_by_zero"));
        codeGenerator.setErr_division();
        codeGenerator.addInstruction(new BranchInstruction(true, "__aeabi_idivmod"));
        codeGenerator.addInstruction(new MovInstruction(register1, R1));
        break;
      case Div:
        codeGenerator.addInstruction(new MovInstruction(new Register(0), register1));
        codeGenerator.addInstruction(new MovInstruction(new Register(1), register2));
        codeGenerator.addInstruction(new BranchInstruction(true, "p_check_divide_by_zero"));
        codeGenerator.setErr_division();
        codeGenerator.addInstruction(new BranchInstruction(true, "__aeabi_idiv"));
        codeGenerator.addInstruction(new MovInstruction(register1, R0));
        break;
      case MUL:
        codeGenerator.addInstruction(
            new SmullInstruction("", false, register1, register2, register1, register2));
        codeGenerator.addInstruction(
            new CmpInstruction(
                register2,
                new ShiftedRegister(register1, new Shift(ShiftType.ASR, new IntImmValue(31)))));
        codeGenerator.addInstruction(new BranchInstruction(true, "NE", "p_throw_overflow_error"));
        codeGenerator.setErr_overflow();
        break;
      case SUB:
        codeGenerator.addInstruction(new SubInstruction(true, register1, register1, register2));
        codeGenerator.addInstruction(new BranchInstruction(true, "VS", "p_throw_overflow_error"));
        codeGenerator.setErr_overflow();
        break;
    }
    if (register1.equals(finalReg)) {
      codeGenerator.addInstruction(new MovInstruction(R10, finalReg));
      registerAllocator.addUnusedReg(finalReg);
      return register2;
    } else {
      registerAllocator.addUnusedReg(register2);
      return register1;
    }
  }

  /* Visit different types of literals */

  public Register visitBoollitExpr(BoollitExprLeaf boollitExprLeaf) {
    Register register = registerAllocator.AllocReg();
    int value = 0;
    if (boollitExprLeaf.isValue()) {
      value = 1;
    }
    codeGenerator.addInstruction(new MovInstruction(register, new IntImmValue(value)));
    return register;
  }

  public Register visitCharlitExpr(CharlitExprLeaf charlitExprLeaf) {
    Register register = registerAllocator.AllocReg();
    char character = charlitExprLeaf.getValue();
    if (character == '\0') {
      codeGenerator.addInstruction(new MovInstruction(register, new IntImmValue(0)));
    } else {
      codeGenerator.addInstruction(new MovInstruction(register, new CharImmValue(character)));
    }
    return register;
  }


  public Register visitIntlitExpr(IntlitExprLeaf intlitExprLeaf) {
    Register register = registerAllocator.AllocReg();
    codeGenerator.addInstruction(
        new LdrInstruction(register, new IntExpression(intlitExprLeaf.getValue())));
    return register;
  }

  public Register visitPairlitExpr() {
    Register register = registerAllocator.AllocReg();
    codeGenerator.addInstruction(new LdrInstruction(register, new IntExpression(0)));
    return register;
  }

  public Register visitStrlitExpr(StrlitExprLeaf strlitExprLeaf) {
    Register register = registerAllocator.AllocReg();
    int dataPosition = codeGenerator.addData(new Data(strlitExprLeaf.getValue()));
    codeGenerator.addInstruction(
        new LdrInstruction(register, new StringExpression("msg_" + dataPosition)));
    return register;
  }

  // When array elem appears on the RHS, it is an expression.
  public Register visitArrayElemExpr(ArrayElemAST arrayElemAST) {
    Register register1 = visitIdentLhs(arrayElemAST.getIdentifier());
    Type type = arrayElemAST.getExprType();
    Register register2;
    // Iterate through array elem AST to load array indices, and allow corresponding instructions to be printed recursively
    for (ExprAST exprAST : arrayElemAST.getExpressions()) {
      register2 = visitExpr(exprAST);
      codeGenerator.addInstruction(new LdrInstruction(register1, new PreIndex(register1)));
      codeGenerator.addInstruction(new MovInstruction(R0, register2));
      codeGenerator.addInstruction(new MovInstruction(R1, register1));
      codeGenerator.addInstruction(new BranchInstruction(true, "p_check_array_bounds"));
      codeGenerator.setErr_array_index();
      codeGenerator.addInstruction(new AddInstruction(register1, register1, new IntImmValue(4)));
      if (type.getOffset() != CHAR_BOOL_OFFSET) {
        codeGenerator.addInstruction(
            new AddInstruction(
                register1,
                register1,
                new ShiftedRegister(register2, new Shift(ShiftType.LSL, new IntImmValue(2)))));
      } else {
        codeGenerator.addInstruction(new AddInstruction(register1, register1, register2));
      }
      // Free register
      registerAllocator.addUnusedReg(register2);
    }
    if (type.getOffset() != CHAR_BOOL_OFFSET) {
      codeGenerator.addInstruction(new LdrInstruction(register1, new PreIndex(register1)));
    } else {
      codeGenerator.addInstruction(
              new LdrInstruction(LoadStoreType.SB, register1, new PreIndex(register1)));
    }
    return register1;
  }

  public Register visitIdentExpr(IdentifierLeaf identifierLeaf) {
    Register register = registerAllocator.AllocReg();
    String varName = identifierLeaf.getText();
    Type type = identifierLeaf.getExprType();
    int stackOffset = stackManager.lookupAllOffset(varName);
    if (type.getOffset() == CHAR_BOOL_OFFSET) {
      codeGenerator.addInstruction(
          new LdrInstruction(
              LoadStoreType.SB, register, new PreIndex(SP, new IntImmValue(stackOffset))));
    } else {
      codeGenerator.addInstruction(
          new LdrInstruction(register, new PreIndex(SP, new IntImmValue(stackOffset))));
    }
    return register;
  }

  /* Assign rhs */

  public Register visitAssignRhs(AssignRhsAST assignRhsAST) {
    switch (assignRhsAST.getRhsToken()) {
      case ARRAY_LIT:
        return visitArraylit((ArraylitRhsAST) assignRhsAST);
      case CALL:
        return visitCall((CallRhsAST) assignRhsAST);
      case EXPR:
        return visitExpr((ExprAST) assignRhsAST);
      case NEWPAIR:
        return visitNewPair((NewpairRhsAST) assignRhsAST);
      case PAIRELEMFST:
        return visitFSTRhs((PairelemFstAST) assignRhsAST);
      case PAIRELEMSND:
        return visitSndRhs((PairelemSndAST) assignRhsAST);
    }
    return null;
  }

  public Register visitArraylit(ArraylitRhsAST arraylitRhsAST) {
    int memSize = arraylitRhsAST.getMemSize();
    codeGenerator.addInstruction(new LdrInstruction(R0, new IntExpression(memSize)));
    codeGenerator.addInstruction(new BranchInstruction(true, "malloc"));
    Register register1 = registerAllocator.AllocReg();
    codeGenerator.addInstruction(new MovInstruction(register1, R0));
    int currOffset = 4;
    int offset = ((ArrayType) arraylitRhsAST.getRhsType()).getElemType().getOffset();
    Register register2;
    // Iterate through expressions for the array literal AST to print instructions recursively
    for (ExprAST exprAST : arraylitRhsAST.getExpressions()) {
      register2 = visitExpr(exprAST);
      if (offset == CHAR_BOOL_OFFSET) {
        codeGenerator.addInstruction(
            new StrInstruction(
                LoadStoreType.B, register2, new PreIndex(register1, new IntImmValue(currOffset))));
      } else {
        codeGenerator.addInstruction(
            new StrInstruction(register2, new PreIndex(register1, new IntImmValue(currOffset))));
      }
      registerAllocator.addUnusedReg(register2);
      currOffset += offset;
    }
    register2 = registerAllocator.AllocReg();
    int length = arraylitRhsAST.getLength();
    codeGenerator.addInstruction(new LdrInstruction(register2, new IntExpression(length)));
    codeGenerator.addInstruction(new StrInstruction(register2, new PreIndex(register1)));
    registerAllocator.addUnusedReg(register2);
    return register1;
  }

  public Register visitCall(CallRhsAST callRhsAST) {
    String funcName = callRhsAST.getIdentifier().getText();
    List<ExprAST> expressions = callRhsAST.getArgumentList();
    Register register;
    // Store the total argument offsets of the function in order to shift the stack pointer correctly
    int argTotalOffset = 0;
    for (int i = expressions.size() - 1; i >= 0; i--) {
      ExprAST expression = expressions.get(i);
      register = visitExpr(expression);
      int offset = expression.getExprType().getOffset();
      if (offset != CHAR_BOOL_OFFSET) {
        codeGenerator.addInstruction(
            new StrInstruction(register, new PreIndex(SP, new IntImmValue(-OTHER_OFFSET), true)));
        argTotalOffset += OTHER_OFFSET;
        stackManager.addCurrOffset(OTHER_OFFSET);
      } else {
        codeGenerator.addInstruction(
            new StrInstruction(
                LoadStoreType.B, register, new PreIndex(SP, new IntImmValue(-CHAR_BOOL_OFFSET), true)));
        // Add each offset of the argument in the function to the total offset
        argTotalOffset += CHAR_BOOL_OFFSET;
        stackManager.addCurrOffset(CHAR_BOOL_OFFSET);
      }
      // Free register
      registerAllocator.addUnusedReg(register);
    }
    register = registerAllocator.AllocReg();
    codeGenerator.addInstruction(new BranchInstruction(true, funcName));
    codeGenerator.addInstruction(new AddInstruction(SP, SP, new IntImmValue(argTotalOffset)));
    codeGenerator.addInstruction(new MovInstruction(register, R0));
    stackManager.addCurrOffset(-argTotalOffset);
    return register;
  }

  public Register visitNewPair(NewpairRhsAST newpairRhsAST) {
    // Create pair:
    codeGenerator.addInstruction(new LdrInstruction(R0, new IntExpression(8)));
    codeGenerator.addInstruction(new BranchInstruction(true, "malloc"));
    Register register1 = registerAllocator.AllocReg();
    codeGenerator.addInstruction(new MovInstruction(register1, R0));
    // Create fst:
    Register register2 = visitExpr(newpairRhsAST.getFirst());
    int offset = ((PairType) newpairRhsAST.getRhsType()).getFirst().getOffset();
    codeGenerator.addInstruction(new LdrInstruction(R0, new IntExpression(offset)));
    codeGenerator.addInstruction(new BranchInstruction(true, "malloc"));
    if (offset == CHAR_BOOL_OFFSET) {
      codeGenerator.addInstruction(
          new StrInstruction(LoadStoreType.B, register2, new PreIndex(R0)));
    } else {
      codeGenerator.addInstruction(new StrInstruction(register2, new PreIndex(R0)));
    }
    codeGenerator.addInstruction(new StrInstruction(R0, new PreIndex(register1)));
    registerAllocator.addUnusedReg(register2);
    // Create snd:
    register2 = visitExpr(newpairRhsAST.getSecond());
    offset = ((PairType) newpairRhsAST.getRhsType()).getSecond().getOffset();
    codeGenerator.addInstruction(new LdrInstruction(R0, new IntExpression(offset)));
    codeGenerator.addInstruction(new BranchInstruction(true, "malloc"));
    if (offset == CHAR_BOOL_OFFSET) {
      codeGenerator.addInstruction(
          new StrInstruction(LoadStoreType.B, register2, new PreIndex(R0)));
    } else {
      codeGenerator.addInstruction(new StrInstruction(register2, new PreIndex(R0)));
    }
    codeGenerator.addInstruction(
        new StrInstruction(R0, new PreIndex(register1, new IntImmValue(OTHER_OFFSET))));
    registerAllocator.addUnusedReg(register2);
    return register1;
  }

  // First element of a pair on the RHS
  public Register visitFSTRhs(PairelemFstAST pairelemFstAST) {
    Register register = visitExpr(pairelemFstAST.getExpression());
    int offset = pairelemFstAST.getRhsType().getOffset();
    codeGenerator.addInstruction(new MovInstruction(R0, register));
    codeGenerator.addInstruction(new BranchInstruction(true, "p_check_null_pointer"));
    codeGenerator.setErr_nullRef();
    codeGenerator.addInstruction(new LdrInstruction(register, new PreIndex(register)));
    if (offset != CHAR_BOOL_OFFSET) {
      codeGenerator.addInstruction(new LdrInstruction(register, new PreIndex(register)));
    } else {
      codeGenerator.addInstruction(
          new LdrInstruction(LoadStoreType.SB, register, new PreIndex(register)));
    }
    return register;
  }

  // Second element of a pair on the RHS
  public Register visitSndRhs(PairelemSndAST pairelemSndAST) {
    Register register = visitExpr(pairelemSndAST.getExpression());
    int offset = pairelemSndAST.getRhsType().getOffset();
    codeGenerator.addInstruction(new MovInstruction(R0, register));
    codeGenerator.addInstruction(new BranchInstruction(true, "p_check_null_pointer"));
    codeGenerator.setErr_nullRef();
    codeGenerator.addInstruction(
        new LdrInstruction(register, new PreIndex(register, new IntImmValue(OTHER_OFFSET))));
    if (offset != CHAR_BOOL_OFFSET) {
      codeGenerator.addInstruction(new LdrInstruction(register, new PreIndex(register)));
    } else {
      codeGenerator.addInstruction(
          new LdrInstruction(LoadStoreType.SB, register, new PreIndex(register)));
    }
    return register;
  }

  /* Assign lhs */

  public Register visitAssignLhs(AssignLhsAST assignLhsAST) {
    switch (assignLhsAST.getLhsToken()) {
      case ARRAYELEM:
        return visitArrayElemLhs((ArrayElemAST) assignLhsAST);
      case IDENT:
        return visitIdentLhs((IdentifierLeaf) assignLhsAST);
      case PAIRELEMFST:
        return visitFstLhs((PairelemFstAST) assignLhsAST);
      case PAIRELEMSND:
        return visitSndLhs((PairelemSndAST) assignLhsAST);
      default:
        return null;
    }
  }

  // Array elem on LHS(different from RHS as an expression)
  public Register visitArrayElemLhs(ArrayElemAST arrayElemAST) {
    Register register1 = registerAllocator.AllocReg();
    String varName = arrayElemAST.getIdentifier().getText();
    codeGenerator.addInstruction(
        new AddInstruction(register1, SP, new IntImmValue(stackManager.lookupAllOffset(varName))));
    Register register2;
    // Iterate through array elem AST to load array indices, and allow corresponding instructions to be printed recursively
    for (ExprAST exprAST : arrayElemAST.getExpressions()) {
      register2 = visitExpr(exprAST);
      codeGenerator.addInstruction(new LdrInstruction(register1, new PreIndex(register1)));
      codeGenerator.addInstruction(new MovInstruction(R0, register2));
      codeGenerator.addInstruction(new MovInstruction(R1, register1));
      codeGenerator.addInstruction(new BranchInstruction(true, "p_check_array_bounds"));
      codeGenerator.setErr_array_index();
      codeGenerator.addInstruction(new AddInstruction(register1, register1, new IntImmValue(4)));
      Type type = arrayElemAST.getExprType();
      if (type.getOffset() == OTHER_OFFSET) {
        codeGenerator.addInstruction(
            new AddInstruction(
                register1,
                register1,
                new ShiftedRegister(register2, new Shift(ShiftType.LSL, new IntImmValue(2)))));
      } else {
        codeGenerator.addInstruction(new AddInstruction(register1, register1, register2));
      }
      registerAllocator.addUnusedReg(register2);
    }
    return register1;
  }

  // Used only in read:
  public Register visitIdentLhs(IdentifierLeaf identifierLeaf) {
    Register register = registerAllocator.AllocReg();
    codeGenerator.addInstruction(
        new AddInstruction(
            register, SP, new IntImmValue(stackManager.lookupAllOffset(identifierLeaf.getText()))));
    return register;
  }

  // First element of a pair on the LHS
  public Register visitFstLhs(PairelemFstAST pairelemFstAST) {
    Register register = visitExpr(pairelemFstAST.getExpression());
    codeGenerator.addInstruction(new MovInstruction(R0, register));
    codeGenerator.addInstruction(new BranchInstruction(true, "p_check_null_pointer"));
    codeGenerator.setErr_nullRef();
    codeGenerator.addInstruction(new LdrInstruction(register, new PreIndex(register)));
    return register;
  }

  // Second element of a pair on the LHS
  public Register visitSndLhs(PairelemSndAST pairelemSndAST) {
    Register register = visitExpr(pairelemSndAST.getExpression());
    codeGenerator.addInstruction(new MovInstruction(R0, register));
    codeGenerator.addInstruction(new BranchInstruction(true, "p_check_null_pointer"));
    codeGenerator.setErr_nullRef();
    codeGenerator.addInstruction(
        new LdrInstruction(register, new PreIndex(register, new IntImmValue(OTHER_OFFSET))));
    return register;
  }
}
