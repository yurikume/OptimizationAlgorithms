package kernel;

import java.util.List;

public class Start
{
	public static void main(String[] args)
	{
		String pathmps = ".\\MK\\INS_15_10_2v.dat";
		//String pathmps = args[0];
		String pathlog = ".";
		String pathConfig = "config.txt";
		Configuration config = ConfigurationReader.read(pathConfig);		
		KernelSearch ks = new KernelSearch(pathmps, pathlog, config);
		ks.start();
		List<List<Double>> objValues = ks.getObjValues();	
	}
}

