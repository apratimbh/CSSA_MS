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

public class cssa_ms_new {
	public double[][] result=null,flabels=null,test_data=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;


	public static void main(String[] args) throws OWLOntologyCreationException
	{
		cssa_ms_new o=new cssa_ms_new();
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
				ArrayList<String> considered=new ArrayList<String>();
				// questionable nodes
				ArrayList<Supernode> questionable=new ArrayList<Supernode>();
				// selected nodes
				ArrayList<String> selected=new ArrayList<String>();
				// selected ms nodes
				ArrayList<String> ms_list=new ArrayList<String>();
				// questionable node- parent map
				HashMap<Supernode,Supernode> questionable_supernode_parent=new HashMap<Supernode,Supernode>();
				HashMap<Supernode,String> questionable_ms_parent=new HashMap<Supernode,String>();

				// -------------

				// ------ CSSA

				int k=0;
				// k1 keeps track of the number of ms labels (nodes) selected so far
				while(!all_nodes_considered(vertex,considered))
				{	

					// ----- select supernode with max snv
					Supernode selected_sn=sn_list.get(0);
					int selected_sn_idx=0;
					double max_snv=sn_list.get(0).snv;
					// selected_sn is the supernode with the highest snv
					//System.out.println("Supernode list length: "+sn_list.size()+" Questionale length: "+questionable.size());
					//System.out.println("Stuck!");
					for(Supernode tmp : sn_list)
					{
						if(tmp.snv>max_snv)
						{
							selected_sn=tmp;
							selected_sn_idx=sn_list.indexOf(tmp);
							max_snv=tmp.snv;
						}
					}
					// add all supernodes to the questionable list
					if(all_parents_considered(considered,selected_sn.parents_list))
					{
						/*String parent_ms=child_of_ms(selected_sn,ms_list);
						// if child of a previously selected ms node
						questionable_node_parent.put(selected_sn, parent_ms);*/
						Supernode parent=find_parent_supernode(questionable,selected_sn);
						if(parent!=null)
							questionable_supernode_parent.put(selected_sn, parent);
						sn_list.remove(selected_sn);
						questionable.add(selected_sn);
						for(String s : selected_sn.nodes)
						{
							considered.add(s);
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
				//System.out.println("number of questionable nodes: "+questionable.size());
				loop1: while(k<current)
				{
					Split current_best=null;
					double max_split=0;
					loop2: for(Supernode current_sn : questionable)
					{
						String parent_ms=child_of_ms(current_sn,ms_list);
						if(parent_ms!=null&&current_sn.ms_list.size()==1)
						{
							questionable_ms_parent.put(current_sn, parent_ms);
							Supernode sibling=search_siblings(current_sn,questionable_ms_parent,questionable);
							if(sibling==null)
							{
								break loop2;
							}
							ArrayList<Supernode> all_supernodes_in_split=get_all_supernodes_in_split(current_sn,sibling,questionable_supernode_parent);
							ArrayList<String> all_nodes_in_split=get_all_nodes_in_split(all_supernodes_in_split);
							double nsnv=new_snv(all_nodes_in_split,vertex,weights);
							if(nsnv>max_split)
							{
								max_split=nsnv;
								ArrayList<String> new_ms_list=get_new_ms_nodes(current_sn.ms_list,sibling.ms_list);
								current_best=new Split(all_nodes_in_split,vertex,new_ms_list,parent_ms,weights,all_supernodes_in_split);
							}
						}
						else
						{
							ArrayList<Supernode> all_supernodes_in_split=get_all_supernodes_in_split(current_sn,null,questionable_supernode_parent);
							ArrayList<String> all_nodes_in_split=get_all_nodes_in_split(all_supernodes_in_split);
							double nsnv=new_snv(all_nodes_in_split,vertex,weights);
							if(nsnv>max_split)
							{
								max_split=nsnv;
								current_best=new Split(all_nodes_in_split,vertex,current_sn.ms_list,parent_ms,weights,all_supernodes_in_split);
							}
						}
					}
					if(current_best!=null)
					{
						for(String node: current_best.nodes)
						{
							selected.add(node);
						}
						if(current_best.parent!=null)
							ms_list.remove(current_best.parent);
						for(String ms: current_best.ms)
						{
							ms_list.add(ms);
							k++;
						}
						for(Supernode sn: current_best.cointained_supernodes)
						{
							questionable.remove(sn);
						}
					}
					else
					{
						break loop1;
					}
				}
				/*System.out.println("List of all questioable nodes: ");
				for(Supernode s: questionable)
				{
					s.print();
				}
				System.exit(0);*/
				/*System.out.println("Num: "+test_ex_no);
				for(String tmp: selected)
				{
					System.out.println(tmp);
				}
				System.out.println("--------------------------");*/

				// -- find splits



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

	public double new_snv(ArrayList<String> nodes,ArrayList<String> vertex,double[] weights)
	{
		double avg=0;
		for(String tmp : nodes)
		{
			avg+=weights[vertex.indexOf(tmp)];
		}
		return (double)avg/nodes.size();
	}

	public ArrayList<String> get_all_nodes_in_split(ArrayList<Supernode> sn_list)
	{
		ArrayList<String> nodes=new ArrayList<String>();
		for(Supernode sn : sn_list)
		{
			for(String stmp : sn.nodes)
			{
				if(!nodes.contains(stmp))
				{
					nodes.add(stmp);
				}
			}
		}
		return nodes;
	}

	public ArrayList<Supernode> get_all_supernodes_in_split(Supernode s1,Supernode s2,HashMap<Supernode,Supernode> map)
	{
		ArrayList<Supernode> parent_list1=get_parent_supernodes(s1,map);
		ArrayList<Supernode> parent_list2=null;
		if(s2!=null)
		{
			parent_list2=get_parent_supernodes(s2,map);
		}
		ArrayList<Supernode> tmp_parent_list=new ArrayList<Supernode>();
		for(Supernode stmp : parent_list1)
		{
			if(!tmp_parent_list.contains(stmp))
			{
				tmp_parent_list.add(stmp);
			}
		}
		if(s2!=null)
		{
			for(Supernode stmp : parent_list2)
			{
				if(!tmp_parent_list.contains(stmp))
				{
					tmp_parent_list.add(stmp);
				}
			}
		}
		if(!tmp_parent_list.contains(s1))
			tmp_parent_list.add(s1);
		if(s2!=null&&!tmp_parent_list.contains(s2))
			tmp_parent_list.add(s2);
		return tmp_parent_list;
	}

	public ArrayList<Supernode> get_parent_supernodes(Supernode s,HashMap<Supernode,Supernode> map)
	{
		ArrayList<Supernode> tmp=new ArrayList<Supernode>();
		Supernode parent=map.get(s);
		while(parent!=null)
		{
			tmp.add(parent);
			parent=map.get(parent);
		}
		return  tmp;
	}

	public boolean all_nodes_considered(ArrayList<String> vertex,ArrayList<String> considered)
	{
		if(considered.size()<vertex.size())
		{
			return false;
		}
		else if(considered.size()==vertex.size())
		{
			return true;
		}
		else if(considered.size()>vertex.size())
		{
			System.out.println("ERROR");
			return true;
		}
		else
		{
			return false;
		}
	}

	public Supernode search_siblings(Supernode s,HashMap<Supernode,String> map,ArrayList<Supernode> questionable)
	{
		String parent=map.get(s);
		double max_snv=0;
		Supernode best_sibling=null;
		for(Supernode sn : questionable)
		{
			if(sn!=s&&(map.get(sn)==parent))
			{
				if(sn.snv>max_snv)
				{
					max_snv=sn.snv;
					best_sibling=sn;
				}
			}
		}
		return best_sibling;
	}

	public String child_of_ms(Supernode s1,ArrayList<String> ms)
	{
		String tmp=null;
		int count=0;
		for(String supernode_ms_node: s1.ms_list)
		{
			for(String selected_ms_node: ms)
			{
				OWLClass c1=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+selected_ms_node));
				OWLClass c2=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+supernode_ms_node));
				OWLAxiom axiom=factory.getOWLSubClassOfAxiom(c2, c1);
				if(reasoner.isEntailed(axiom)) 
				{
					tmp=selected_ms_node;
					count++;
					if(count>1)
					{
						System.out.println("ERROR");
						System.exit(0);
					}
				}
			}
		}
		return tmp;
	}

