package types;

public abstract class BaseType implements Type {

  private final TypeToken type;

  public BaseType(TypeToken type) {
    this.type = type;
  }

  @Override
  public TypeToken getTypeToken() {
    return type;
  }

  @Override
  public int countChildren() {
    return 0;
  }

  @Override
  public String print() {
    return String.valueOf(type);
  }
}
