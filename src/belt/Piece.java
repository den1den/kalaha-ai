package belt;

/**
 * An placement of a certain type
 */
public class Piece {
    PieceType pieceType;
    Orientation outputDirection;
    Resource outputResource;

    public Piece(PieceType pieceType, Orientation outputDirection, Resource outputResource) {
        assert outputDirection != null || pieceType.cannotRotate();
        assert outputResource != null || pieceType.hasNoOutput();
        assert pieceType.canOutput(outputResource);
        this.pieceType = pieceType;
        this.outputDirection = outputDirection;
        this.outputResource = outputResource;
    }

    public Piece(PieceType pieceType) {
        this(pieceType, null, null);
    }

    public Resource getResource() {
        return outputResource;
    }

    public enum Orientation {
        N, E, S, W;

        public int getSourceX(int targetX) {
            switch (this) {
                case E:
                    return targetX - 1;
                case W:
                    return targetX + 1;
                case N:
                case S:
                    return targetX;
            }
            throw new IllegalArgumentException();
        }

        public int getSourceY(int targetY) {
            switch (this) {
                case S:
                    return targetY - 1;
                case N:
                    return targetY + 1;
                case E:
                case W:
                    return targetY;
            }
            throw new IllegalArgumentException();
        }
    }

    public enum PieceType {
        STARTER, SELLER, ROLLER, FURNANCE, CUTTER, DRAWER, CRAFTER,
        ROBOTIC_ARM, LEFT_SELECTOR, RIGHT_SELECTOR, TIMED_ROLLER, MULTI_SELECTOR,
        HYD_PRESS, FILTERED_ROBOTIC_ARM, SPLITTER, L_SPLITTER, R_SPLITTER, _3WAY_SPLITTER;

        public static PieceType get(Resource output) {
            Resource.State state = output.getState();
            switch (state) {
                case RAW:
                    return STARTER;
                case COMPOSITE:
                    return CRAFTER;
                case LIQUID:
                    return FURNANCE;
                case WIRE:
                    return DRAWER;
                case GEAR:
                    return CUTTER;
                case PLATE:
                    return HYD_PRESS;
            }
            throw new IllegalArgumentException();
        }

        public int getPrice() {
            switch (this) {
                case STARTER:
                    return 1000;
                case SELLER:
                    return 5000;
                case ROLLER:
                    return 300;
                case FURNANCE:
                case CUTTER:
                case DRAWER:
                    return 10000;
                case CRAFTER:
                    return 20000;
                case ROBOTIC_ARM:
                    return 15000;
                case LEFT_SELECTOR:
                case RIGHT_SELECTOR:
                    return 50000;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        public boolean canOutput(Resource outputResource) {
            switch (this) {
                case STARTER:
                    return outputResource.getState() == Resource.State.RAW;
                case FURNANCE:
                    return outputResource.getState() == Resource.State.LIQUID;
                case CUTTER:
                    return outputResource.getState() == Resource.State.GEAR;
                case DRAWER:
                    return outputResource.getState() == Resource.State.WIRE;
                case HYD_PRESS:
                    return outputResource.getState() == Resource.State.PLATE;
                case CRAFTER:
                    return outputResource.getState() == Resource.State.COMPOSITE;
                default:
                    return true;
            }
        }

        public boolean canChangeMaterial() {
            switch (this) {
                case FURNANCE:
                case CUTTER:
                case DRAWER:
                case HYD_PRESS:
                case CRAFTER:
                    return true;
                default:
                    return false;
            }
        }

        public boolean hasNoOutput() {
            switch (this) {
                case SELLER:
                    return true;
                default:
                    return false;
            }
        }

        public boolean cannotRotate() {
            switch (this) {
                case SELLER:
                    return true;
                default:
                    return false;
            }
        }
    }
}
