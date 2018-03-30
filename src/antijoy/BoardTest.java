package antijoy;

import org.junit.Test;

import static antijoy.BoardBuilder.Type.*;

public class BoardTest {

    @Test
    public void getMoves() {
        Board board = new BoardBuilder(new BoardBuilder.Type[][]{
            new BoardBuilder.Type[]{___, __0, __0, ___, ___, ___, ___},
            new BoardBuilder.Type[]{___, P30, __0, B_0, ___, ___, ___},
            new BoardBuilder.Type[]{___, __0, __0, __0, P00, ___, ___},
            new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___},
            new BoardBuilder.Type[]{___, ___, ___, ___, __1, __1, ___},
            new BoardBuilder.Type[]{___, ___, ___, __1, B_1, ___, ___},
            new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___},
            new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___}
        }).toBoard();

        System.out.println("board = \n" + board);
    }
}