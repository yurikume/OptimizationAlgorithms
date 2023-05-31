package kernel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleAttr;
import gurobi.GRB.IntAttr;
import gurobi.GRB.StringAttr;
import gurobi.GRBCallback;

public class Model
{
	private String mpsFilePath;
	private String logPath;
	private int timeLimit;
	private Configuration config;
	private boolean lpRelaxation;
	private GRBEnv env;
	private GRBModel model;
	private boolean hasSolution;
	private double positiveThreshold = 1e-5;
	private int c[][][];
	private int aij[][];
	private int fit[][];
	private int di[];
	private int bt[];
	private final int maxT = 20;
	private final int maxN = 30;
	private final int maxNItems = 60;
	
	public Model(String mpsFilePath, String logPath, int timeLimit, Configuration config, boolean lpRelaxation)
	{
		this.c = new int[maxN][maxNItems][maxT];
		this.aij = new int[maxN][maxNItems];
		this.fit = new int[maxN][maxT];
		this.di = new int[maxN];
		this.bt = new int[maxT];
		this.mpsFilePath = mpsFilePath;
		this.logPath = logPath;
		this.timeLimit = timeLimit;	
		this.config = config;
		this.lpRelaxation = lpRelaxation;
		this.hasSolution = false;
	}
	
	public void buildModel()
	{
		try
		{
			env = new GRBEnv();
			setParameters();
			//model = new GRBModel(env, mpsFilePath);
			model = new GRBModel(env);
			createModel();
			model.update();
			if(lpRelaxation) {
				model = model.relax();
			}
				
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}

	private void createModel() {
		try(BufferedReader reader = new BufferedReader(new FileReader(mpsFilePath))){
			int T = Integer.parseInt(reader.readLine());
			int N = Integer.parseInt(reader.readLine());
			String line=reader.readLine();
		    String num[]=line.split(" ");
		    int ni[]=new int[N];
		    
		    int nTotItems=0;
		    int nTot = 0;
		     
		    for(int i=0; i < N;i++) {
		    	 ni[i]=Integer.parseInt(num[i]);
		    	 nTotItems+=ni[i];
		    }
		    
		    int maxNItems = ni[0];
		    for(int i = 1; i < N; i++) {
		    	if(ni[i] > maxNItems) {
		    		maxNItems = ni[i];
		    	}
		    }
		    
		    line=reader.readLine();
		    num=line.split(" ");
		     
		    int bt[]=new int[T];
		    for(int i=0; i < T;i++) {
		    	bt[i]=Integer.parseInt(num[i]);
		    }
		    this.bt = bt;
		     
		    line=reader.readLine();
		    num=line.split(" ");
		     
		    int c[][][] = new int[N][maxNItems][T];
		    for(int t = 0; t < T; t++) {
		    	nTot=0;
			   	for(int i = 0; i < N; i++) {
		    		for(int j = 0; j < ni[i]; j++) {
		    			c[i][j][t] = Integer.parseInt(num[j+nTot+t*nTotItems]);	
		    		}
		    		nTot += ni[i];
		    	}		
			 }
		     this.c = c;
		    
		     line=reader.readLine();
		     num=line.split(" ");
		     
		     int fit[][]=new int[N][T];
		     for(int t = 0; t < T; t++) {
		    	 for(int i = 0; i < N; i++) {
		    		 fit[i][t] = Integer.parseInt(num[i+t*N]);
		    	 }
		     }
		     this.fit = fit;
		     
		     line=reader.readLine();
		     num=line.split(" ");
		     int di[] = new int[N];
		     for(int i=0; i < N;i++) {
		    	 di[i]=Integer.parseInt(num[i]);
		     }
		     this.di = di;
		     
		     nTot = 0;
		     line=reader.readLine();
		     num=line.split(" ");
		     
		     int aij[][]=new int[N][maxNItems];
		     for(int i = 0; i < N; i++) {
		    	 for(int j = 0; j < ni[i]; j++) {
		    		 aij[i][j]=Integer.parseInt(num[j+nTot]);
		    	 }
		    	 nTot = nTot + ni[i];
		     }	
		     this.aij = aij;
		     
		    // Variables
		    ArrayList<ArrayList<ArrayList<GRBVar>>> x = new ArrayList<>();
		    ArrayList<ArrayList<GRBVar>> xFam = new ArrayList<>();
		    ArrayList<GRBVar> xItems = new ArrayList<>();
		    
		    
		    ArrayList<ArrayList<GRBVar>> y = new ArrayList<>();
		    ArrayList<GRBVar> yFam = new ArrayList<>();
		    
		   
		    for(int t = 0; t < T; t++) {
		    	xFam = new ArrayList<>();
		    	for(int i = 0; i < N; i++) {
		    		xItems = new ArrayList<>();
		    		for(int j = 0; j < ni[i]; j++) {
		    			xItems.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_"+i+"_"+j+"_"+t));
		    		}
		    		xFam.add(xItems);
		    	}
		    	x.add(xFam);
			}
		    for(int t = 0; t < T; t++) {
		    	yFam = new ArrayList<>();
		    	for(int i = 0; i < N; i++) {
		    		yFam.add(model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y_"+i+"_"+t));
		    	}
		    	y.add(yFam);
		    }
		    
		    nTot = 0;
		    
		    // Objective function
		    GRBLinExpr expr = new GRBLinExpr();
		    for(int t = 0; t < T; t++) {
		    	for(int i = 0; i < N; i++) {
		    		for(int j = 0; j < ni[i]; j++) {
		    			expr.addTerm(c[i][j][t], x.get(t).get(i).get(j));
		    		}
		    	}
			}
		    
		    for(int t = 0; t < T; t++) {
		    	for(int i = 0; i < N; i++) {
		    		expr.addTerm(fit[i][t], y.get(t).get(i));
		    	}
		    }
		    
		    model.setObjective(expr, GRB.MAXIMIZE);
		    
		    // Constraint 2.2
		    for(int t = 0; t < T; t++) {
		    	expr = new GRBLinExpr();
		    	for(int i = 0; i < N; i++) {	
			    	for(int j = 0; j < ni[i]; j++) {
			    		expr.addTerm(aij[i][j], x.get(t).get(i).get(j));
			    	}
		    	}
		    	for(int i = 0; i < N; i++) {
		    		expr.addTerm(di[i], y.get(t).get(i));
		    	}
		    	model.addConstr(expr, GRB.LESS_EQUAL, bt[t], "2.2."+t);
		    }
		    
		    // Constraint 2.3
		    int numConstr = 0;
		    for(int t = 0; t < T; t++) {
		    	for(int i = 0; i < N; i++) {
		    		for(int j = 0; j < ni[i]; j++) {
		    			expr = new GRBLinExpr();
		    			expr.addTerm(1.0, x.get(t).get(i).get(j));
		    			expr.addTerm(-1.0, y.get(t).get(i));
		    			model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "2.3."+(numConstr++));
		    		}
		    	}
			}
		    
		    // Constraint 2.4
		    for(int i = 0; i < N; i++) {
		    	expr = new GRBLinExpr();
		    	for(int t = 0; t < T; t++) {
		    		expr.addTerm(1.0, y.get(t).get(i));
		    	}
		    	model.addConstr(expr, GRB.LESS_EQUAL, 1.0, "2.4."+i);
		    }
		}catch(IOException e) {
	
		}catch(GRBException e) {
			
		}
	}

	private void setParameters() throws GRBException
	{                 
		env.set(GRB.StringParam.LogFile, logPath+"log.txt");
		env.set(GRB.IntParam.Threads, config.getNumThreads());
		env.set(GRB.IntParam.Presolve, config.getPresolve());
		env.set(GRB.DoubleParam.MIPGap, config.getMipGap());
		if (timeLimit > 0)
			env.set(GRB.DoubleParam.TimeLimit, timeLimit);
		
	}
	
	public void solve()
	{
		try
		{
			model.optimize();
			if(model.get(IntAttr.SolCount) > 0)
				hasSolution = true;
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public List<String> getVarNames()
	{
		List<String> varNames = new ArrayList<>();
		
		for(GRBVar v : model.getVars())
		{
			try
			{
				varNames.add(v.get(StringAttr.VarName));
			} catch (GRBException e)
			{
				e.printStackTrace();
			}
		}
		return varNames;
	}

	public double getVarValue(String v)
	{
		try
		{
			if(model.get(IntAttr.SolCount) > 0)
			{
				return model.getVarByName(v).get(DoubleAttr.X);
			}
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public double getVarRC(String v)
	{
		try
		{
			if(model.get(IntAttr.SolCount) > 0)
			{
				return model.getVarByName(v).get(DoubleAttr.RC);
			}
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	public void disableItems(List<Item> items)
	{
		try
		{
			for(Item it : items)
			{
				model.addConstr(model.getVarByName(it.getName()), GRB.EQUAL, 0, "FIX_VAR_"+it.getName());
			}
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void exportSolution()
	{
		try
		{
			model.write("bestSolution.sol");
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public void readSolution(String path)
	{
		try
		{
			model.read(path);
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public void readSolution(Solution solution)
	{
		try
		{
			for(GRBVar var : model.getVars())
			{
				var.set(DoubleAttr.Start, solution.getVarValue(var.get(StringAttr.VarName)));
			}
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}

	public boolean hasSolution()
	{
		return hasSolution;
	}
	
	public Solution getSolution()
	{
		Solution sol = new Solution();
		
		try
		{
			sol.setObj(model.get(DoubleAttr.ObjVal));
			Map<String, Double> vars = new HashMap<>();
			for(GRBVar var : model.getVars())
			{
				vars.put(var.get(StringAttr.VarName), var.get(DoubleAttr.X));
			}
			sol.setVars(vars);
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
		return sol;
	}
	
	public void addBucketConstraint(List<Item> items)
	{
		GRBLinExpr expr = new GRBLinExpr();
			
		try
		{
			for(Item it : items)
			{
				expr.addTerm(1, model.getVarByName(it.getName()));
			}
			model.addConstr(expr, GRB.GREATER_EQUAL, 1, "bucketConstraint");
		} catch (GRBException e)
		{
			e.printStackTrace();
		}	
	}

	public void addObjConstraint(double obj)
	{
		try
		{
			System.out.println("sol = " + obj);
			model.getEnv().set(GRB.DoubleParam.Cutoff, obj);
		} catch (GRBException e)
		{
			e.printStackTrace();
		}
	}
	
	public List<Item> getSelectedItems(List<Item> items)
	{
		List<Item> selected = new ArrayList<>();
		for(Item it : items)
		{
			try
			{
				if(model.getVarByName(it.getName()).get(DoubleAttr.X)> positiveThreshold)
					selected.add(it);
			} catch (GRBException e)
			{
				e.printStackTrace();
			}
		}
		return selected;
	}
	
	public void setCallback(GRBCallback callback) 
	{ 
		model.setCallback(callback);
	}
	
	public int getProfit(int i, int j, int t) {
		return this.c[i][j][t];
	}
	
	public int getWeight(int i, int j) {
		return this.aij[i][j];
	}
	
	public int getSetupCost(int i, int t) {
		return this.fit[i][t];
	}
	
	public int getFamilyWeight(int i) {
		return this.di[i];
	}
	
	public int getKnapsackCapacity(int t) {
		return this.bt[t];
	}
}