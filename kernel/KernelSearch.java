package kernel;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gurobi.GRBCallback;

public class KernelSearch
{
	private String instPath;
	private String logPath;
	private Configuration config;
	private List<Item> items;
	private ItemSorter sorter;
	private BucketBuilder bucketBuilder;
	private KernelBuilder kernelBuilder;
	private int tlim;
	private Solution bestSolution;
	private List<Bucket> buckets;
	private Kernel kernel;
	private int tlimKernel;
	private int tlimBucket;
	private int numIterations;
	private GRBCallback callback;
	private int timeThreshold = 5;
	private List<List<Double>> objValues;
	private long tot_time;
	private Double current_best;
	
	private Instant startTime;
	
	public KernelSearch(String instPath, String logPath, Configuration config)
	{
		this.instPath = instPath;
		this.logPath = logPath;
		this.config = config;
		bestSolution = new Solution();
		objValues = new ArrayList<>();
		configure(config);
	}
	
	private void configure(Configuration configuration)
	{
		sorter = config.getItemSorter();
		tlim = config.getTimeLimit();
		bucketBuilder = config.getBucketBuilder();
		kernelBuilder = config.getKernelBuilder();
		tlimKernel = config.getTimeLimitKernel();
		numIterations = config.getNumIterations();
		tlimBucket = config.getTimeLimitBucket();
	}
	
	public Solution start()
	{
		startTime = Instant.now();
		callback = new CustomCallback(logPath, startTime);
		items = buildItems();
		sorter.sort(items);	
		
		// Print out the items in an external file
		try {
	        PrintWriter out = new PrintWriter("items_list.txt");
	        out.println("****** Items dopo il sorting:");
	        out.println("Num items = " + items.size());
	        
	        int a,c;
		    double perc_good;
		    for(Item it: items) {
				a = it.getWeight();
				c = it.getProfit();
				perc_good = it.getGoodness();
				out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - c = " + c + " - a = " + a + " - good% = " + perc_good);
		    }

	        out.close();  
	      	System.out.println("Successfully wrote to the file.");
	    }catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
		}
		
		kernel = kernelBuilder.build(items, config);
		List<Item> sel_items = items.stream().filter(it -> it.getName().startsWith("y") || !kernel.contains(it)).collect(Collectors.toList());
		List<Item> y_ker = items.stream().filter(it -> kernel.contains(it) && it.getName().startsWith("y")).collect(Collectors.toList());

		buckets = bucketBuilder.build(sel_items, config, y_ker);
		solveKernel();
		iterateBuckets();
		
