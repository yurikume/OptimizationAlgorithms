package kernel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Start{
	
	public static void main(String[] args){
		ArrayList<String> instance = new ArrayList<>();
		
		instance.add("5_10_1");
//		instance.add("5_10_2");
//		instance.add("5_10_5");
//		instance.add("5_20_1");
		instance.add("5_20_2");
//		instance.add("5_20_5");
//		instance.add("5_30_1");
		instance.add("5_30_6");
//		instance.add("5_30_7");
		instance.add("10_10_2");
//		instance.add("10_10_3");
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
		instance.add("15_20_1");
//		instance.add("15_20_5");
//		instance.add("15_20_9");
//		instance.add("15_30_1");
//		instance.add("15_30_4");
//		instance.add("15_30_10");
		instance.add("20_10_1");
//		instance.add("20_10_3");
//		instance.add("20_10_7");
//		instance.add("20_20_3");
//		instance.add("20_20_6");
//		instance.add("20_20_9");
//		instance.add("20_30_1");
//		instance.add("20_30_4");
//		instance.add("20_30_10");
		
		
		String pathmps;
		String pathlog = ".";
		String pathConfig = "config.txt";
		
		try {
	        PrintWriter out = new PrintWriter("config_3.txt");
	        out.println("Riassunto configurazione: ");
	        
	        for (int i = 0; i < instance.size(); i++){
				pathmps = "./MK/INS_" + instance.get(i) + "v.dat";
				Configuration config = ConfigurationReader.read(pathConfig);		
				KernelSearch ks = new KernelSearch(pathmps, pathlog, config);
				ks.start();
				//List<List<Double>> objValues = ks.getObjValues();
				out.println(instance.get(i) + "\t\t" + ks.getCurrent_best() + "\t" + ks.getTot_time());
			}

	        out.close();  
	      	System.out.println("Successfully wrote to the file.");
	    }catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
		}
		
		
			
	}
}
