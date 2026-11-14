package uts;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import uts.ds.SearchResult;
import uts.ds.SinglyLinkedList;

class SinglyLinkedListTest {

    private SinglyLinkedList<Integer> of(int... vals) {
        SinglyLinkedList<Integer> l = new SinglyLinkedList<>();
        for (int v : vals) l.add(v);
        return l;
    }

    @Test
    void addDanSizeMenjagaInvariant() {
        SinglyLinkedList<Integer> l = of(1, 2, 3);
        l.addFirst(0);
        assertEquals(4, l.size());
        assertEquals(0, l.get(0));
        assertEquals(3, l.get(3));
        l.validateInvariant();
    }

    @Test
    void hapusPertamaTengahTerakhir() {
        SinglyLinkedList<Integer> l = of(1, 2, 3, 4, 5);
        assertEquals(1, l.remove(0));
        l.validateInvariant();
        assertEquals(3, l.remove(1));
        l.validateInvariant();
        assertEquals(5, l.remove(l.size() - 1));
        l.validateInvariant();
        l.addLast(9);
        assertEquals(9, l.get(l.size() - 1));
        assertEquals(3, l.size());
        l.validateInvariant();
    }

    @Test
    void hapusSatuSatunyaMengosongkanHeadTail() {
        SinglyLinkedList<Integer> l = of(7);
        l.remove(0);
        assertTrue(l.isEmpty());
        l.validateInvariant();
    }

    @Test
    void insertBatasDanError() {
        SinglyLinkedList<Integer> l = of(1, 3);
        l.insert(1, 2);
        l.insert(0, 0);
        l.insert(l.size(), 4);
        assertEquals(5, l.size());
        for (int i = 0; i < 5; i++) assertEquals(i, l.get(i));
        l.validateInvariant();
        assertThrows(IndexOutOfBoundsException.class, () -> l.insert(-1, 9));
        assertThrows(IndexOutOfBoundsException.class, () -> l.insert(6, 9));
        assertEquals(5, l.size());
    }

    @Test
    void getDanRemoveDiLuarBatasMelempar() {
        SinglyLinkedList<Integer> l = of(1);
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(-1));
        l.validateInvariant();
    }

    @Test
    void setMenggantiNilaiTermasukTail() {
        SinglyLinkedList<Integer> l = of(1, 2, 3);
        assertEquals(3, l.set(2, 30));
        assertEquals(1, l.set(0, 10));
        l.addLast(4);
        assertEquals(10, l.get(0));
        assertEquals(30, l.get(2));
        assertEquals(4, l.get(3));
        l.validateInvariant();
    }

    @Test
    void searchMelaporkanLangkah() {
        SinglyLinkedList<Integer> l = of(10, 20, 30);
        SearchResult<Integer> ada = l.search(v -> v == 30);
        assertTrue(ada.isFound());
        assertEquals(2, ada.getPosition());
        assertEquals(3, ada.getSteps());
        SearchResult<Integer> tidak = l.search(v -> v == 99);
        assertFalse(tidak.isFound());
        assertEquals(3, tidak.getSteps());
        assertTrue(l.contains(20));
        assertFalse(l.contains(21));
    }

    @Test
    void clearMengosongkan() {
        SinglyLinkedList<Integer> l = of(1, 2, 3);
        l.clear();
        assertEquals(0, l.size());
        l.validateInvariant();
    }
}
