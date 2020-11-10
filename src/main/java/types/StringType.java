package types;

public final class StringType implements Type {

  public StringType() {}

  @Override
  public int getOffset() {
    return 4;
  }

  @Override
  public TypeToken getTypeToken() {
    return TypeToken.STRING;
  }

  @Override
  public int countChildren() {
    return 1;
  }

  @Override
  public String print() {
    return String.valueOf(TypeToken.STRING);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj instanceof StringType || obj instanceof AnyType;
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
