public class Driver {
    public static void main(String[] args) {
        CellControl control = new CellControl();
        Cell[] cells = new Cell[21];
        for (int i = 0; i < 21; i++) {
            cells[i] = new Cell(i);
            control.addCell(cells[i]);
        }
        Thread[] threads = new Thread[21];
        for (int i = 19; i < 21; i++) {
            threads[i] = new Thread(cells[i], Integer.toString(i));
            threads[i].start();
        }
        Control con = new Control();
        con.start();
        System.out.println("hi");
        for (Cell c : cells) {
            c.turnOn();
        }
    }
}

