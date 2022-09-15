package kernel;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationReader
{
	public static Configuration read(String path)
	{
		Configuration config = new Configuration();
		
		List<String> lines = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path)))
        {
        	lines = br.lines().collect(Collectors.toList());
        } catch (IOException e)
		{
			e.printStackTrace();
		}
        
        for(String line : lines)
        {
        	String[] splitLine = line.split("\\s+");
        	
        	switch(splitLine[0])
			{
        		case "THREADS":
        			config.setNumThreads(Integer.parseInt(splitLine[1]));
        			break;
        		case "MIPGAP": 
        			config.setMipGap(Double.parseDouble(splitLine[1]));
        			break;
        		case "PRESOLVE":
        			config.setPresolve(Integer.parseInt(splitLine[1]));
        			break;
        		case "TIMELIMIT":
        			config.setTimeLimit(Integer.parseInt(splitLine[1]));
        			break;
        		case "SORTER":
        			switch(Integer.parseInt(splitLine[1]))
        			{
        				case 0:
        					config.setItemSorter(new ItemSorterByValueAndAbsoluteRC());
        					break;
        				case 1:
        					config.setItemSorter(new ItemSorterByName());
        					break;
        				case 2:
        					config.setItemSorter(new ItemSorterRandom());
        					break;
        				case 3:
        					config.setItemSorter(new ItemSorterRandomXbyY());
        					break;
        				default:
        					System.out.println("Unrecognized item sorter.");
        			}
        			break;
        		case "KERNELSIZE":
        			config.setKernelSize(Double.parseDouble(splitLine[1]));
        			break;
        		case "BUCKETSIZE":
        			config.setBucketSize(Double.parseDouble(splitLine[1]));
        			break;
        		case "BUCKETBUILDER":
        			switch(Integer.parseInt(splitLine[1]))
        			{
        				case 0:
        					config.setBucketBuilder(new DefaultBucketBuilder());
        					break;
        				case 1:
        					config.setBucketBuilder(new VariableSizeBucketBuilder());
        					break;
        				case 2:
        					config.setBucketBuilder(new OverlappingBucketBuilder());
        					break;
        				case 3:
        					config.setBucketBuilder(new OverlappingBucketBuilder2());
        					break;
        				case 4:
        					config.setBucketBuilder(new PercentageBucketBuilder());
        					break;
        				default:
        					System.out.println("Unrecognized bucket builder.");
        			}
        			break;
        		case "TIMELIMITKERNEL":
        			config.setTimeLimitKernel(Integer.parseInt(splitLine[1]));
        			break;
        		case "NUMITERATIONS":
        			config.setNumIterations(Integer.parseInt(splitLine[1]));
        			break;
        		case "TIMELIMITBUCKET":
        			config.setTimeLimitBucket(Integer.parseInt(splitLine[1]));
        			break;
        		case "KERNELBUILDER":
        			switch(Integer.parseInt(splitLine[1]))
        			{
        				case 0:
        					config.setKernelBuilder(new KernelBuilderPositive());
        					break;
        				case 1:
        					config.setKernelBuilder(new KernelBuilderPercentage());
        					break;
        				case 2:
        					config.setKernelBuilder(new PercentageKernelBuilder());
        					break;
        				default:
        					System.out.println("Unrecognized kernel builder.");
        			}
        			break;
        		default:
        			System.out.println("Unrecognized parameter name.");			
			}
        }
        return config;
	}
}