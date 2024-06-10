package tpe_prog_3;

public class Main {

	public static void main(String args[]) {
		Servicios servicios = new Servicios("./src/tpe_prog_3/datasets/Procesadores.csv", "./src/tpe_prog_3/datasets/Tareas.csv");
		int tiempoMaxNoRefrigerado = 60;

		servicios.backtracking(tiempoMaxNoRefrigerado);
		servicios.solucionBacktracking();

//		servicios.greedy(tiempoMaxNoRefrigerado);
//		servicios.solucionGreedy();

	}
}
