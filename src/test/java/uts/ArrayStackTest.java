package uts;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import uts.ds.ArrayStack;
import uts.ds.SearchResult;

class ArrayStackTest {

    @Test
    void lifoDanPeek() {
        ArrayStack<Integer> s = new ArrayStack<>();
        s.push(1); s.push(2); s.push(3);
        assertEquals(3, s.peek());
        assertEquals(3, s.pop());
        assertEquals(2, s.pop());
        assertEquals(1, s.pop());
        assertThrows(IllegalStateException.class, s::pop);
        s.validateInvariant();
    }

    @Test
    void resizeOtomatis() {
        ArrayStack<Integer> s = new ArrayStack<>(2);
        for (int i = 0; i < 100; i++) s.push(i);
        assertEquals(100, s.size());
        assertTrue(s.capacity() >= 100);
        assertEquals(99, s.peek());
        s.validateInvariant();
    }

    @Test
    void searchPreserveTidakMengubahUrutan() {
        ArrayStack<Integer> s = new ArrayStack<>();
        for (int i = 1; i <= 5; i++) s.push(i);
        SearchResult<Integer> r = s.searchPreserve(v -> v == 2);
        assertTrue(r.isFound());
        assertEquals(3, r.getPosition());
        assertEquals(4, r.getSteps());
        assertEquals(5, s.size());
        for (int i = 0; i < 5; i++) assertEquals(5 - i, s.get(i));
        s.validateInvariant();
    }

    @Test
    void searchPreserveTidakDitemukanTetapUtuh() {
        ArrayStack<Integer> s = new ArrayStack<>();
        s.push(1); s.push(2);
        SearchResult<Integer> r = s.searchPreserve(v -> v == 9);
        assertFalse(r.isFound());
        assertEquals(2, r.getSteps());
        assertEquals(2, s.get(0));
        assertEquals(1, s.get(1));
    }

    @Test
    void removeTengahMenggeserElemen() {
        ArrayStack<Integer> s = new ArrayStack<>();
        for (int i = 1; i <= 4; i++) s.push(i);
        assertEquals(3, s.remove(1));
        assertEquals(3, s.size());
        assertEquals(4, s.get(0));
        assertEquals(2, s.get(1));
        s.validateInvariant();
    }

    @Test
    void nullDitolak() {
        assertThrows(IllegalArgumentException.class, () -> new ArrayStack<String>().push(null));
    }
}
