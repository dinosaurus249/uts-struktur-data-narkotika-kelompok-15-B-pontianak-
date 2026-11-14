package uts.ds;

import java.util.function.Predicate;

public class ArrayStack<T> implements StrukturData<T> {
    private Object[] data;
    private int size;

    public ArrayStack() { this(8); }

    public ArrayStack(int initialCapacity) {
        if (initialCapacity < 1) throw new IllegalArgumentException("Kapasitas minimal 1");
        data = new Object[initialCapacity];
    }

    public void push(T value) {
        if (value == null) throw new IllegalArgumentException("Elemen tidak boleh null");
        if (size == data.length) resize(data.length * 2);
        data[size++] = value;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (size == 0) throw new IllegalStateException("Stack kosong");
        T v = (T) data[--size];
        data[size] = null;
        return v;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) throw new IllegalStateException("Stack kosong");
        return (T) data[size - 1];
    }

    public SearchResult<T> searchPreserve(Predicate<T> predicate) {
        ArrayStack<T> temp = new ArrayStack<>(Math.max(size, 1));
        int steps = 0;
        int depth = 0;
        T found = null;
        boolean ok = false;
        while (!isEmpty()) {
            T v = pop();
            temp.push(v);
            steps++;
            if (predicate.test(v)) { found = v; ok = true; break; }
            depth++;
        }
        restore(temp);
        return new SearchResult<>(found, ok ? depth : -1, steps);
    }

    public void restore(ArrayStack<T> temp) {
        while (!temp.isEmpty()) push(temp.pop());
    }

    @Override public void add(T value) { push(value); }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int position) {
        checkIndex(position);
        return (T) data[size - 1 - position];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T remove(int position) {
        checkIndex(position);
        int idx = size - 1 - position;
        T v = (T) data[idx];
        for (int i = idx; i < size - 1; i++) data[i] = data[i + 1];
        data[--size] = null;
        return v;
    }

    @Override public int size() { return size; }
    @Override public boolean isEmpty() { return size == 0; }

    @Override
    public boolean contains(T value) {
        for (int i = 0; i < size; i++) {
            if (data[i].equals(value)) return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) data[i] = null;
        size = 0;
    }

    public int capacity() { return data.length; }

    public void validateInvariant() {
        if (size < 0 || size > data.length) throw new IllegalStateException("size di luar kapasitas");
        for (int i = 0; i < size; i++) {
            if (data[i] == null) throw new IllegalStateException("Elemen aktif null pada indeks " + i);
        }
        for (int i = size; i < data.length; i++) {
            if (data[i] != null) throw new IllegalStateException("Slot tidak aktif tidak bersih pada indeks " + i);
        }
    }

    private void resize(int newCapacity) {
        Object[] n = new Object[newCapacity];
        for (int i = 0; i < size; i++) n[i] = data[i];
        data = n;
    }

    private void checkIndex(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Posisi " + position + " di luar 0.." + (size - 1));
        }
    }
}
