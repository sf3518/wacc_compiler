package types;

public enum TypeToken {
  FUNCTION {
    @Override
    public String toString() {
      return "FUNCTION";
    }
  },
  INT {
    @Override
    public String toString() {
      return "INT";
    }
  },
  BOOL {
    @Override
    public String toString() {
      return "BOOL";
    }
  },
  CHAR {
    @Override
    public String toString() {
      return "CHAR";
    }
  },
  STRING {
    @Override
    public String toString() {
      return "STRING";
    }
  },
  ARRAY {
    @Override
    public String toString() {
      return "ARRAY";
    }
  },
  PAIR {
    @Override
    public String toString() {
      return "PAIR";
    }
  },
  NULL {
    @Override
    public String toString() {
      return "NULL";
    }
  },
  ANY {
    @Override
    public String toString() {
      return "ANY";
    }
  }
}
