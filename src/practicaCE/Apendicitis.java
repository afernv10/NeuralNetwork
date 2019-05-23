package practicaCE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.invoke.ConstantCallSite;
import java.util.ArrayList;
import java.util.Collections;

public class Apendicitis {
	
	private static final int MAX_ITERACIONES = 2000;
	private static final int opcion_f = 1;		// 0 = 0_1ps / 1 = identidad / 2 = sigmoide / 3 = gauss / 4 = seno(modificado)
	private final static int opcion_pot = 0; 	// 0 = r cte a 1 / 1 = r aleatoria de 1-9 para cada i de cada muestra
	private final static int opcion_k = 1; 		// 0 = orden archivo / 1 = aleatorio
	private static final double opcion_gamma = 1;	// 0 = cte / 1 = con enfriamiento
	private static final double GAMMA = 0.8;	// para cte
	private static double gamma;
	
	// salida : salida esperada
	private int si;	// 1 : 1
	private int falsos_no;	// <0.5 : 1
	private int falsos_si;	// >0.5 : 0
	private int no;	// 0 : 0
	private static int mejor_iteracion;	// el mayor de (si+no)
	

	private static int mejor_si;
	private static int mejor_no;
	private static int mejor_falsos_si;
	private static int mejor_falsos_no;
	
	
	@SuppressWarnings("static-access")
	public Apendicitis() {
		
		this.si = 0;
		this.falsos_no = 0;
		this.falsos_si = 0;
		this.no = 0;
		
		this.mejor_iteracion = 0;
		this.mejor_si = 0;
		this.mejor_no = 0;
		this.mejor_falsos_si = 0;
		this.mejor_falsos_no = 0;
		
		this.gamma = GAMMA;
	}
	