		return bestSolution;
	}

	private List<Item> buildItems()
	{
		Model model = new Model(instPath, logPath, config.getTimeLimit(), config, true); // time limit equal to the global time limit
		model.buildModel();
		model.solve();
		List<Item> items = new ArrayList<>();
		List<String> varNames = model.getVarNames();
		int i,t,j,c,a,f,d;
		int items_weight,items_profit;
		double good;
		List<Item> x_items;
		for(String v : varNames)
		{
			double value = model.getVarValue(v);
			double rc = model.getVarRC(v); // can be called only after solving the LP relaxation
			Item it;
			
			if(v.startsWith("x")) {
				String vars[] = v.split("_");
				i = Integer.parseInt(vars[1]);
				j = Integer.parseInt(vars[2]);
				t = Integer.parseInt(vars[3]);
	            c = model.getProfit(i, j, t);
	            a = model.getWeight(i, j);
	            good = (c-a)*100.0/a;
				it = new Item(v, value, rc, c, a, good);
			}else {
				// y item
				String vars[]= v.split("_");
				i = Integer.parseInt(vars[1]);
				t = Integer.parseInt(vars[2]);
				f = model.getSetupCost(i, t);
				d = model.getFamilyWeight(i);
				it = new Item(v, value, rc, f, d, 1);
			}
				
			items.add(it);
		}

		// Now that we have initialized all the x items, we can evaluate the goodness of the y items
		List<Item> y_items = items.stream().filter(it -> it.getName().startsWith("y")).collect(Collectors.toList());
		int fam_setup_cost,fam_weight,knap_cap,profit_measure;
		double weight_percentage, y_goodness, res_knap_cap;
		
		for(Item y_it : y_items) {
			int fam,knap;
			String vars[]= y_it.getName().split("_");
			fam = Integer.parseInt(vars[1]);
			knap = Integer.parseInt(vars[2]);
			knap_cap = model.getKnapsackCapacity(knap);
			fam_setup_cost = y_it.getProfit();
			fam_weight = y_it.getWeight();
			x_items = items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
			items_weight = 0;
			items_profit = 0;
			for(Item x_it : x_items) {
				items_weight += x_it.getWeight();
				items_profit += x_it.getProfit();
			}
			
			// residual capacity of the knapsack
			res_knap_cap = knap_cap - fam_weight;
			// percentage of items that fit the knapsack
			weight_percentage = res_knap_cap/items_weight;
			
			// if it is above 1.0 it means that all the items can fit in
			if(weight_percentage >= 1.0)
				weight_percentage = 1.0;
			
			// the setup cost is negative
			profit_measure = items_profit + fam_setup_cost; 
			
			y_goodness = profit_measure * weight_percentage; 
			
			y_it.setGoodness(y_goodness);
		}
		return items;
	}
	
	private void solveKernel()
	{
		Model model = new Model(instPath, logPath, Math.min(tlimKernel, getRemainingTime()), config, false);	
		model.buildModel();
		objValues.add(new ArrayList<>());
		
		if(!bestSolution.isEmpty())
		{
			model.addObjConstraint(bestSolution.getObj());		
			model.readSolution(bestSolution);
		}
		
		List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it)).collect(Collectors.toList());
		
		model.disableItems(toDisable);
		model.setCallback(callback);
		
		model.solve();
		
		if(model.hasSolution())
		{
			
			bestSolution = model.getSolution();
			model.exportSolution();
			
			objValues.get(objValues.size()-1).add(bestSolution.getObj());
		}
		else
		{
			objValues.get(objValues.size()-1).add(0.0);
		}
	}
	
	private void iterateBuckets()
	{
		// if we are using Two-Phase we are not taking in consideration the "NUMITERATIONS" parameter in the configuration
		// since we are going to run the solveBuckets function in two different ways
		
		if(config.getBucketBuilder() instanceof BucketBuilderByGoodness) {
			// first phase
			solveBuckets(); 
			
			// second phase
			System.out.println("\n\n****** START 2ND PHASE ********");
			System.out.println("Time first phase: " + Duration.between(startTime, Instant.now()).getSeconds());
			config.setBucketBuilder(new BucketBuilderByName());
			config.setKernelBuilder(new KernelBuilderByNamePercentage());
			
			List<Item> y_ker = items.stream().filter(it -> kernel.contains(it) && it.getName().startsWith("y")).collect(Collectors.toList());
			List<Item> newItems = new ArrayList<Item>();
			for(Item y_it : y_ker) {
				String vars[]= y_it.getName().split("_");
	            String fam = vars[1];
	            String knap = vars[2];
	            
	            newItems.addAll(items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList()));
			}
			// list of items to use during the second phase
			newItems.addAll(y_ker); 
			sorter.sort(newItems);
						
			kernelBuilder = config.getKernelBuilder();
			kernel = kernelBuilder.build(newItems, config);
			List<Item> sel_items = newItems.stream().filter(it -> it.getName().startsWith("y") || !kernel.contains(it)).collect(Collectors.toList());
			y_ker = newItems.stream().filter(it -> kernel.contains(it) && it.getName().startsWith("y")).collect(Collectors.toList());
			bucketBuilder = config.getBucketBuilder();
			buckets = bucketBuilder.build(sel_items, config, y_ker);
			
			solveKernel();
			solveBuckets();
			
		} else {
			
			// We take in consideration the "NUMITERATION" parameter
			
			for (int i = 0; i < numIterations; i++)
			{
				if(getRemainingTime() <= timeThreshold)
					return;
				if(i != 0)
					objValues.add(new ArrayList<>());
				
				System.out.println("\n\n\n\t\t******** Iteration "+i+" ********\n\n\n");
				solveBuckets();			
			}
		}
		current_best = bestSolution.getObj();
		tot_time = Duration.between(startTime, Instant.now()).getSeconds();
		System.out.println("Total time:" + tot_time);
		
	}
	
	public long getTot_time() {
		return tot_time;
	}

	public Double getCurrent_best() {
		return current_best;
	}

	private void solveBuckets()
	{
		int count = 0;
		
		for(Bucket b : buckets) {
			System.out.println("\n\n\n\n\t\t** Solving bucket "+count++ +" **\n");
			
			List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it) && !b.contains(it)).collect(Collectors.toList());
			
			Model model = new Model(instPath, logPath, Math.min(tlimBucket, getRemainingTime()), config, false);	
			model.buildModel();
			
			model.disableItems(toDisable);
			model.addBucketConstraint(b.getItems());		
			
			if(!bestSolution.isEmpty())
			{
				model.addObjConstraint(bestSolution.getObj());		
				model.readSolution(bestSolution);
			}
			
			model.setCallback(callback);
			model.solve();
			
			if(model.hasSolution())
			{
				bestSolution = model.getSolution();
				List<Item> selected = model.getSelectedItems(b.getItems());
				
				selected.forEach(it -> kernel.addItem(it));
				selected.forEach(it -> b.removeItem(it));
				
				model.exportSolution();
			}
			if(!bestSolution.isEmpty())
				objValues.get(objValues.size()-1).add(bestSolution.getObj());
			else
				objValues.get(objValues.size()-1).add(0.0);
				
			if(getRemainingTime() <= timeThreshold)
				return;
		}		
	}

	private int getRemainingTime()
	{
		return (int) (tlim - Duration.between(startTime, Instant.now()).getSeconds());
	}

	public List<List<Double>> getObjValues()
	{
		return objValues;
	}
}