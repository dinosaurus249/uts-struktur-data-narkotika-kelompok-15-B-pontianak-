package uts.ds;

public final class SearchResult<T> {
    private final T value;
    private final int position;
    private final int steps;

    public SearchResult(T value, int position, int steps) {
        this.value = value;
        this.position = position;
        this.steps = steps;
    }

    public T getValue() { return value; }
    public int getPosition() { return position; }
    public int getSteps() { return steps; }
    public boolean isFound() { return position >= 0; }
}
