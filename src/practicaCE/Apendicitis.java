package practicaCE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.invoke.ConstantCallSite;
import java.util.ArrayList;

public class Apendicitis {
	
	private static final int MAX_ITERACIONES = 200;
	private static final int opcion_f = 1;	// 0 = 0_1ps / 1 = identidad / 2 = sigmoide / 3 = gauss / 4 = seno(modificado)
	private final static int opcion_pot = 1; // de 1-9 para la r
	private final static int opcion_k = 1; // 0 = orden archivo; 1 aleatorio
	private static final int opcion_gamma = 0;	// 0 = cte, 1 = enfriamiento

	private double gamma;
	
	public Apendicitis() {
		
		if(opcion_gamma == 0) this.gamma = 1;
		//else gamma = 0;//enfriamiento
	}
	
	public static void main(String[] args) {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;
		double[] esperados = null;
		ArrayList<String> lectura = new ArrayList<>();
		
		Apendicitis a = new Apendicitis();
		
		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			archivo = new File("sources/datos");
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);

			// Lectura del fichero
			String linea;
			while ((linea = br.readLine()) != null) {
				lectura.add(linea);
				//System.out.println(linea);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// En el finally cerramos el fichero, para asegurarnos
			// que se cierra tanto si todo va bien como si salta
			// una excepcion.
			try {
				if (null != fr) {
					fr.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		
		// Metemos lo leido en una matriz
		double[][] datos = new double[lectura.size()][lectura.get(0).split(" ").length];
		esperados = new double[lectura.size()];
		for(int i = 0; i<lectura.size(); i++) {
			String[] line = lectura.get(i).split(" ");
			esperados[i] = Double.parseDouble(line[7]);
			line[7] = "-1.0";
			
			for(int j = 0; j<datos[0].length; j++) {
				
				datos[i][j] = Double.parseDouble(line[j]);
				System.out.print(datos[i][j] + "\t");
			}
			System.out.println();
		}
		
		System.out.println("ESPERADOS: " + esperados.toString());
		
		// SACAMOS PESOS aleatorios entre 
		double pesos[] = new double[datos[0].length-1];
		
		for (int i = 0; i < pesos.length; i++) {
			pesos[i] = Math.random()* (-2) + 1;
			System.out.println("pesos inicial: " + pesos[i]);
		}
		
		// r exponente de funcion potencial
			// coge aleatorio una r de 1-9
		int r[] = new int[datos[0].length-1];
		int n = 9;
		if(opcion_pot == 1) {
			for (int i = 0; i < r.length; i++) {
				r[i] = (int) (Math.random() * n) + 1;
			}
		} else {
			for (int i = 0; i < r.length; i++) {
				r[i] = 1;
			}
		}
		System.out.println("R de pot: "+ r.toString());
		
		
		// bucle dando vueltas maxim
		int cont = 0;
		double potencial = 0, y = 0;
		int muestra = 1;
		
		while (cont < MAX_ITERACIONES) {
			// potencial(dato j);
			potencial = a.potencial(datos, pesos, muestra, r);
			System.out.println("Potencial: "+potencial);
			
			// salida y, aplicando f(n)
			y = a.salida(potencial);
			System.out.println("y: " + y);
			
			// se actualizan los pesos
			System.out.println("pesos l: " + pesos.length);
			for (int i = 0; i < pesos.length; i++) {
				pesos[i] = a.aprendizaje(esperados[muestra], y, potencial, datos[muestra][i], r[i]);
				System.out.println("p: " + pesos[i]);
			}
			
			// interpretación
			a.interpretacion(datos, muestra, pesos, r, esperados);
			
			// calculo error red para cada y, y para toda la red es suma de todas
			// si interpretaramos una vez cada corrida de todo el archivo cogemos los ult pesos y recalculamos todas las y
			// tenemos que guardar los resultados para cada iteracion
			cont++;
			
			if(++muestra == 105) muestra = 0;
			System.out.println("--------------");
		}
		
		// sacar ultimos pesos
		
		
	}

	private void interpretacion(double[][] datos, int cont, double[] pesos, int[] r, double[] esperados) {
		
		double pot = this.potencial(datos, pesos, cont, r);
		
		double y = this.salida(pot);
		
		if(y>= 0.5 && esperados[cont] == 1) {
			// k bien si++
		} else if(y>=0.5 && esperados[cont] == 0) {
			// k mal falso no ++
		} else if(y<0.5 && esperados[cont]==1) {
			// k mal falso_si ++
		} else {
			//k bien (no++)
		}
		
	}

	/**
	 * Función activación, calculamos potencial
	 * 
	 * @param datos
	 * @param pesos
	 * @param cont
	 * @param r
	 * @return
	 */
	private double potencial(double[][] datos, double[] pesos, int cont, int[] r) {
		
		double pot = 0;
		for (int i = 0; i < datos[0].length-1; i++) {
			pot += pesos[i] * Math.pow(datos[cont][i], r[i]); 
		}
		return pot;	 
	}
	
	/**
	 * Función segun la opción
	 * 
	 * @param potencial
	 * @return
	 */
	private double salida(double potencial) {
		
		double y = 0;
		
		// salida
		if(opcion_f==0) {
			// 0 1
			if(potencial>= 0) {
				y = 1;
			}
		
		} else if (opcion_f==1) {
			// identidad
			y = potencial;
		
		} else if (opcion_f==2) {
			// sigmoide
			y = 1.0 / (1.0 + (Math.pow(Math.E, -potencial)));
		
		} else if (opcion_f==3) {
			// gauss
			y = Math.pow(Math.E, -(Math.pow(potencial, 2)));
		
		} else if (opcion_f==4) {
			// seno
			y = (Math.sin(potencial) + 1) / 2.0;
		}
		
		return y;
	}
	
	/**
	 * 
	 * @param y_esperada
	 * @param y
	 * @param potencial
	 * @param valorX
	 * @param r
	 * @return
	 */
	private double aprendizaje(double y_esperada, double y, double potencial, double valorX, double r) {
		// actualizo pesos con las derivadas
		double ret = gamma * (y_esperada - y) * derivar(potencial, y) * Math.pow(valorX, r);
		
		return ret;
	}
	
	private double derivar(double potencial, double y) {
		double derivada = 0;
		
		if(opcion_f<=1) {
			// 0 1 o identidad
			derivada = 1; 	
		} else if (opcion_f==2) {
			// sigmoide
			derivada = y * (1 - y);
		
		} else if (opcion_f==3) {
			// gauss
			derivada = y * ((-2) * potencial);
		
		} else if (opcion_f==4) {
			// seno modificado
			derivada = 0.5 * Math.cos(potencial);
		} 
		
		return derivada;
	}
}

