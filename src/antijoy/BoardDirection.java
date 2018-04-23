package antijoy;

public enum BoardDirection {
    RightDown, LeftDown, Left, LeftUp, RightUp, Right;

    BoardDirection nextRR() {
        switch (this) {
            case LeftDown:
                return Left;
            case Left:
                return LeftUp;
            case LeftUp:
                return RightUp;
            case RightUp:
                return Right;
            case Right:
                return RightDown;
            case RightDown:
                return LeftDown;
        }
        throw new UnsupportedOperationException();
    }
}
