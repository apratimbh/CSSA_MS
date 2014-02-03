package results;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import funcat.pr_store;
import pr_curve.curve_point;

public class get_results {
	
	public void get_file()
	{
		double[][] cssa=read_file("");
		double[][] aims=read_file("");
		ArrayList<pr_store> pr_store_list=create_pr_store(cssa.length);
		for(int i=0;i<cssa.length;i++)
		{
			curve_point pt=new curve_point(0,0);
		}
		
	}
	
	public ArrayList<pr_store> create_pr_store(int num)
	{
		ArrayList<pr_store> pr_store_list=new ArrayList<pr_store>();
		for(int i=1;i<=num;i++)
		{
			pr_store tmp=new pr_store(i);
			pr_store_list.add(tmp);
		}
		return pr_store_list;
	}
	
	public double[][] read_file(String file)
	{
		double[][] temp=null;
		try {

			// -- read file size lines and rows

			System.out.println("Reading file: "+file);
			LineNumberReader reader  = new LineNumberReader(new FileReader(file));
			int rows = 0, cols=0;
			String line = "";
			while ((line = reader.readLine()) != null) {
				if(rows==0)
				{
					cols=line.split(",").length;
					rows++;
				}
			}

			rows = reader.getLineNumber(); 
			reader.close();

			System.out.println("File size: "+rows+" - "+cols);

			// -- read the file

			int i=0,j=0;
			temp = new double[rows][cols];
			BufferedReader br  = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null)
			{
				j=0;
				for(String tmp : line.split(","))
				{
					temp[i][j]=Double.parseDouble(tmp);
					j++;
				}
				i++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}

}
