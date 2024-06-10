package tpe_prog_3;

import tpe_prog_3.utils.CSVReader;

import java.util.*;

/**
 * NO modificar la interfaz de esta clase ni sus métodos públicos.
 * Sólo se podrá adaptar el nombre de la clase "tpe.Tarea" según sus decisiones
 * de implementación.
 */

public class Servicios {
	private static final int MAXCRITICAS = 2;
	private Map<String, Tarea> tareas;
	private Map<String, Procesador> procesadores;
	private List<Tarea> criticas;
	private List<Tarea> noCriticas;
	private Map<Procesador, List<Tarea>> mejorAsignacion;
	private int tiempoMaximoOptimo; //infinito
	private boolean corteBusqueda;
	private List<Tarea> tareasPorPrioridad;
	private List<Tarea> tareasPorTiempo;
	private int cantidadEstados;


	/*
	 * Expresar la complejidad temporal del constructor.
	 * O(n log n):
		* Lectura de procesadores y tareas: O(n)
		* Inicialización de listas criticas, noCriticas: O(1)
		* Inicialización de lista tareas por prioridad y por tiempo: O(n)
		* División por criticidad: O(n) --> itera todas las tareas
		* Ordenar tareas por prioridad: O(n log n)
		* Inicialización de mejorAsignacion: O(n)
		* Inicializacion de cantidadEstados: O(1)
	 */

	public Servicios(String pathProcesadores, String pathTareas)
	{
		CSVReader reader = new CSVReader();
		procesadores = reader.readProcessors(pathProcesadores);
		tareas = reader.readTasks(pathTareas);
		criticas = new ArrayList<>();
		noCriticas = new ArrayList<>();
		tareasPorPrioridad = new ArrayList<>(tareas.values());
		tareasPorTiempo = new ArrayList<>(tareas.values());
		this.dividirPorCriticidad();
		this.ordenarTareasPorPrioridad();
		mejorAsignacion = new HashMap<>();
		tiempoMaximoOptimo = Integer.MAX_VALUE;
		corteBusqueda = false;
		asignarClaves(mejorAsignacion);
		cantidadEstados = 0;
    }

	private void asignarClaves(Map<Procesador, List<Tarea>> asignacion){
		//Inicializamos mejorAsignacion: asignamos como keys todos los procesadores y como values ArrayList vacios
		for (Procesador p : procesadores.values()) {
			asignacion.put(p, new ArrayList<>());
		}
	}

	private void dividirPorCriticidad(){
		for(Tarea t: tareas.values()){
			if(t.esCritica()){
				criticas.add(t);
			} else {
				noCriticas.add(t);
			}
		}
	}

	private void ordenarTareasPorPrioridad(){
		Comparator<Tarea> comparador = new ComparadorPrioridad();
		Collections.sort(tareasPorPrioridad, comparador);
	}

	private void ordenarTareasPorTiempo(){
		Comparator<Tarea> comparador = new ComparadorTiempo();
		Collections.sort(tareasPorTiempo, comparador);
	}

	/*
	 * Expresar la complejidad temporal del servicio 1.
	 * O(1): al ser HashMap accede directamente a la Tarea a partir del ID.
	 */
	public Tarea servicio1(String ID) {
		return tareas.get(ID);
	}

	/*
	 * Expresar la complejidad temporal del servicio 2.
	 * O(1): devuelve el ArrayList de tareas criticas o no criticas segun se pase por parametro
	 */
	public List<Tarea> servicio2(boolean esCritica) {
		if(esCritica){
			return new ArrayList<>(criticas);
		} else {
			return new ArrayList<>(noCriticas);
		}
	}

	/*
	 * Expresar la complejidad temporal del servicio 3.
	 * O(n): recorre (en el peor de los casos) todas las tareas (si las prioridades son los extremos)
	 */
	public List<Tarea> servicio3(int prioridadInferior, int prioridadSuperior) {
		List<Tarea> resultantesDelRango = new LinkedList<>();
		Iterator<Tarea> itTareas = tareasPorPrioridad.iterator();
		while(itTareas.hasNext() && itTareas.next().getPrioridad() <= prioridadSuperior){
			Tarea t = itTareas.next();
			if(t.getPrioridad() >= prioridadInferior){
				resultantesDelRango.add(t);
			}
		}
		return resultantesDelRango;
	}

