package kernel;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.time.Instant;

import gurobi.*;

public class CustomCallback extends GRBCallback
{
	private String path;
	private PrintStream log;
	private Instant startTime;
	private double bestObj;

	public CustomCallback(String path, Instant startTime)
	{
		this.path = path;
		this.startTime = startTime;
		bestObj = GRB.INFINITY;
		try
		{
			this.log = new PrintStream(new BufferedOutputStream(new FileOutputStream(this.path + "/" + "log_best_solutions.txt")), true);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	protected void callback()
	{
		try
		{
			if (where == GRB.CB_MIPSOL)
			{
				double obj = getDoubleInfo(GRB.CB_MIPSOL_OBJ);
				
				if(obj != bestObj)
				{
					bestObj = obj;
					long time = Duration.between(startTime, Instant.now()).getSeconds();
					log.println("OBJ:" + obj + "  -  TIME: " + time + " s.");					
				}
			}
		} catch (GRBException e)
		{
			System.out.println("Error code: " + e.getErrorCode());
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}