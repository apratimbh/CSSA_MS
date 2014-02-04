package results;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;

import funcat.pr_store;
import pr_curve.curve;
import pr_curve.curve_point;

public class get_results {
	
	public static void main(String[] args)
	{
		get_results g=new get_results();
		g.get_file();
	}
	
	public void get_file()
	{
		double[][] cssa=read_file("E:/curve/church_curve_aims_ms.txt");
		double[][] aims=read_file("E:/curve/church_curve_aims_selected.txt");
		curve prc_cssa=new curve();
		double area1=0,area2=0;
		for(int i=0;i<50;i++)
		{
			curve_point pt=new curve_point(cssa[i][0],cssa[i][1]);
			area1+=cssa[i][0]*cssa[i][1];
			prc_cssa.add(pt);
		}
		curve prc_aims=new curve();
		for(int i=0;i<50;i++)
		{
			curve_point pt=new curve_point(aims[i][0],aims[i][1]);
			area2+=aims[i][0]*aims[i][1];
			prc_aims.add(pt);
		}
		System.out.println("Area-1: "+area1+"Area-2: "+area2);
	}
	
	public void make_monotonic(curve prc)
	{
		
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
					cols=line.split("\\s+").length;
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
				//line=line.replaceAll("\t", ",");
				for(String tmp : line.split("\\s+"))
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
