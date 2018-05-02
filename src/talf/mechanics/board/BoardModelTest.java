package talf.mechanics.board;

import org.junit.Test;
import talf.mechanics.Coordinate;

import java.util.HashMap;
import java.util.IdentityHashMap;

public class BoardModelTest {
    @Test
    public void iterator() {
        BoardModel boardModel = new BoardModel(new byte[][]{
            new byte[]{2, 2},
            new byte[]{2, 2}
        }, -1, -1, 3, 0, 0);
        BoardModel.PiecesIterator i = boardModel.new PiecesIterator((byte) 2);
        assert i.hasNext();
        assert i.next().equals(new Coordinate(0, 0));
        assert i.hasNext();
        assert i.next().equals(new Coordinate(1, 0));
        assert i.hasNext();
        assert i.next().equals(new Coordinate(0, 1));
        assert i.hasNext();
        assert i.next().equals(new Coordinate(1, 1));
        assert !i.hasNext();
    }

    @Test
    public void identityHashmap() {
        IdentityHashMap<Coordinate, Object> map1 = new IdentityHashMap<>();

        Coordinate a1 = new Coordinate(5, 5);
        Coordinate a2 = new Coordinate(5, 5);

        assert a1.equals(a2);
        assert a1 != a2;

        Object a1_ = "A1";
        Object a2_ = "A2";
        map1.put(a1, a1_);
        map1.put(a2, a2_);

        assert map1.get(a1) == a1_;
        assert map1.get(a2) == a2_;

        HashMap<Coordinate, Object> map2 = new HashMap<>();
        map2.put(a1, a1_);
        map2.put(a2, a2_);

        assert map2.get(a1) == a2_;
        assert map2.get(a2) == a2_;
    }
}