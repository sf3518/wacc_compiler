package error;

import error.semanticError.*;
import error.syntaxError.BadFormatError;
import error.syntaxError.MismatchedReturnError;
import error.syntaxError.SyntaxError;

import java.util.ArrayList;
import java.util.List;

import static main.Main.EXIT_SEMANTIC_ERROR;

public class ErrorHandler {

  List<String> errorMsgList = new ArrayList<>();

  public void printErrorMessage() {
    StringBuilder builder = new StringBuilder();
    builder.append("Errors detected during compilation! Exit code 100 returned.\n");
    for (String message : errorMsgList) {
      builder.append(message);
    }
    builder
        .append(errorMsgList.size())
        .append(" parser error(s) detected, no further compilation attempted.");
    System.err.println(builder.toString());
  }

  public void report(Error error) {
    if (error instanceof SyntaxError) {
      if (error instanceof BadFormatError) {
        reportBadFormatError((BadFormatError) error);
      } else if (error instanceof MismatchedReturnError) {
        reportMismatchedReturnError((MismatchedReturnError) error);
      }
    } else if (error instanceof SemanticError) {
      String errorMessage = "";
      if (error instanceof DoubleDeclError) {
        errorMessage = reportDoubleDeclError((DoubleDeclError) error);
      } else if (error instanceof IncompatibleTypeError) {
        errorMessage = reportIncompatibleTypeError((IncompatibleTypeError) error);
      } else if (error instanceof FunctionUndefinedError) {
        errorMessage = reportFunctionUndefinedError((FunctionUndefinedError) error);
      } else if (error instanceof UndeclaredVarError) {
        errorMessage = reportUndeclaredVarError((UndeclaredVarError) error);
      } else if (error instanceof InvalidPairelemAccessError) {
        errorMessage = reportInvalidPairelemAccessError((InvalidPairelemAccessError) error);
      } else if (error instanceof ReadError) {
        errorMessage = reportReadError((ReadError) error);
      } else if (error instanceof ReturnFromGlobalScopeError) {
        errorMessage = reportReturnFromGlobalScopeError((ReturnFromGlobalScopeError) error);
      } else if (error instanceof FunctionParamError) {
        errorMessage = reportFunctionParamError((FunctionParamError) error);
      }
      System.err.println(
          "Errors detected during compilation! Exit code 200 returned.\n" + errorMessage);
      System.exit(EXIT_SEMANTIC_ERROR);
    }
  }

  private void reportBadFormatError(BadFormatError error) {
    errorMsgList.add(
        "Integer value "
            + error.getOffendingToken()
            + " on line "
            + error.getLine()
            + " is badly formatted (either it has a badly defined sign or it is too large for a 32-bit signed integer).\n");
  }

  private void reportMismatchedReturnError(MismatchedReturnError error) {
    errorMsgList.add(
        "Syntactic Error at "
            + error.getLine()
            + ":"
            + error.getPosition()
            + " -- Function "
            + error.getOffendingToken()
            + " is not ended with a return or an exit statement.\n");
  }

  private String reportDoubleDeclError(DoubleDeclError error) {
    return "Semantic Error at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- \""
        + error.getOffendingToken()
        + "\" is already defined in this scope.";
  }

  private String reportIncompatibleTypeError(IncompatibleTypeError error) {
    if (error.getExpected() != null) {
      return "SemanticError at "
          + error.getLine()
          + ":"
          + error.getPosition()
          + " -- Incompatible type at \""
          + error.getOffendingToken()
          + "\" (expected: "
          + error.getExpected()
          + ", actual: "
          + error.getActual()
          + ").";
    }
    return "SemanticError at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- Incompatible type "
        + error.getActual();
  }

  private String reportFunctionUndefinedError(FunctionUndefinedError error) {
    return "SemanticError at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- Function "
        + error.getOffendingToken()
        + " is not defined in this scope";
  }

  private String reportUndeclaredVarError(UndeclaredVarError error) {
    return "SemanticError at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- Variable "
        + error.getOffendingToken()
        + " is not defined in this scope";
  }

  private String reportInvalidPairelemAccessError(InvalidPairelemAccessError error) {
    return "Semantic Error at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- Accessing element of a none pair";
  }

  private String reportReadError(ReadError error) {
    return "Semantic Error at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- Read argument not an integer or a character";
  }

  private String reportReturnFromGlobalScopeError(ReturnFromGlobalScopeError error) {
    return "Semantic Error at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- Cannot return from the global scope";
  }

  private String reportFunctionParamError(FunctionParamError error) {
    return "Semantic Error at "
        + error.getLine()
        + ":"
        + error.getPosition()
        + " -- Function call on "
        + error.getOffendingToken()
        + "has wrong argument number";
  }
}
