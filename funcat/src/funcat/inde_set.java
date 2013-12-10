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
import java.util.HashMap;

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

import FordFulkerson.FordFulkerson;
import FordFulkerson.FlowNetwork;
import FordFulkerson.StdOut;
import FordFulkerson.FlowEdge;

public class inde_set {
	public double[][] result=null,flabels=null,test_data=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	public static void main(String[] args) throws OWLOntologyCreationException
	{
		inde_set o=new inde_set();
		o.main();
	}

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
		double limit=0.0,current=0.9;
		while(current>limit)
		{
			double tp=0;
			double fp=0;
			double fn=0;
			for(int test_ex_no=1;test_ex_no<result[0].length;test_ex_no++)
			{
				// --- create lists
				//System.out.println("\n\nNew example - - - - - - - - - -"+test_ex_no);
				int vertex_num=2;
				double[] weights=new double[result.length];
				ArrayList<String> selected=null;
				for(int i=0;i<result.length;i++)
				{
					weights[i]=result[i][test_ex_no];
				}
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
				ArrayList<String> nvertex=get_vertex(vertex,weights,current);
				//System.out.println("nvertex size: "+nvertex.size());
				/*if(nvertex.size()==0)
					continue;*/
				ArrayList<vertex> vertex_list=new ArrayList<vertex>();
				ArrayList<edge> edge_list=new ArrayList<edge>();
				for(String v : nvertex)
				{
					vertex v1=new vertex(v,1,vertex_num++);
					vertex v2=new vertex(v,2,vertex_num++);
					vertex_list.add(v1);
					vertex_list.add(v2);
					edge_list.add(new edge(v1,v2,weights[vertex.indexOf(v)]));
				}
				for(String v1: nvertex)
				{
					OWLClass c1=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+v1));
					for(String v2 : nvertex)
					{
						OWLClass c2=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+v2));
						OWLAxiom axiom=factory.getOWLSubClassOfAxiom(c2, c1);
						if(reasoner.isEntailed(axiom))
						{
							vertex v1l=null,v2e=null;
							for(vertex v : vertex_list)
							{
								if(v.name==v1&&v.type==2)
								{
									v1l=v;
								}
								else if(v.name==v2&&v.type==1)
								{
									v2e=v;
								}
							}
							edge_list.add(new edge(v1l,v2e,999999));
						}
					}
				}
				// --check which vertices do not have incoming edges
				ArrayList<String> has_in_e=new ArrayList<String>();
				for(edge e : edge_list)
				{
					if(e.v2.type==1)
						has_in_e.add(e.v2.name);
				}
				ArrayList<String> max_v=new ArrayList<String>();
				for(String v : nvertex)
				{
					if(!has_in_e.contains(v))
					{
						max_v.add(v);
					}
				}
				// -- check which vertices do not have out-going edges
				ArrayList<String> has_out_e=new ArrayList<String>();
				for(edge e : edge_list)
				{
					if(e.v1.type==2)
						has_out_e.add(e.v1.name);
				}
				ArrayList<String> min_v=new ArrayList<String>();
				for(String v : nvertex)
				{
					if(!has_out_e.contains(v))
					{
						min_v.add(v);
					}
				}
				/*System.out.println("printing-");
			System.out.println();
			for(String s : max_v)
			{
				System.out.print(s+" / ");
			}
			System.out.println();
			for(String s : min_v)
			{
				System.out.print(s+" / ");
			}
			System.out.println();
			System.exit(0);*/
				// --- create source and destination vertices
				vertex vs=new vertex("s",2,0);
				vertex vd=new vertex("d",1,1);
				vertex_list.add(vs);
				vertex_list.add(vd);
				// -- create incoming edges
				for(String v : max_v )
				{
					vertex vi=null;
					for(vertex vv : vertex_list)
					{
						if(vv.name==v&&vv.type==1)
						{
							vi=vv;
						}
					}
					edge_list.add(new edge(vs,vi,999999));
				}
				// -- create outgoing edges

				for(String v : min_v )
				{
					vertex vi=null;
					for(vertex vv : vertex_list)
					{
						if(vv.name==v&&vv.type==2)
						{
							vi=vv;
						}
					}
					if(vi==null)
					{
						System.out.println("Error: "+min_v.size()+" "+max_v.size());
						System.exit(0);
					}
					edge_list.add(new edge(vi,vd,999999));
				}
				/*System.out.println("No of vertices: "+vertex_list.size()+" Vertex No: "+vertex_num);
			System.exit(0);*/
				FlowNetwork G = new FlowNetwork(vertex_list, edge_list);
				FordFulkerson maxflow = new FordFulkerson(G, 0, 1);
				/*double[][] dfsg=new double[vertex_list.size()][vertex_list.size()]; 
			int m[]= new int[vertex_list.size()];
			for (int i=0; i<vertex_list.size(); i++)  
			{  
				m[i] = 0;  
			}*/
				//StdOut.println("Max flow from " + 0 + " to " + 1);
				/*	for (int v = 0; v < G.V(); v++) {
				for (FlowEdge e : G.adj(v)) {
					if ((v == e.from()) && e.flow() > 0)
						//StdOut.println("   " + e);
						//dfsg[e.from()][e.to()]=e.capacity();
				}
			}
			dfs(dfsg,m,0,vertex_list.size());*/
				ArrayList<Integer> min_cut=new ArrayList<Integer>();
				ArrayList<Integer> min_cut_r=new ArrayList<Integer>();
				selected=new ArrayList<String>();
				for (int v = 1; v < G.V(); v++) {
					if (maxflow.inCut(v)) 
					{
						min_cut.add(v);
					}
				}
				for(int i : min_cut)
				{
					if(i>1&&!min_cut.contains(i+1))
					{
						min_cut_r.add(i);
					}
				}
				for(vertex v : vertex_list)
				{
					if(min_cut_r.contains(v.num))
					{
						selected.add(v.name);
					}
				}
				ArrayList<String> parents=new ArrayList<String>();
				for(String node : selected)
				{
					OWLClass t_cls = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+node));
					NodeSet<OWLClass> set=reasoner.getSuperClasses(t_cls, false);
					//System.out.println("\nLabel: "+temp+"\tSuperClasses:");
					for(Node<OWLClass> cls : set)
					{
						if(!cls.isTopNode())
						{
							String news=cls+"";
					 		news=news.split(" ")[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
					 		parents.add(news);
						}
					}
				}
				for(String s: parents)
				{
					if(!selected.contains(s))
						selected.add(s);
				}
				/*System.out.println("Visited vertices: -\n");
			for(int i: max_cut)
			{
				System.out.print(i+" / ");
			}
			System.out.println();
			// print min-cut*/
				/*StdOut.print("Min cut: ");
			for (int v = 0; v < G.V(); v++) {
				if (maxflow.inCut(v)) StdOut.print(v + " ");
			}
			StdOut.println();

			StdOut.println("Max flow value = " +  maxflow.value());*/
				// --------------
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
			current-=0.05;

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