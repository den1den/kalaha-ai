package marblegame.gamemechanics;

import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/**
 * Created by dennis on 14-9-17.
 */
public class PossibleMoveIteratorTest {
    PossibleMoveIterator it;

    @org.junit.Test
    public void noNext() throws Exception {
        it = new PossibleMoveIterator(new int[]{0, 0, 0}, 0, 2);
        assert !it.hasNext();
        it = new PossibleMoveIterator(new int[]{}, 0, -1);
        assert !it.hasNext();
    }

    @org.junit.Test
    public void next() throws Exception {
        it = new PossibleMoveIterator(new int[]{1, 1, 1}, 0, 2);
        assert it.hasNext();
        Assert.assertEquals(0, (int) it.next());
        assert it.hasNext();
        Assert.assertEquals(1, (int) it.next());
        assert it.hasNext();
        Assert.assertEquals(2, (int) it.next());
        assert !it.hasNext();
        try {
            it.next();
            Assert.fail();
        } catch (NoSuchElementException e) {

        }
    }

    @org.junit.Test
    public void missing0() throws Exception {
        it = new PossibleMoveIterator(new int[]{0, 1, 1}, 0, 2);
        assert it.hasNext();
        Assert.assertEquals(1, (int) it.next());
        assert it.hasNext();
        Assert.assertEquals(2, (int) it.next());
        assert !it.hasNext();
    }

    @org.junit.Test
    public void missing1() throws Exception {
        it = new PossibleMoveIterator(new int[]{1, 0, 1}, 0, 2);
        assert it.hasNext();
        Assert.assertEquals(0, (int) it.next());
        assert it.hasNext();
        Assert.assertEquals(2, (int) it.next());
        assert !it.hasNext();
    }

    @org.junit.Test
    public void missing2() throws Exception {
        it = new PossibleMoveIterator(new int[]{1, 1, 0}, 0, 2);
        assert it.hasNext();
        Assert.assertEquals(0, (int) it.next());
        assert it.hasNext();
        Assert.assertEquals(1, (int) it.next());
        assert !it.hasNext();
    }

    @Test
    public void wrongConstr() throws Exception {
        try {
            it = new PossibleMoveIterator(new int[]{1}, 0, 0);
        } catch (IndexOutOfBoundsException e) {
            Assert.fail();
        }
        try {
            it = new PossibleMoveIterator(new int[]{1}, 0, 1);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            it = new PossibleMoveIterator(new int[]{1}, -1, 0);
            Assert.fail();
        } catch (IndexOutOfBoundsException e) {
        }

    }
}