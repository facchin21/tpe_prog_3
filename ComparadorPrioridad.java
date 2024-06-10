package tpe_prog_3;

import java.util.Comparator;

public class ComparadorPrioridad implements Comparator<Tarea> {


    @Override
    public int compare(Tarea t1, Tarea t2) {
        return t1.getPrioridad().compareTo(t2.getPrioridad());
    }
}
