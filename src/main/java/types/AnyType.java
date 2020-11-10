package types;

public final class AnyType extends BaseType {

  public AnyType() {
    super(TypeToken.ANY);
  }

  @Override
  public String toString() {
    return "Any Type";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    return obj instanceof Type;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public int getOffset() {
    return 0;
  }
}
