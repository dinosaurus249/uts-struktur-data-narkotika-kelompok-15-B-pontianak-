package uts.ds;

import java.util.Objects;
import java.util.function.Predicate;

public class SinglyLinkedList<T> implements StrukturData<T> {

    public static final class Node<T> {
        private final T value;
        private Node<T> next;

        private Node(T value) { this.value = value; }

        public T getValue() { return value; }
        public Node<T> getNext() { return next; }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public Node<T> getHead() { return head; }

    public void addFirst(T value) {
        Node<T> n = new Node<>(value);
        n.next = head;
        head = n;
        if (tail == null) tail = n;
        size++;
    }

    public void addLast(T value) {
        Node<T> n = new Node<>(value);
        if (tail == null) {
            head = n;
            tail = n;
        } else {
            tail.next = n;
            tail = n;
        }
        size++;
    }

    @Override
    public void add(T value) { addLast(value); }

    public void insert(int position, T value) {
        if (position < 0 || position > size) {
            throw new IndexOutOfBoundsException("Posisi sisip " + position + " di luar 0.." + size);
        }
        if (position == 0) { addFirst(value); return; }
        if (position == size) { addLast(value); return; }
        Node<T> prev = nodeAt(position - 1);
        Node<T> n = new Node<>(value);
        n.next = prev.next;
        prev.next = n;
        size++;
    }

    @Override
    public T get(int position) {
        checkIndex(position);
        return nodeAt(position).value;
    }

    public T set(int position, T value) {
        checkIndex(position);
        Node<T> old = nodeAt(position);
        Node<T> n = new Node<>(value);
        n.next = old.next;
        if (position == 0) {
            head = n;
        } else {
            nodeAt(position - 1).next = n;
        }
        if (old == tail) tail = n;
        old.next = null;
        return old.value;
    }

    @Override
    public T remove(int position) {
        checkIndex(position);
        Node<T> removed;
        if (position == 0) {
            removed = head;
            head = head.next;
            if (head == null) tail = null;
        } else {
            Node<T> prev = nodeAt(position - 1);
            removed = prev.next;
            prev.next = removed.next;
            if (removed == tail) tail = prev;
        }
        removed.next = null;
        size--;
        return removed.value;
    }

    public SearchResult<T> search(Predicate<T> predicate) {
        int steps = 0;
        int pos = 0;
        for (Node<T> c = head; c != null; c = c.next) {
            steps++;
            if (predicate.test(c.value)) return new SearchResult<>(c.value, pos, steps);
            pos++;
        }
        return new SearchResult<>(null, -1, steps);
    }

    @Override
    public boolean contains(T value) {
        return search(v -> Objects.equals(v, value)).isFound();
    }

    @Override public int size() { return size; }
    @Override public boolean isEmpty() { return size == 0; }

    @Override
    public void clear() {
        Node<T> c = head;
        while (c != null) {
            Node<T> nx = c.next;
            c.next = null;
            c = nx;
        }
        head = null;
        tail = null;
        size = 0;
    }

    public void validateInvariant() {
        if (size < 0) throw new IllegalStateException("size negatif");
        if (size == 0) {
            if (head != null || tail != null) {
                throw new IllegalStateException("List kosong tetapi head/tail tidak null");
            }
            return;
        }
        if (head == null || tail == null) {
            throw new IllegalStateException("List tidak kosong tetapi head/tail null");
        }
        int count = 0;
        Node<T> last = null;
        for (Node<T> c = head; c != null; c = c.next) {
            count++;
            last = c;
            if (count > size) throw new IllegalStateException("Jumlah node melebihi size (siklus?)");
        }
        if (count != size) throw new IllegalStateException("Jumlah node " + count + " != size " + size);
        if (last != tail) throw new IllegalStateException("tail tidak menunjuk node terakhir");
        if (tail.next != null) throw new IllegalStateException("tail.next harus null");
    }

    private void checkIndex(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Posisi " + position + " di luar 0.." + (size - 1));
        }
    }

    private Node<T> nodeAt(int position) {
        Node<T> c = head;
        for (int i = 0; i < position; i++) c = c.next;
        return c;
    }
}
