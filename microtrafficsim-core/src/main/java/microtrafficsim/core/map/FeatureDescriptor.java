package microtrafficsim.core.map;


import microtrafficsim.utils.hashing.FNVHashBuilder;

public class FeatureDescriptor {
    private String name;
    private Class<? extends FeaturePrimitive> type;


    public FeatureDescriptor(String name, Class<? extends FeaturePrimitive> type) {
        this.name = name;
        this.type = type;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Class<? extends FeaturePrimitive> getType() {
        return type;
    }

    public void setType(Class<? extends FeaturePrimitive> type) {
        this.type = type;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FeatureDescriptor))
            return false;

        FeatureDescriptor other = (FeatureDescriptor) obj;

        return this.name.equals(other.name)
                && this.type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return new FNVHashBuilder()
                .add(name)
                .add(type)
                .getHash();
    }
}