	public void backtracking(int tiempoMaxNoRefrigerado) {
		//Creamos un nuevos HashMap para ir guardando el estado de asignaciones
		Map<Procesador, List<Tarea>> asignacionActual = new HashMap<>();

		//Asignamos todos los procesadores como clave y la lista vacia
		asignarClaves(asignacionActual);

		// Buscamos la mejor solución (mínimo tiempo máximo de ejecución)
		back(asignacionActual,tareasPorPrioridad, 0, tiempoMaxNoRefrigerado);
	}

	private void back(Map<Procesador, List<Tarea>> asignacionActual, List<Tarea> tasks, int indice, int tiempoMaximoNoRefrigerado) {

		if(corteBusqueda){return;}

		// Si no encuentra más tareas, se compara el tiempo maximo de la asignacion actual con el tiempo maximo previo (inicializado como "infinito")
		if (indice == tasks.size()) {
			int tiempoMaximo = calcularTiempoMaximoAsignacion(asignacionActual);

			// Si el tiempo obtenido es menor al previo lo reemplazamos para obtener el minimo posible
			if (tiempoMaximo < tiempoMaximoOptimo) {
				tiempoMaximoOptimo = tiempoMaximo;
				mejorAsignacion = new HashMap<>(asignacionActual);

				if(poda2()){
					corteBusqueda = true;
				}
			}
			return;
		}

		Tarea t = tasks.get(indice);
		for (Procesador p : asignacionActual.keySet()) {
			if (esAsignacionValida(p, t, tiempoMaximoNoRefrigerado)) {

				if(!poda1(p, t)){
					asignacionActual.get(p).add(t);
					cantidadEstados++; //cantidad de estados por los que va pasando el backtracking
					p.incrementarTiempoTotal(t.getTiempo());
					if(t.esCritica()){
						p.incrementarTCriticas();
					}
					back(asignacionActual, tasks, indice + 1, tiempoMaximoNoRefrigerado);

					if(corteBusqueda) return;

					asignacionActual.get(p).remove(t);
					p.decrementarTiempoTotal(t.getTiempo());
					if(t.esCritica()){
						p.decrementarTCriticas();
					}
				}
			}
		}
	}

	private boolean esAsignacionValida(Procesador p, Tarea t, int tiempoMaximoNoRefrigerado) {
		int tiempoTotal = p.getTiempoTotal();
		int tareasCriticas = p.cantTCriticas();

		if (t.esCritica() && tareasCriticas >= 2) return false;
		if (!p.esRefrigerado() && (tiempoTotal + t.getTiempo()) > tiempoMaximoNoRefrigerado) return false;

		return true;
	}

	/* PODA1: Si el tiempo que se obtendría al agregar la nueva tarea al procesador ya es mayor al
	* tiempo minimo obtenido entre las soluciones anteriores, cortamos ejecucion de esa rama */
	private boolean poda1(Procesador p, Tarea t){
		return (p.getTiempoTotal() + t.getTiempo()) > tiempoMaximoOptimo;
	}

	/* PODA2: Si el tiempo obtenido es el menor tiempo posible (porque por ejemplo la tarea más lenta es 100
	 * y tiempo no puede ser menor a eso), se corta la ejecucion
	 */
	private boolean poda2(){
		return tiempoMaximoOptimo == tiempoMaximoTareas();
	}

	private int calcularTiempoMaximoAsignacion(Map<Procesador, List<Tarea>> asignacion) {
		int tiempoMaximo = 0;
		// Con keySet() obtenemos todas las claves de la asignacion actual (todos los procesadores)
		for(Procesador p : asignacion.keySet()){
			// Por cada procesador, obtenemos el tiempo total de procesamiento
			int tiempoTotal = p.getTiempoTotal();
			// Si el tiempoTotal de p es mayor al tiempoMaximo que habiamos obtenido anteriormente, lo reemplazamos
			if(tiempoTotal > tiempoMaximo){
				tiempoMaximo = tiempoTotal;
			}
		}
		return tiempoMaximo;
	}

