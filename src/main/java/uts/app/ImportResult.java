package uts.app;

import uts.ds.SinglyLinkedList;

public class ImportResult {
    int barisDibaca;
    int valid;
    final SinglyLinkedList<String> errors = new SinglyLinkedList<>();

    public int getBarisDibaca() { return barisDibaca; }
    public int getValid() { return valid; }
    public int getJumlahError() { return errors.size(); }
    public SinglyLinkedList<String> getErrors() { return errors; }
}
