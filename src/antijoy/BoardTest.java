package antijoy;

import org.junit.jupiter.api.Test;

import static antijoy.BoardBuilder.Type.*;

public class BoardTest {

    @Test
    public void getMoves() {
        Board board = new BoardBuilder(new BoardBuilder.Type[][]{
                new BoardBuilder.Type[]{XXXXX, _0_, _0_, ___, ___, ___, ___},
                new BoardBuilder.Type[]{XXX, P30, _0_, B_0, ___, ___, ___},
                new BoardBuilder.Type[]{XXXXX, _0_, _0_, _0_, P00, ___, ___},
            new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, _1_, _1_, ___},
                new BoardBuilder.Type[]{___, ___, ___, _1_, B_1, ___, ___},
                new BoardBuilder.Type[]{_____, ___, ___, ___, ___, ___, ___},
            new BoardBuilder.Type[]{___, ___, ___, ___, ___, ___, ___}
        }).toBoard();

        System.out.println("board = \n" + board);
    }
}