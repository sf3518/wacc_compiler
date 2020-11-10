package ast.statement;

import ast.Tokens;

public enum NormalStatToken implements Tokens {
  FREE {
    @Override
    public String toString() {
      return "free";
    }
  },
  RETURN {
    @Override
    public String toString() {
      return "return";
    }
  },
  EXIT {
    @Override
    public String toString() {
      return "exit";
    }
  },
  PRINT {
    @Override
    public String toString() {
      return "print";
    }
  },
  PRINTLINE {
    @Override
    public String toString() {
      return "println";
    }
  }
}
