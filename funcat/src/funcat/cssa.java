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

public class cssa {
	public double[][] result=null,flabels=null,test_data=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	public static void main(String[] args) throws OWLOntologyCreationException
	{
		cssa o=new cssa();
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



		int limit=10,current=1;
		while(current<limit)
		{
			double tp=0;
			double fp=0;
			double fn=0;
			for(int test_ex_no=1;test_ex_no<result[0].length;test_ex_no++)
			{
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
				// CSSA

				// list of supernodes (each supernode contains one node and snv is the weight of that node)
				ArrayList<Supernode> sn_list=create_supernodes(vertex,weights,vertex);
				// kernel density assigned
				ArrayList<Supernode> assigned=new ArrayList<Supernode>();
				// kernel density value
				HashMap<Supernode,Double> allocated_density=new HashMap<Supernode,Double>();
				// selected nodes
				ArrayList<String> selected=new ArrayList<String>();

				// -------------

				// ------ CSSA

				int k=0;
				// k1 keeps track of the number of ms labels (nodes) selected so far
				while(k<current)
				{	
					// ----- select supernode with max snv
					Supernode selected_sn=sn_list.get(0);
					int selected_sn_idx=0;
					double max_snv=sn_list.get(0).snv;
					// selected_sn is the supernode with the highest snv
					
					for(Supernode tmp : sn_list)
					{
						if(tmp.snv>max_snv)
						{
							selected_sn=tmp;
							selected_sn_idx=sn_list.indexOf(tmp);
							max_snv=tmp.snv;
						}
					}
					
					/*System.out.println("Selected supernode: ");
					selected_sn.print();*/
					// check if all its parents are selected
					if(all_parents_selected(selected,selected_sn.parents_list))
					{
						sn_list.remove(selected_sn);
						assigned.add(selected_sn);
						allocated_density.put(selected_sn, (double)(current-k)/selected_sn.size);
						k+=selected_sn.size;
						// add nodes to the selected list
						for(String tmp : selected_sn.nodes)
						{
							if(!selected.contains(tmp))
							{
								selected.add(tmp);
							}
						}
					}
					else
					{
						Supernode parent=find_parent_with_lowest_snv(sn_list,selected,selected_sn);
						Supernode merged=merge_supernodes(parent,selected_sn);
						sn_list.remove(parent);
						sn_list.remove(selected_sn);
						sn_list.add(merged);
					}

				}
				//System.exit(0);
				/*System.out.println("Num: "+test_ex_no);
				for(String tmp: selected)
				{
					System.out.println(tmp);
				}
				System.out.println("--------------------------");*/
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

	public Supernode merge_supernodes(Supernode s1,Supernode s2)
	{
		ArrayList<String> tmp_node_list=new ArrayList<String>();
		// add nodes from both supernodes  into a new temporary list
		for(String stmp : s1.nodes)
		{
			tmp_node_list.add(stmp);
		}
		for(String stmp : s2.nodes)
		{
			tmp_node_list.add(stmp);
		}
		// update parents : parents of s1 +parents of s2 - those parents of s1 who were in s2 and vice versa 
		ArrayList<String> tmp_parent_list=new ArrayList<String>();
		for(String stmp : s1.parents_list)
		{
			if(!tmp_node_list.contains(stmp))
			{
				tmp_parent_list.add(stmp);
			}
		}
		for(String stmp : s2.parents_list)
		{
			if(!tmp_node_list.contains(stmp))
			{
				tmp_parent_list.add(stmp);
			}
		}
		// ------------------
		double new_snv=(s1.snv*s1.nodes.size()+s2.snv*s2.nodes.size())/(s1.nodes.size()+s2.nodes.size());
		Supernode tmp=new Supernode(tmp_node_list,new_snv,tmp_parent_list);
		return tmp;
	}

	public Supernode find_parent_with_lowest_snv(ArrayList<Supernode> sn_list,ArrayList<String> selected,Supernode current_sn)
	{
		ArrayList<Supernode> candidates=new ArrayList<Supernode>();
		// find all unassigned parents 
		for(String parent : current_sn.parents_list)
		{
			if(!selected.contains(parent))
			{
				loop: for(Supernode parent_sn : sn_list)
				{
					if(parent_sn.nodes.contains(parent))
					{
						candidates.add(parent_sn);
						break loop;
					}
				}
			}
		}
		// ------ find minimum

		Supernode selected_sn=candidates.get(0);
		int selected_sn_idx=0;
		double min_snv=candidates.get(0).snv;
		for(Supernode tmp : candidates)
		{
			if(tmp.snv<min_snv)
			{
				selected_sn=tmp;
				selected_sn_idx=sn_list.indexOf(tmp);
				min_snv=tmp.snv;
			}
		}

		// ----------------------

		return selected_sn;

	}

	public boolean all_parents_selected(ArrayList<String> selected,ArrayList<String> parents)
	{
		if(parents.size()!=0)
		{
			for(String parent: parents)
			{
				if(!selected.contains(parent))
				{
					return false;
				}
			}
		}
		return true;

	}

	public ArrayList<Supernode> create_supernodes(ArrayList<String> sn_list,double[] w,ArrayList<String> vertex)
	{
		ArrayList<Supernode> temp=new ArrayList<Supernode>();
		if(vertex.size()!=w.length)
		{
			System.out.println("Dimensions mis-match");
			System.exit(1);
		}
		// create a supernode for each node
		for(String tmp : sn_list)
		{
			// add all the immediate parents as parents of the supernode
			ArrayList<String> parents=new ArrayList<String>();
			OWLClass vertex_c = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+tmp));
			NodeSet<OWLClass> set=reasoner.getSuperClasses(vertex_c, true);
			for(Node<OWLClass> cls : set)
			{
				if(!cls.isTopNode())
				{
					String news=cls+"";
					String[] part=news.split(" ");
					part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
					parents.add(part[1]);
				}
			}
			Supernode tmp_sn=new Supernode(tmp,w[vertex.indexOf(tmp)],parents);
			temp.add(tmp_sn);
		}
		return temp;
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