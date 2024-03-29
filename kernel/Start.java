package kernel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Start{
	
	public static void main(String[] args){
		ArrayList<String> instance = new ArrayList<>();
		
//		List of instances to execute		
		instance.add("5_10_1");
//		instance.add("5_10_2");
//		instance.add("5_10_5");
//		instance.add("5_20_1");
//		instance.add("5_20_2");
//		instance.add("5_20_5");
//		instance.add("5_30_1");
//		instance.add("5_30_6");
//		instance.add("5_30_7");
//		instance.add("10_10_2");
		instance.add("10_10_3");
//		instance.add("10_10_7");
//		instance.add("10_20_3");
//		instance.add("10_20_5");
//		instance.add("10_20_8");
//		instance.add("10_30_1");
//		instance.add("10_30_2");
//		instance.add("10_30_5");
		instance.add("15_10_1");
//		instance.add("15_10_3");
//		instance.add("15_10_7");
//		instance.add("15_20_1");
//		instance.add("15_20_5");
//		instance.add("15_20_9");
//		instance.add("15_30_1");
//		instance.add("15_30_4");
//		instance.add("15_30_10");
//		instance.add("20_10_1");
//		instance.add("20_10_3");
//		instance.add("20_10_7");
//		instance.add("20_20_3");
//		instance.add("20_20_6");
//		instance.add("20_20_9");
//		instance.add("20_30_1");
//		instance.add("20_30_4");
//		instance.add("20_30_10");
		
		String[] istanza;
		String pathmps;
		String pathlog = ".";
		String pathConfig = "config.txt";
		
		//PER IL PROFE
		int T;
		int N;
		int version;
		
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter the number of knapsacks [5 or 10 or 15 or 20]: ");
		T = keyboard.nextInt();
		System.out.print("Enter the number of families [10 or 20 or 30]: ");
		N = keyboard.nextInt();
		System.out.print("Enter the number the instance [1 to 10]: ");
		version = keyboard.nextInt();
		
		Configuration config = ConfigurationReader.read(pathConfig);
		pathmps = "./MK/INS_" + T + "_" + N + "_" + version + "v.dat";
		config = ConfigurationReader.read(pathConfig);
		KernelSearch ks = new KernelSearch(pathmps, pathlog, config);
		ks.start();
		
		
		//PER I NOSTRI RUN
//		try {
//	        PrintWriter out = new PrintWriter("results_prova.txt");
//	        Configuration config = ConfigurationReader.read(pathConfig);
//	        out.println(String.format("Configuration recap (KS: %.3f - BS: %.3f - ITLIMS: %d - TLKER: %d - TLBUCK: %d)", config.getKernelSize(),config.getBucketSize(),config.getItemsLimit(),config.getTimeLimitKernel(),config.getTimeLimitBucket()));
//					
//	        for (int i = 0; i < instance.size(); i++){
//				pathmps = "./MK/INS_" + instance.get(i) + "v.dat";
//				config = ConfigurationReader.read(pathConfig);
//				KernelSearch ks = new KernelSearch(pathmps, pathlog, config);
//				ks.start();
//				istanza = instance.get(i).split("_");
//				
//				// out.println(istanza[0] + " & " + istanza[1] + " & " + istanza[2] + " & " + ks.getCurrent_best() + " & " + ks.getTot_time() + " \\\\");
//				
//				// Per stampa prima e seconda fase
//				out.println(istanza[0] + " & " + istanza[1] + " & " + istanza[2] + " & " + ks.getFirstPhaseObj() + " & " + ks.getFirstPhaseTime() + " & " + ks.getCurrent_best() + " & " + ks.getTot_time() + " \\\\");
//	        }
//	        out.close();  
//	      	System.out.println("Successfully wrote to the file.");
//	    }catch (IOException e) {
//	      System.out.println("An error occurred.");
//	      e.printStackTrace();
//		}		   	
	}
}
