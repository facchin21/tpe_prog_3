package tpe_prog_3.utils;

import tpe_prog_3.Procesador;
import tpe_prog_3.Tarea;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CSVReader {
	private Map<String, Tarea> tasks;
	private Map<String, Procesador> processors;

	public CSVReader() {
		tasks = new HashMap<>();
		processors = new HashMap<>();
	}
	
	public Map<String, Tarea> readTasks(String taskPath) {
		
		// Obtengo una lista con las lineas del archivo
		// lines.get(0) tiene la primer linea del archivo
		// lines.get(1) tiene la segunda linea del archivo... y así
		ArrayList<String[]> lines = this.readContent(taskPath);
		
		for (String[] line: lines) {
			// Cada linea es un arreglo de Strings, donde cada posicion guarda un elemento
			String id = line[0].trim();
			String nombre = line[1].trim();
			Integer tiempo = Integer.parseInt(line[2].trim());
			Boolean critica = Boolean.parseBoolean(line[3].trim());
			Integer prioridad = Integer.parseInt(line[4].trim());

			// Aca instanciar lo que necesiten en base a los datos leidos
			Tarea task = new Tarea(id, nombre, tiempo, critica, prioridad);
			tasks.put(id, task);
		}
		return tasks;
	}

	/*
	public ArrayList<String[]> getTasks(){
		return new ArrayList<String[]>(tasks);
	}
	*/
	public Map<String, Procesador> readProcessors(String processorPath) {
		
		// Obtengo una lista con las lineas del archivo
		// lines.get(0) tiene la primer linea del archivo
		// lines.get(1) tiene la segunda linea del archivo... y así
		ArrayList<String[]> lines = this.readContent(processorPath);
		
		for (String[] line: lines) {
			// Cada linea es un arreglo de Strings, donde cada posicion guarda un elemento
			String id = line[0].trim();
			String codigo = line[1].trim();
			Boolean refrigerado = Boolean.parseBoolean(line[2].trim());
			Integer anio = Integer.parseInt(line[3].trim());

			// Aca instanciar lo que necesiten en base a los datos leidos
			Procesador processor = new Procesador(id, codigo, refrigerado, anio);
			processors.put(id, processor);
		}

		return processors;
		
	}

	private ArrayList<String[]> readContent(String path) {
		ArrayList<String[]> lines = new ArrayList<String[]>();

		File file = new File(path);
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.trim();
				lines.add(line.split(";"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		
		return lines;
	}
	
}