	public Supernode merge_supernodes(Supernode s1,Supernode s2)
	{
		//System.out.println("Stuck!");
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
		ArrayList<String> new_ms_list=get_new_ms_nodes(s1.ms_list,s2.ms_list);
		Supernode tmp=new Supernode(tmp_node_list,new_snv,tmp_parent_list,new_ms_list);
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

	public Supernode find_parent_supernode(ArrayList<Supernode> questionable,Supernode s)
	{
		// the supernode can have only one parent
		if(questionable.size()>0&&s.parents_list.size()>0)
		{
			String parent=s.parents_list.get(0);
			for(Supernode qs: questionable)
			{
				if(qs.nodes.contains(parent))
				{
					return qs;
				}
			}
		}
		return null;
	}

	public boolean all_parents_considered(ArrayList<String> selected,ArrayList<String> parents)
	{
		if(parents.size()>2)
			System.out.println("Number of parents: "+parents.size());
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

	public ArrayList<String> get_new_ms_nodes(ArrayList<String> ms_l1,ArrayList<String> ms_l2)
	{
		ArrayList<String> new_ms_list=new ArrayList<String>();
		ArrayList<String> not_ms=new ArrayList<String>();
		// add all ms nodes to a temp list
		for(String s: ms_l1)
		{
			new_ms_list.add(s);
		}
		for(String s: ms_l2)
		{
			new_ms_list.add(s);
		}

		// check if one ms is a parent of the other
		for(String n1 : new_ms_list)
		{

			OWLClass c1=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+n1));
			loop: for(String n2 : new_ms_list)
			{
				if(n1!=n2)
				{
					OWLClass c2=factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+n2));
					OWLAxiom axiom2=factory.getOWLSubClassOfAxiom(c2,c1);
					if(reasoner.isEntailed(axiom2))
					{
						not_ms.add(n1);
						break loop;
					}
				}
			}
		}
		for(String s: not_ms)
		{
			new_ms_list.remove(s);
		}
		return new_ms_list; 
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