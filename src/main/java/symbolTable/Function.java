package symbolTable;

import types.Type;
import types.TypeToken;

import java.util.List;

public class Function implements Node {

  private String name;
  private Type returnType;
  private List<Type> paramTypes;

  public Function(String name, Type returnType, List<Type> paramTypes) {
    this.name = name;
    this.returnType = returnType;
    this.paramTypes = paramTypes;
  }

  public Type getType() {
    return returnType;
  }

  public String getName() {
    return name;
  }

  public List<Type> getParamTypes() {
    return paramTypes;
  }

  public Type getParamType(int position) {
    return paramTypes.get(position);
  }

  @Override
  public TypeToken getTypeToken() {
    return TypeToken.FUNCTION;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof Function) {
      Function function = (Function) obj;
      if (this.paramTypes.size() != function.paramTypes.size()) {
        return false;
      }
      for (int i = 0; i < paramTypes.size(); i++) {
        if (!this.paramTypes.get(i).equals(function.paramTypes.get(i))) {
          return false;
        }
      }
      return this.name.equals(function.getName()) && this.returnType.equals(function.getType());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.length() + paramTypes.size();
  }
}
