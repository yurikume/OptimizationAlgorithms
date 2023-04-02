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
		
		// Stampa degli items con i loro dati in un file esterno
		try {
	        PrintWriter out = new PrintWriter("items_list.txt");
	        out.println("****** Items dopo il sorting:");
	        out.println("Num items = " + items.size());
	        
	        int a,c;
		    double perc_good;
		    for(Item it: items) {
				a = it.getWeight();
				c = it.getProfit();
				// Calcoliamo la goodness, che è una misura da massimizzare. Preferiamo in questo modo item
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
			}else { // è un item y
				String vars[]= v.split("_");
				i = Integer.parseInt(vars[1]);
				t = Integer.parseInt(vars[2]);
				f = model.getSetupCost(i, t);
				d = model.getFamilyWeight(i);
				it = new Item(v, value, rc, f, d, 1);
			}
				
			items.add(it);
		}
		// Ora che ho tutti gli items x inizializzati, posso calcolare la goodness delle y
		List<Item> y_items = items.stream().filter(it -> it.getName().startsWith("y")).collect(Collectors.toList());
		int fam_setup_cost,fam_weight,knap_cap,weight_measure,profit_measure;
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
			
			res_knap_cap = knap_cap - fam_weight; // Capacità residua del knapsack per inserire gli item
			weight_percentage = res_knap_cap/items_weight; // Percentuale di item che ci stanno nel knapsack
			if(weight_percentage >= 1.0)
				weight_percentage = 1.0;
			
//			weight_measure = (items_weight + fam_weight) - knap_cap; // Da minimizzare
			profit_measure = items_profit + fam_setup_cost; // Setup cost è negativo (da massimizzare)
			
			y_goodness = profit_measure * weight_percentage; 
			// Prendo il profit che otterrei mettendo tutti gli items nel knapsack e lo moltiplico per la percentuale
			// di items che può essere in esso inserita ottenuta dalla stima precedente
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
		
		System.out.println("Dimensione items: "+items.size());
		List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it)).collect(Collectors.toList());
		// Stampa item disabilitati
		// toDisable.stream().forEach(it->System.out.println(it.getName()));
		model.disableItems(toDisable);
		model.setCallback(callback);
		
		// Stampa items del kernel
//		System.out.println("****** Items su cui opera il kernel :");
//		System.out.println("Num items = " + kernel.getItems().size());
//		kernel.getItems().stream().forEach(it->System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness()));
		
		model.solve();
		// Aggiunta
//		for(Item it : model.getSelectedItems(kernel.getItems())) {
//			it.set_in_kernel(true);
//		}
		
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
		// Se ho la configurazione BucketBuilder 6 (e Kernel Builder 4), che è quella del nuovo algoritmo, eseguo 
		// due volte solve buckets in modi differenti
		// In questo caso il parametro Numiterations non influenza, ma serve Itemslimit
		if(config.getBucketBuilder() instanceof BucketBuilderByGoodness) {
			solveBuckets(); // Eseguo la prima iterazione con i primi items_limit items di ogni famiglia
			// Dopo aver fatto la prima iterazione seleziono le y che ci sono nel kernel con tutte le loro x e uso queste
			// come sottoinsieme di items per fare la seconda iterazione
			System.out.println("\n\n****** INIZIO ITERAZIONE 2 ********");
			System.out.println("Tempo iterazione 1: " + Duration.between(startTime, Instant.now()).getSeconds());
			config.setBucketBuilder(new BucketBuilderByName());
			config.setKernelBuilder(new KernelBuilderByNamePercentage());
			// Stampa items del kernel
			//kernel.getItems().stream().forEach(it->System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness()));
			
			List<Item> y_ker = items.stream().filter(it -> kernel.contains(it) && it.getName().startsWith("y")).collect(Collectors.toList());
			System.out.println("Num famiglie selezionate dalla prima iterazione: "+y_ker.size());
			List<Item> newItems = new ArrayList<Item>();
			for(Item y_it : y_ker) {
				String vars[]= y_it.getName().split("_");
	            String fam = vars[1];
	            String knap = vars[2];
	            
	            newItems.addAll(items.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList()));
			}
			newItems.addAll(y_ker); // Lista degli item da usare nella seconda iterazione
			// Da qui devo rifare la kernel search sui nuovi items
			sorter.sort(newItems);
			
			// Stampa nuovi items