	// Devuelve el tiempo de la tarea que tiene mayor tiempo de procesamiento
	public int tiempoMaximoTareas(){
		int tiempoMax = 0;
		for(Tarea t: tareas.values()){
			if(t.getTiempo() > tiempoMax){
				tiempoMax = t.getTiempo();
			}
		}
		return tiempoMax;
	}

	public int getCantidadEstados(){
		return cantidadEstados;
	}

	public void greedy(int tiempoMaxNoRefrigerado){

		ArrayList<Procesador> processors = new ArrayList<>(procesadores.values());

		int posP=0;
		for(int posT = tareasPorTiempo.size()-1; posT >= 0; posT--){
			Tarea t = tareasPorTiempo.get(posT);
			Procesador p = processors.get(posP);

			if(p.getTiempoTotal() + t.getTiempo() <= tiempoMaximoTareas()
					&& esAsignacionValida(p, t, tiempoMaxNoRefrigerado))
			{
				mejorAsignacion.get(p).add(t);
				cantidadEstados++;
				p.incrementarTiempoTotal(t.getTiempo());

			} else {
				posT++;
				posP++;
			}

			//Si recorrimos todos los procesadores y aún quedan tareas por asignar, volvemos a recorrer desde el primer procesador
			if(posP == processors.size()) posP=0;
		}
	}

	public String getMejorAsignacion(){
		String mejorAsig = "";
		// Imprimir la asignación resultante
		for(Map.Entry<Procesador, List<Tarea>> entry : mejorAsignacion.entrySet()) {
			Procesador procesador = entry.getKey();
			List<Tarea> tareasAsignadas = entry.getValue();

			mejorAsig += "\n Procesador: " + procesador.getId();
			//System.out.println("Procesador: " + procesador.getId());

			if (tareasAsignadas.isEmpty()) {
				mejorAsig += "\n  Sin tareas asignadas.";
				//System.out.println("  Sin tareas asignadas.");
			} else {
				for (Tarea tarea : tareasAsignadas) {
					mejorAsig += "\n  Tarea: " + tarea.getNombre();
					//System.out.println("  Tarea: " + tarea.getNombre());
				}
			}
		}
		return mejorAsig;
	}

	/* Para encontrar la solucion con el mejor tiempo posible y la menor cantidad de recorridos, utilizamos
	* las siguientes 2 podas:
	* PODA1: Si el tiempo que se obtendría al agregar la nueva tarea al procesador ya es mayor al
	* tiempo minimo obtenido entre las soluciones anteriores, cortamos ejecucion de esa rama
	* PODA2: Si el tiempo obtenido es el menor tiempo posible (porque por ejemplo la tarea más lenta es 100
	* y tiempo no puede ser menor a eso), se corta la ejecucion
	* */
	public void solucionBacktracking() {
		System.out.println( "Backtracking\n" +
				"Solución obtenida: " + getMejorAsignacion() +
				"\nSolución obtenida (tiempo máximo de ejecución): " + calcularTiempoMaximoAsignacion(mejorAsignacion)+
				"\nMétrica para analizar el costo de la solución (cantidad de estados generados): "+ getCantidadEstados());
	}

	/* Teniendo en cuenta el tiempo de la tarea de tiempo maximo, ir agregando a los procesadores (por tiempo)
	 * las tareas y cuando llegue a ese tiempo maximo, cambiar de procesador. Si el recorrido de los procesadores termina
	 * (no tenemos mas procesadores), tendria que volver a empezar para seguir asignando desde el primer procesador */
	public void solucionGreedy() {
		System.out.println( "Greedy\n" +
				"Solución obtenida: " + getMejorAsignacion() +
				"\nSolución obtenida (tiempo máximo de ejecución): " + calcularTiempoMaximoAsignacion(mejorAsignacion)+
				"\nMétrica para analizar el costo de la solución (cantidad de estados generados): "+ getCantidadEstados());
	}

}