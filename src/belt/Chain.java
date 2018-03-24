package belt;

// useless?
public class Chain {
    private Piece.PieceType needsFieldType;
    private Resource.Material material;
    private Chain[] children;
    private int nextToSearch;

    public Chain(Piece.PieceType needsFieldType, Resource.Material material, Chain[] children) {
        this.needsFieldType = needsFieldType;
        this.material = material;
        this.children = children;
        nextToSearch = 0;
    }

    public Piece.PieceType getEndBlock() {
        return needsFieldType;
    }

    public Resource.Material getMaterialOutput() {
        return material;
    }

    public Chain[] getChildren() {
        return children;
    }

    public boolean hasNext() {
        return nextToSearch < children.length;
    }

    public Chain next() {
        return children[nextToSearch++];
    }

    public void reset() {
        for (int i = 0; i < children.length; i++) {
            children[i].reset();
        }
        nextToSearch = 0;
    }

    public Chain peek() {
        return children[nextToSearch];
    }

    public static class ChainBuilder {
        int elements = 0;

        Chain create(Resource root) {
            return construct(root);
        }

        private Chain construct(Resource resource) {
            if (resource.getMaterial() != Resource.Material.COMPOSITE) {
                return construct(resource.getState(), resource.getMaterial());
            }
            elements++;
            Resource[] recipe = resource.getRecipe();
            assert recipe.length > 0;
            Chain[] children = new Chain[recipe.length];
            for (int i = 0; i < recipe.length; i++) {
                children[i] = construct(recipe[i]);
            }
            return new Chain(Piece.PieceType.CRAFTER, resource.getMaterial(), children);
        }

        private Chain construct(Resource.State state, Resource.Material material) {
            elements++;
            switch (state) {
                case RAW:
                    return new Chain(Piece.PieceType.STARTER, material,
                        new Chain[0]);
                case GEAR:
                    return new Chain(Piece.PieceType.CUTTER, material,
                        new Chain[]{construct(Resource.State.RAW, material)});
                case WIRE:
                    return new Chain(Piece.PieceType.DRAWER, material,
                        new Chain[]{construct(Resource.State.RAW, material)});
                case LIQUID:
                    return new Chain(Piece.PieceType.FURNANCE, material,
                        new Chain[]{construct(Resource.State.RAW, material)});
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
}