//			System.out.println("NUOVI ITEMS");
//			newItems.stream().forEach(it->System.out.println(it.getName()));
						
			kernelBuilder = config.getKernelBuilder(); // Il kernel builder by name prende tutte le famiglie con value positivo
			kernel = kernelBuilder.build(newItems, config);
			List<Item> sel_items = newItems.stream().filter(it -> it.getName().startsWith("y") || !kernel.contains(it)).collect(Collectors.toList());
			y_ker = newItems.stream().filter(it -> kernel.contains(it) && it.getName().startsWith("y")).collect(Collectors.toList());
			bucketBuilder = config.getBucketBuilder();
			buckets = bucketBuilder.build(sel_items, config, y_ker);
			
			solveKernel();
			solveBuckets(); // Nella seconda iterazione considero solo un sottoinsieme degli items 
		}else {
			// In tutti gli altri casi eseguo un certo numero di iterazioni della kernel search
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
		System.out.println("Tempo totale:" + tot_time);
		
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
		boolean first_iter = true;
		
		for(Bucket b : buckets) {
			System.out.println("\n\n\n\n\t\t** Solving bucket "+count++ +" **\n");
			
//			System.out.println("****** Items contenuti nel kernel:");
//			kernel.getItems().stream().forEach(it->System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness()));
			
			// Stampa items del bucket
//			System.out.println("\n****** Items su cui opera il bucket:");
//			System.out.println("Num items = " + b.getItems().size());
//			b.getItems().stream().forEach(it->System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness()));
			
			System.out.println("****** Items contenuti nel kernel");
			System.out.println("Num items = " + kernel.getItems().size());
			
//			for(Item it: kernel.getItems()) {
//				System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness());
//			}
			
			// Devo aggiungere/togliere gli item al bucket prima di questa riga che mi disabilita gli item per il modello
			List<Item> toDisable = items.stream().filter(it -> !kernel.contains(it) && !b.contains(it)).collect(Collectors.toList());
			
			Model model = new Model(instPath, logPath, Math.min(tlimBucket, getRemainingTime()), config, false);	
			model.buildModel();
			
			model.disableItems(toDisable);
			model.addBucketConstraint(b.getItems()); // can we use this constraint regardless of the type of variables chosen as items?			
			
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
				
//				for(Item it: selected){
//					kernel.addItem(it);
//					b.removeItem(it);
//					it.set_in_kernel(true);
//				}
				
				selected.forEach(it -> kernel.addItem(it));
				selected.forEach(it -> b.removeItem(it));
				
//				selected.forEach(it -> it.set_in_kernel(true));
//				List<Item> non_selected = items.stream().filter(it -> !selected.contains(it)).collect(Collectors.toList());
//				non_selected.forEach(it -> it.set_in_kernel(false));
//	
//				int num_items_da_reinserire = 3;
//				int cont = 0;
//				List<Item> x_items_non_selected = new ArrayList<Item>();
//				
//				List<Item> y_items_non_selected = non_selected.stream().filter(it -> it.getName().startsWith("y")).collect(Collectors.toList());
//				for(Item y_item: y_items_non_selected) {
//					kernel.addItem(y_item); // Qui ogni volta mette TUTTE le y nel kernel
//					y_item.set_in_kernel(true);
//					
//					String vars[]= y_item.getName().split("_");
//		            String fam = vars[1];
//		            String knap = vars[2];
//					
//					x_items_non_selected = non_selected.stream().filter(p -> p.getName().startsWith("x_"+fam) && p.getName().endsWith("_"+knap)).collect(Collectors.toList());
//					
//					for(Item x_item: x_items_non_selected) {
//						kernel.addItem(x_item);
//						x_item.set_in_kernel(true);
//						
//						cont++;
//						if(cont >= num_items_da_reinserire) break;
//					}
//				}
				
				
				// INIZIO MODIFICA REINSERIMENTO ITEMS
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
				
				// Stampa items selezionati dal bucket
//				System.out.println("****** Items selezionati dal bucket " + count + " :");
//				System.out.println("Num items = " + selected.size());
//				selected.stream().forEach(it->System.out.println(it.getName() + " :" + it.getRc() + " - value = " + it.getXr() + " - good% = " + it.getGoodness()));
				
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