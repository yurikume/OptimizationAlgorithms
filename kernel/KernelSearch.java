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
		
		// Stampa degli items con i loro dati in un file esterno
		// Commento
		try {
	        PrintWriter out = new PrintWriter("items_list.txt");
	        out.println("****** Items dopo il sorting:");
	        out.println("Num items = " + items.size());
	        
	        int a,c;
		    double perc_good;
		    for(Item it: items) {
				a = it.getWeight();
				c = it.getProfit();
				// Calcoliamo la differenza percentuale, che è una misura da massimizzare. Preferiamo in questo modo item
				// con peso piccolo e differenza tra profitto e peso elevata, che abbiamo visto anche dalle soluzioni essere
				// i migliori
				perc_good = it.getGoodness();
				out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - c = " + c + " - a = " + a + " - good% = " + perc_good);
		    }

	        out.close();  
	      	System.out.println("Successfully wrote to the file.");
	    }catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
		}
		
//		kernel = kernelBuilder.build(items, config);
//		buckets = bucketBuilder.build(items.stream().filter(it -> !kernel.contains(it)).collect(Collectors.toList()), config);
		
		// Solo kernel by goodness
		kernel = kernelBuilder.build(items, config);
		List<Item> sel_items = items.stream().filter(it -> it.getName().startsWith("y") || !kernel.contains(it)).collect(Collectors.toList());
		List<Item> y_ker = items.stream().filter(it -> kernel.contains(it) && it.getName().startsWith("y")).collect(Collectors.toList());
//		for(Item it : kernel.getItems()) {
//			if(it.getName().startsWith("y")) {
//				sel_items.add(it);
//			}
//		}
//		sorter.sort(sel_items);
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
		int i,j,t,c,a,f,d;    
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
				it = new Item(v, value, rc, c, a);
			}else { // è un item y
				String vars[]= v.split("_");
				i = Integer.parseInt(vars[1]);
				t = Integer.parseInt(vars[2]);
				f = model.getSetupCost(i, t);
				d = model.getFamilyWeight(i);
				it = new Item(v, value, rc, f, d);
			}
				
			items.add(it);
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
		
		System.out.println("****** Items su cui opera il kernel :");
		System.out.println("Num items = " + kernel.getItems().size());
		
		for(Item it: kernel.getItems()) {
			System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness());
		}
		
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
		for (int i = 0; i < numIterations; i++)
		{
			if(getRemainingTime() <= timeThreshold)
				return;
			if(i != 0)
				objValues.add(new ArrayList<>());
			
			System.out.println("\n\n\n\t\t******** Iteration "+i+" ********\n\n\n");
			solveBuckets();			
		}
		System.out.println("Tempo totale:" + Duration.between(startTime, Instant.now()).getSeconds());
	}

	private void solveBuckets()
	{
		int count = 0;
		
		for(Bucket b : buckets)
		{
			System.out.println("\n\n\n\n\t\t** Solving bucket "+count++ +" **\n");
			List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it) && !b.contains(it)).collect(Collectors.toList());

			Model model = new Model(instPath, logPath, Math.min(tlimBucket, getRemainingTime()), config, false);	
			model.buildModel();
					
			model.disableItems(toDisable);
			model.addBucketConstraint(b.getItems()); // can we use this constraint regardless of the type of variables chosen as items?
			
			System.out.println("****** Items su cui opera il bucket " + count + " :");
			System.out.println("Num items = " + b.getItems().size());
			Bucket b_copy = new Bucket();
			b_copy.copy(b.getItems());
			
			// Aggiunta per builder by goodness
			for(Item it: b_copy.getItems()) {
				if(it.getName().startsWith("y") && kernel.contains(it)) { // Se il kernel già contiene l'item non lo rimetto (per le y)
					b.removeItem(it);
					System.out.println("Item " + it.getName() + " rimosso");
				}
				System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness());
			}
			
			System.out.println("****** Items contenuti nel kernel");
			System.out.println("Num items = " + kernel.getItems().size());
			
//			for(Item it: kernel.getItems()) {
//				System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness());
//			}
			
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
				
				for(Item it: selected){
					kernel.addItem(it);
					b.removeItem(it);
					it.set_in_kernel(true);
				}
				
				// selected.forEach(it -> kernel.addItem(it));
				// selected.forEach(it -> b.removeItem(it));
				
				
				// selected.forEach(it -> it.set_in_kernel(true));
				List<Item> non_selected = items.stream().filter(it -> !selected.contains(it)).collect(Collectors.toList());
				non_selected.forEach(it -> it.set_in_kernel(false));
				
				
				int num_items_da_reinserire = 3;
				int cont = 0;
				List<Item> x_items_non_selected = new ArrayList<Item>();
				
				List<Item> y_items_non_selected = non_selected.stream().filter(it -> it.getName().startsWith("y")).collect(Collectors.toList());
				for(Item y_item: y_items_non_selected) {
					kernel.addItem(y_item);
					y_item.set_in_kernel(true);
					
					String vars[]= y_item.getName().split("_");
		            String fam = vars[1];
		            String knap = vars[2];
					
					x_items_non_selected = non_selected.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
					
					for(Item x_item: x_items_non_selected) {
						kernel.addItem(x_item);
						x_item.set_in_kernel(true);
						
						cont++;
						if(cont >= num_items_da_reinserire) break;
					}
				}
				
				
				// INIZIO MODIFICA
				// Promising items chosen through the goodness measure
//				double perc_good;
//				double threshold = 0.5; // è un esempio
//				int a,c;
//				// We iterate over the remaining items in the bucket, i.e. the ones that have not been selected 
//				for(Item it : b.getItems()) {
//					String name = it.getName();
//					c = it.getProfit();
//		            a = it.getWeight();
//					perc_good = it.getGoodness();
//		            System.out.println(String.format("%s, good_perc: %f, c: %d, a: %d ",name,perc_good,c,a));
//					if(name.startsWith("x") && perc_good > threshold) {
//			            System.out.println("Item " + name + " added!");
//		            	kernel.addItem(it);
//					}	
//				}
				
//				System.out.println("****** Items rimasti nel bucket " + count + " :");
//				for(Item it: b.getItems()) {
//					System.out.println(it.getName() + " - value = " + model.getVarValue(it.getName()));
//				}
				System.out.println("****** Items selezionati dal bucket " + count + " :");
				System.out.println("Num items = " + selected.size());
				for(Item it: selected) {
					System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness());
				}
				
				// FINE MODIFICA
				
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