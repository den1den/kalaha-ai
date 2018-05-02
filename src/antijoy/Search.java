package antijoy;

import java.util.List;

public class Search {
    Board board;

    public Search(Board board) {
        this.board = board;
    }

    public void search() {
        List<Move> moves = this.board.getMoves(0);
        for (int i = 0; i < moves.size(); i++) {
            Move m = moves.get(i);

            Board board = new Board(this.board);
            board.move(0, m);
            System.out.println("board" + i + " = " + board);

        }
    }
}
