package funcat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import lpsolve.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

public class template {
	public double[][] result=null,flabels=null,test_data=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	/*public static void main(String[] args) throws OWLOntologyCreationException
	{
		cssa o=new cssa();
		o.main();
	}*/

	public void main() throws OWLOntologyCreationException
	{
		ArrayList<String> vertex = null;
		try {
			vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex1")).readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		result=read_file("D:/matp/funcat_cellcyle_1.txt");
		//result=read_file("fun_cellcycle_result.txt");
		manager = OWLManager.createOWLOntologyManager();
		go = manager.loadOntologyFromOntologyDocument(new File("E:/ontologies/cellcycle_FUN.owl"));
		System.out.println("Loaded ontology: " + go.getOntologyID().toString());
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		reasoner = reasonerFactory.createReasoner(go, config);
		factory = manager.getOWLDataFactory();

		test_data=load_test_data("E:/dataset/cellcycle_FUN_test_expanded.arff",77);



		int limit=10,current=1;
		while(current<limit)
		{
			double tp=0;
			double fp=0;
			double fn=0;
			for(int test_ex_no=1;test_ex_no<result[0].length;test_ex_no++)
			{
				ArrayList<String> selected=new ArrayList<String>();
				// load the weights of each label
				double[] weights=new double[result.length];
				for(int i=0;i<result.length;i++)
				{
					weights[i]=result[i][test_ex_no];
				}
				// normalize the weights between 0 and 1. (we do not want negative weights)
				double max=weights[0],min=weights[0];
				for(int i=0;i<weights.length;i++)
				{
					if(weights[i]>max)
					{
						max=weights[i];
					}
					if(weights[i]<min)
					{
						min=weights[i];
					}
				}
				for(int i=0;i<weights.length;i++)
				{
					weights[i]=(weights[i]-min)/(max-min);
				}
				// ----------------------
				// INSERT ,CODE HERE
				
				// THE OUTPUT OF ASP SHOULD BE IN selected ARRAYLIST
				 ArrayList<String> parents=new ArrayList<String>();
					for(String s : selected)
					{
						OWLClass vertex_c = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+s));
						NodeSet<OWLClass> set=reasoner.getSuperClasses(vertex_c, false);
				 		for(Node<OWLClass> cls : set)
				 	 	{
				 	 		if(!cls.isTopNode())
				 	 		{
				 	 			String news=cls+"";
				 	 			String[] part=news.split(" ");
				 	 			part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
				 	 			if(!parents.contains(part[1]))
				 	 				parents.add(part[1]);
				 	 		}
				 	 	}
					}
					for(String s : parents)
					{
						selected.add(s);
					}
				// ----------------------
				int[] test_data_this_ex=new int[test_data[test_ex_no].length];
				for(int l=0;l<test_data[test_ex_no].length;l++)
				{
					test_data_this_ex[l]=(int) test_data[test_ex_no][l];
				}
				for(String s : selected)
				{
					if(test_data_this_ex[vertex.indexOf(s)]==1)
					{
						tp++;
					}
					else
					{
						fp++;
					}
				}
				for(int l=0;l<test_data[test_ex_no].length;l++)
				{
					if(test_data_this_ex[l]==1)
					{
						if(!selected.contains(vertex.get(l)))
						{
							fn++;
						}
					}
				}
			} // --for loop end // all examples

			System.out.println("Current-limit--"+current+"\n");
			current++;

			System.out.println("Precision: "+tp/(tp+fp));
			System.out.println("Recall: "+tp/(tp+fn));
			System.out.println();
		}
	}
	public double[][] load_test_data(String file,int no_of_columns_to_exclude)
	{
		double[][] temp=read_file(file);
		double[][] temp1=new double[temp.length][temp[0].length- no_of_columns_to_exclude];
		for(int j= no_of_columns_to_exclude+1;j<temp[0].length;j++)
		{
			for(int i=0;i<temp.length;i++)
			{
				temp1[i][j-no_of_columns_to_exclude]=temp[i][j];
			}
		}
		return temp1;
	}
	public ArrayList<String> get_vertex(ArrayList<String> vertex,double[] weights,double w)
	{
		ArrayList<String> tmp=new ArrayList<String>();
		for(int i=0;i<weights.length;i++)
		{
			if(weights[i]>=w)
			{
				tmp.add(vertex.get(i));
			}
		}
		return tmp;

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
