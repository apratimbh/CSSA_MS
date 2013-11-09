import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;


public class weka_test {
	public static void main(String[] args)
	{
		weka_test wt=new weka_test();
		wt.create_files();
	}
	public void create_files()
	{
		double[][] data=read_file("D:/matp/TData.txt");
		double[][] rlabels=read_file("D:/matp/TResult.txt");
		double[][] test=read_file("D:/matp/TTest.txt");
		for(int j=0;j<rlabels[0].length;j++)
		{
			double[] tmp=new double[rlabels.length];
			for(int i=0;i<rlabels.length;i++)
			{
				tmp[i]=rlabels[i][j];
			}
			create_arff_file(data[0].length,data,tmp,"E:/weka/"+(j+1)+".arff");
		}
	}
	public void create_arff_file(int no_of_attr,double[][] attr_val,double[] result,String file)
	{
		try {
			BufferedWriter bw=new BufferedWriter(new FileWriter(file));
			bw.write("@RELATION 'attr_test'\n");
			for(int i=1;i<=no_of_attr;i++)
			{
				bw.write("@ATTRIBUTE attr"+i+" numeric\n");
			}
			bw.write("@ATTRIBUTE val numeric\n@DATA\n");
			for(int i=0;i<attr_val.length;i++)
			{
				String line="";
				for(int j=0;j<attr_val[i].length;j++)
				{
					line+=attr_val[i][j]+",";
				}
				line+=result[i]+"\n";
				bw.write(line);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