	public static void main(String[] args) {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;
		double[] esperados = null;
		ArrayList<String> lectura = new ArrayList<>();
		
		Apendicitis a = new Apendicitis();
		
		try {
			// Apertura del fichero
			archivo = new File("sources/datos");
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);

			// Lectura del fichero
			String linea;
			while ((linea = br.readLine()) != null) {
				lectura.add(linea);
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
		
		if(getOpcionK() == 1) {
			System.out.println("ORDEN ALEATORIO FICHERO ACTIVADO: Desordenamos los valores");
			Collections.shuffle(lectura);
		}
		
		// Metemos lo leido en una matriz
		double[][] datos = new double[lectura.size()][lectura.get(0).split(" ").length];
		esperados = new double[lectura.size()];
		for(int i = 0; i<lectura.size(); i++) {
			String[] line = lectura.get(i).split(" ");
			esperados[i] = Double.parseDouble(line[7]);	// guardamos la ultima columna de cada entrada, el valor esperado
			line[7] = "-1.0";
			
			for(int j = 0; j<datos[0].length; j++) {
				
				datos[i][j] = Double.parseDouble(line[j]);
				System.out.print(datos[i][j] + "\t");
			}
			System.out.println();
		}
		
		// SACAMOS PESOS aleatorios entre -1 y 1 
		double pesos[] = new double[datos[0].length-1];
		
		for (int i = 0; i < pesos.length; i++) {
			pesos[i] = Math.random()* (-2) + 1;
			System.out.println("pesos inicial: " + pesos[i]);
		}
		
		// r exponente de funcion potencial
			// coge aleatorio una r de 1-9
		int r[] = new int[]{1, 1, 1, 1, 1, 1, 1};
		int n = 6;
		
		if(opcion_pot == 1) {
			for (int i = 0; i < r.length; i++) {
				r[i] = (int) (Math.random() * n) + 1;
			}
		}
		
		System.out.print("R de pot: ");
		for (int i = 0; i < r.length; i++) {
			System.out.print("[" +r[i]+"]");
		}
		System.out.println("\n");
	
		
		int cont = 0;	// equivalente a t (tiempo)
		double potencial = 0, y = 0;
		int muestra = 0;	// cada una de las filas del fichero
		
		// bucle dando vueltas hasta numero iteracion máxima
		while (cont < MAX_ITERACIONES) {
			
			// GAMMA varía si hay ENFRIAMIENTO
			if( opcion_gamma == 1) gamma = enfriamiento(cont+1); 
				
			System.out.println("MUESTRA " + (muestra+1) + " - [Iteración " + ((cont/106) +1) + "]");

			
			// POTENCIAL(dato j);
			potencial = a.potencial(datos, pesos, muestra, r);
			System.out.println("Potencial principal muestra: "+potencial);
			
			// SALIDA Y, aplicando f(n)
			y = a.salida(potencial, opcion_f);
			System.out.println("Salida y principal: " + y);
			
			// ACTUALIZACION DE PESOS
			System.out.print("Pesos: ");
			for (int i = 0; i < pesos.length; i++) {
				pesos[i] += a.aprendizaje(esperados[muestra], y, potencial, datos[muestra][i], r[i]);
				System.out.print("["+pesos[i] + "]");
			}
			System.out.println();
			
			// INTERPRETACION
			a.interpretacion(datos, pesos, r, esperados, cont, opcion_f);
			
			// cada recorrido del fichero entero situamos el indice a 0
			if(++muestra == 106) muestra = 0;
			
			a.limpiarRegistrosVuelta();	// ponemos a 0 los "si", "no", "falsos_...
			
			System.out.println("\n--------------");
			cont++;	// siguiente iteración
		}
		
		
		System.out.println("Parámetros mejores resultados-->");
		System.out.println("\tMáximo de iteraciones: " + MAX_ITERACIONES);
		System.out.print("\t R de pot: ");
		StringBuilder sBr = new StringBuilder();
		for (int i = 0; i < r.length; i++) {
			sBr.append("[" +r[i]+"]");
		}
		System.out.println(sBr.toString());
		
		System.out.println("\t Si: " + mejor_si);
		System.out.println("\t No: " + mejor_no);
		System.out.println("\t Falsos si: " + mejor_falsos_si);
		System.out.println("\t Falsos no: " + mejor_falsos_no);
		System.out.println("-----------------------");
		System.out.println("MEJOR: " + (mejor_no+mejor_si));
		System.out.println("Mejor ITERACIÓN: " + mejor_iteracion);		
	}
	
	
	/**
	 * Cada iteración cambia gamma según la función añadida en el método
	 * @param cont	Equivalente a la t de tiempo o iteración
	 * @return
	 */
	private static double enfriamiento(int cont) {
		double a_enfriamiento = (7/8) * MAX_ITERACIONES;
		double gamma = (-1.0)/(1+ (Math.pow(Math.E, -(cont-a_enfriamiento)))) + 1;
		return gamma;
	}

	/**
	 * Función activación, calculamos potencial.
	 * 
	 * @param datos
	 * @param pesos
	 * @param muestra
	 * @param r
	 * @return
	 */
	private double potencial(double[][] datos, double[] pesos, int muestra, int[] r) {
		
		double pot = 0;
		for (int i = 0; i < datos[0].length-1; i++) {
			pot += (pesos[i] * Math.pow(datos[muestra][i], r[i])); 
		}
		return pot;	 
	}
	
	/**
	 * Aplicamos función segun la opción.
	 * 
	 * @param potencial
	 * @param opcion
	 * @return
	 */
	private double salida(double potencial, int opcion) {
		
		double y = 0;
		
		// salida
		if(opcion==0) {
			// 0 1
			if(potencial>= 0) {
				y = 1;
			}
		
		} else if (opcion==1) {
			// identidad
			y = potencial;
		
		} else if (opcion==2) {
			// sigmoide
			y = 1.0 / (1.0 + (Math.pow(Math.E, -potencial)));
		
		} else if (opcion==3) {
			// gauss
			y = Math.pow(Math.E, -(Math.pow(potencial, 2)));
		
		} else if (opcion==4) {
			// seno
			y = (Math.sin(potencial) + 1) / 2.0;
		}
		
		return y;
	}
	
	
	
	/**
	 * Actualización de los pesos.
	 * 
	 * @param y_esperada
	 * @param y
	 * @param potencial
	 * @param valorX	cada i de cada muestra
	 * @param r
	 * @return
	 */
	private double aprendizaje(double y_esperada, double y, double potencial, double valorX, double r) {
		int opcion_funcion = opcion_f;
		// actualizo pesos con las derivadas
		double ret = gamma * (y_esperada - y) * derivar(potencial, y, opcion_funcion) * Math.pow(valorX, r);
		
		return ret;
	}
	
	/**
	 * Derivada según la función.
	 * 
	 * @param potencial
	 * @param y
	 * @param opcion_funcion 
	 * @return
	 */
	private double derivar(double potencial, double y, int opcion_funcion) {
		double derivada = 0;
		
		if(opcion_funcion<=1) {
			// 0 1 o identidad
			derivada = 1; 	
		} else if (opcion_funcion==2) {
			// sigmoide
			derivada = y * (1 - y);
		
		} else if (opcion_funcion==3) {
			// gauss
			derivada = y * ((-2) * potencial);
		
		} else if (opcion_funcion==4) {
			// seno modificado
			derivada = 0.5 * Math.cos(potencial);
		} 
		
		return derivada;
	}
	
	/**
	 * Interpretamos los resultados en cada iteracion.
	 * Con los pesos obtenidos en esta recorremos nuevamente todo el fichero sacando nuevo "potencial" y nueva "y".
	 * Según esta actualizamos los valores de resultado.
	 * 
	 * @param datos
	 * @param pesos
	 * @param r
	 * @param esperados
	 * @param cont
	 * @param opcionF 
	 */
	private void interpretacion(double[][] datos, double[] pesos, int[] r, double[] esperados, int cont, int opcionF) {
		
		// recorremos todo el archivo con los nuevos pesos que tenemos calculados de la última muestra
		for (int i = 0; i < datos.length; i++) {
			
			double pot = this.potencial(datos, pesos, i, r);
			
			double y = this.salida(pot, opcionF);
			
			System.out.println("pot interpretacion: " + pot);
			System.out.println("y interpretacion: " + y);
			System.out.println("esperada: " + esperados[i]);
			
			if(y>= 0.5 && esperados[i] == 1) {
				// k bien si++
				si++;
			} else if(y>=0.5 && esperados[i] == 0) {
				// k mal falso si ++
				falsos_si++;
			} else if(y<0.5 && esperados[i]==1) {
				// k mal falso_no ++
				falsos_no++;
			} else {
				//k bien (no++)
				no++;	
			}
		
		}
		
		// actualizamos LOS MEJORES
		if((mejor_si + mejor_no) < (si+no) ) {
			mejor_no = no;
			mejor_si = si;
			mejor_falsos_si = falsos_si;
			mejor_falsos_no = falsos_no;
			mejor_iteracion = cont;	
		}
	}
	
	/**
	 * Se resetean los registros de resultados cada iteración
	 */
	private void limpiarRegistrosVuelta() {
		si = 0;
		falsos_no = 0;
		falsos_si = 0;
		no = 0;
	}

	/**
	 * @return the opcionK
	 */
	public static int getOpcionK() {
		return opcion_k;
	}

}

