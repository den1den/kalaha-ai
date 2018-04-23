package antijoy;

import java.util.List;

public class Search {
    Board board;

    public Search(Board board) {
        this.board = board;
    }

    public void search() {
        List<int[]> moves = this.board.getMoves(0);
        for (int i = 0; i < moves.size(); i++) {
            int[] m = moves.get(i);

            Board board = new Board(this.board);
            board.move(0, m[0], m[1], m[2], m[3]);
        }
    }
}
