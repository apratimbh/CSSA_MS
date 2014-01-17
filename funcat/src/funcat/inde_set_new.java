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

import max_cut.Fordfulkerson;

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

public class inde_set_new {
	public double[][] result=null,flabels=null,test_data=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	public static void main(String[] args) throws OWLOntologyCreationException
	{
		inde_set_new o=new inde_set_new();
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
		int limit=20,current=1;
		while(current<limit)
		{
			double tp=0;
			double fp=0;
			double fn=0;
			for(int test_ex_no=1;test_ex_no<result[0].length;test_ex_no++)
			{
				
				// load the weights of each label
				double[] weights=new double[result.length];
				ArrayList<String> selected=null;
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

				/*double[] tmp_weights=new double[vertex.size()];
				for(int i=0;i<weights.length;i++)
				{
					tmp_weights[i]=avg_parents_weight(weights,vertex,vertex.get(i));
				}
				for(int i=0;i<weights.length;i++)
				{
					weights[i]=tmp_weights[i];
				}*/
				// list of all nodes with weights above current
				selected=new ArrayList<String>();
				int k=0;
				int num=current;
				main_loop: while(true)
				{
					ArrayList<String> nvertex=get_vertex(vertex,weights,num);
					// list of vertices in the network
					ArrayList<vertex> vertex_list=new ArrayList<vertex>();
					// list of edges in the network
					ArrayList<edge> edge_list=new ArrayList<edge>();
					// create the source and destination vertices
					vertex vs=new vertex("s",2,0);
					vertex vd=new vertex("d",1,1);
					vertex_list.add(vs);
					vertex_list.add(vd);
					// each node corresponds to two vertices with an edge between them
					int vertex_num=2;
					for(String v : nvertex)
					{
						vertex v1=new vertex(v,1,vertex_num++);
						vertex v2=new vertex(v,2,vertex_num++);
						vertex_list.add(v1);
						vertex_list.add(v2);
						// lower-bound: w and capacity; inf.
						edge_list.add(new edge(v1,v2,weights[vertex.indexOf(v)],999999));
					}
					// we need edges between parents and child nodes
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
								// lower-bound: 0 and capacity; inf.
								edge_list.add(new edge(v1l,v2e,0,999999));
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
					// nodes without incoming vertices have a new edge added: source-node
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
						edge_list.add(new edge(vs,vi,0,999999));
					}
					// nodes without outgoing vertices have a new edge added: node-destination
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
							System.out.println("Her: Error: "+min_v.size()+" "+max_v.size());
							System.exit(0);
						}
						edge_list.add(new edge(vi,vd,0,999999));
					}

					// As we need to solve the max-cut problem, we swap the roles of the source and destination nodes
					Collections.swap(vertex_list, 0, 1);
					vertex_list.get(0).num=0;
					vertex_list.get(1).num=1;
					vertex_list.get(0).name="s";
					vertex_list.get(1).name="t";
					ArrayList<String> min_cut=new ArrayList<String>();
					// use modified FOrdFulkerson to solve the max-cut problem
					Fordfulkerson f=new Fordfulkerson();
					for(vertex vi : f.construct_r_graph(vertex_list, edge_list))
					{
						min_cut.add(vi.name);
					}
					// choose those nodes whose only one vertex (v1'-v2') is in the cut
					for(int i=0;i<min_cut.size();i++)
					{
						if(min_cut.get(i)!="s")
						{
							boolean flag=true;
							for(int j=0;j<min_cut.size();j++)
							{
								if((i!=j)&&(min_cut.get(i)==min_cut.get(j)))
								{
									flag=false;
								}
							}
							if(flag)
							{
								selected.add(min_cut.get(i));
							}
						}
					}
					if(selected.size()<current)
					{
						num++;
					}
					else 
					{
						break main_loop;
					}
				}
				/*for(String s : selected)
				{
					System.out.println(s);
				}
				System.exit(0);*/
				// add parents of the selected nodes into the selected list as they are logically implied
				ArrayList<String> parents=new ArrayList<String>();
				for(String s : selected)
				{
					OWLClass vertex_c = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+s));
					NodeSet<OWLClass> set=reasoner.getSuperClasses(vertex_c,false);
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
				/*FlowNetwork G = new FlowNetwork(vertex_list, edge_list);
				FordFulkerson maxflow = new FordFulkerson(G, 0, 1);
				double[][] dfsg=new double[vertex_list.size()][vertex_list.size()]; 
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

				// calculate p and r
				if(selected.size()>0)
				{
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
				}
			} // --for loop end // all examples

			System.out.println("Current-limit--"+current+"\n");
			current++;

			System.out.println("Precision: "+tp/(tp+fp));
			System.out.println("Recall: "+tp/(tp+fn));
			System.out.println();
		}
	}

	public double avg_parents_weight(double[] weights,ArrayList<String> vertex,String node)
	{
		double avg=0;
		int c=1;
		OWLClass vertex_c = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+node));
		NodeSet<OWLClass> set=reasoner.getSuperClasses(vertex_c, false);
		avg+=weights[vertex.indexOf(node)];
		for(Node<OWLClass> cls : set)
		{
			if(!cls.isTopNode())
			{
				String news=cls+"";
				String[] part=news.split(" ");
				part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
				avg+=weights[vertex.indexOf(part[1])];
				c++;
			}
		}
		return (double)avg/c;
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
	
	public void sort(ArrayList<String> vertex,double[] weights,int c)
	{
		for(int i=0;i<c;i++)
		{
			double max=weights[i];
			int index=i;
			for(int j=i;j<weights.length;j++)
			{
				if(weights[j]>max)
				{
					max=weights[j];
					index=j;
				}
			}
			double tmp=weights[i];
			weights[i]=weights[index];
			weights[index]=tmp;
			Collections.swap(vertex, i, index);
		}
	}
	
	public ArrayList<String> get_vertex(ArrayList<String> vertex,double[] weights,int num)
	{
		ArrayList<String> tmp=new ArrayList<String>();
		double[] tmp_weights=new double[weights.length];
		ArrayList<String> tmp_vertex=new ArrayList<String>();
		for(int i=0;i<weights.length;i++)
		{
			tmp_weights[i]=weights[i];
		}
		for(String s: vertex)
		{
			tmp_vertex.add(s);
		}
		sort(tmp_vertex,weights,num);
		for(int i=0;i<num;i++)
		{
			tmp.add(tmp_vertex.get(i));
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