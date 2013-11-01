import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
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

public class fv_arr {
	public static int[][] path = null, dis = null, is_a = null, part_of = null;
	public static ArrayList<String> vertex = null;
	public static void main(String[] args) throws OWLOntologyCreationException
	{
		double[][] arr = null;
		int a1=0,a2=0;
		int n_labels=0;
		try {
			File file = new File("E:/ontologies/cellcycle_FUN.owl"); //go_daily-termdb  treeOnt
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology go = manager.loadOntologyFromOntologyDocument(file);
			System.out.println("Loaded ontology: " + go.getOntologyID().toString());
			OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
			OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
			OWLReasoner reasoner = reasonerFactory.createReasoner(go, config);
			OWLDataFactory factory = manager.getOWLDataFactory();
			BufferedReader br = null;
			BufferedWriter bw = null;
			dis=(int[][]) new ObjectInputStream(new FileInputStream("dis")).readObject();
			path=(int[][]) new ObjectInputStream(new FileInputStream("path")).readObject();
			part_of=(int[][]) new ObjectInputStream(new FileInputStream("part_of")).readObject();
			is_a=(int[][]) new ObjectInputStream(new FileInputStream("is_a")).readObject();
			vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex1")).readObject();
			int max=0,n=vertex.size();
			for (int i=0; i<n; i++) 
			{
				for (int j=0; j<n;j++) 
				{
					if(dis[i][j]!=99999)
					{
						if (dis[i][j]>max) 
						{
							max = dis[i][j];
						}
					}
				}
			}
			System.out.println("Max:: "+max);
			double[][] fv_arr=new double[vertex.size()][vertex.size()];
			System.out.println(".");
			int p=0,prev=0;
			for(String temp : vertex)
			{
				if((p*100)/vertex.size()!=prev)
					System.out.println((p*100)/vertex.size()+"%");
				prev=(p*100)/vertex.size();
				//System.out.println(temp);
				ArrayList<Double> vec=new ArrayList<Double>();
				for(String temp1 : vertex)
				{
					vec.add(new Double(0));
				}
				vec.set(vertex.indexOf(temp),new Double(1));
				OWLClass t_cls = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+temp));
				/*for(String news : vertex)
			 	{

				 			int i=vertex.indexOf(news);
				 			int j=vertex.indexOf(temp);
				 			double cost=(double)((max+1)-(double)dis[i][j])/(max+1);
				 			if(cost>vec.get(vertex.indexOf(news)))
				 			{
				 				vec.set(vertex.indexOf(news),new Double(cost));
				 				//System.out.println("Present super class: "+temp+" "+news+" Distance "+dis[m1][n1]);
				 			}
			 	}*/
				NodeSet<OWLClass> set=reasoner.getSuperClasses(t_cls, false);
				System.out.println("\nLabel: "+temp+"\tSuperClasses:");
				for(Node<OWLClass> cls : set)
				{
					if(!cls.isTopNode())
					{
						String news=cls+"";
						news=news.split(" ")[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
						System.out.println("\t\t\t"+news);
						if(vertex.contains(news))
						{
							int i=vertex.indexOf(news);
							int j=vertex.indexOf(temp);
							double cost=(double)((max+1)-(double)dis[i][j])/(max+1);
							if(1>vec.get(vertex.indexOf(news)))
							{
								int m1=vertex.indexOf(temp);
								int n1=vertex.indexOf(news);
								vec.set(vertex.indexOf(news),new Double(1));
								//System.out.println("Present super class: "+temp+" "+news+" Distance "+dis[m1][n1]);
							}
						}
					}
				}
				/*set=reasoner.getSubClasses(t_cls, false);
				//System.out.println("\nLabel: "+temp+"\tSuperClasses:");
				for(Node<OWLClass> cls : set)
			 	{
			 		if(!cls.isBottomNode())
			 		{
				 		String news=cls+"";
				 		news=news.split(" ")[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
				 		//System.out.println("\t\t\t"+news);
				 		if(vertex.contains(news))
				 		{
				 			int i=vertex.indexOf(news);
				 			int j=vertex.indexOf(temp);
				 			double cost=(double)((max+1)-(double)dis[i][j])/(max+1);
				 			if(cost>vec.get(vertex.indexOf(news)))
				 			{
				 				int m1=vertex.indexOf(temp);
				 				int n1=vertex.indexOf(news);
				 				vec.set(vertex.indexOf(news),new Double(cost));
				 				//System.out.println("Present super class: "+temp+" "+news+" Distance "+dis[m1][n1]);
				 			}
				 		}
			 		}
			 	}*/
				int c=0;
				for(double d : vec)
				{
					fv_arr[vertex.indexOf(temp)][c]=d;
					c++;
				}
				p++;
			}
			System.out.println("100%\n");
			PrintWriter pr = new PrintWriter("fun_cellcycle_fv_arr_one.txt");    

			for (int i=0; i<fv_arr.length ; i++)
			{
				String temp=Arrays.toString(fv_arr[i]);
				pr.println(temp.substring(1, temp.length()-1).replaceAll(",", ""));
			}
			pr.close();
			System.exit(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}