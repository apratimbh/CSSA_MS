package funcat;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
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

import pr_curve.curve;
import pr_curve.curve_point;

public class aims_fast {
	public double[][] result=null,flabels=null,test_data=null;
	public double[][] pr_val=new double[10][3];
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;




	public curve main(String result_file,String expanded_test_file,String ontology_file,ArrayList<String> vertex,int columns,int limit) throws OWLOntologyCreationException
	{
		result=read_file(result_file);
		//result=read_file("fun_cellcycle_result.txt");
		manager = OWLManager.createOWLOntologyManager();
		go = manager.loadOntologyFromOntologyDocument(new File(ontology_file));
		System.out.println("Loaded ontology: " + go.getOntologyID().toString());
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		reasoner = reasonerFactory.createReasoner(go, config);
		factory = manager.getOWLDataFactory();

		test_data=load_test_data(expanded_test_file,columns);



		curve prc=new curve();

		ArrayList<pr_store> pr_store_list=create_pr_store(vertex.size()+1);
		int prev=0;

		HashMap<String,String> node_parent=create_parent_map(vertex);

		HashMap<String,Integer> vertex_index=new HashMap<String,Integer>();
		for(int i=0;i<vertex.size();i++)
		{
			vertex_index.put(vertex.get(i), i);
		}
		/*ArrayList<String> tmp_selected=new ArrayList<String>();
		tmp_selected.add("root");
		ArrayList<String> all_nodes_in_splitt=get_all_nodes_in_split("14","10.03",node_parent,tmp_selected);
		//System.out.println(node_parent.get("10.03"));
		for(String s: all_nodes_in_splitt)
		{
			System.out.println(s);
		}
		System.exit(0);*/
		main_loop: for(int test_ex_no=1;test_ex_no<result[0].length;test_ex_no++)
		{

			int curr_done=(int)(((double)test_ex_no/result[0].length)*100);
			if(curr_done!=prev)
			{
				System.out.println(curr_done+"%");
				prev=curr_done;
			}

			int[] test_data_this_ex=new int[test_data[test_ex_no].length];
			for(int l=0;l<test_data[test_ex_no].length;l++)
			{
				test_data_this_ex[l]=(int) test_data[test_ex_no][l];
			}
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
			// AIMS

			// list of supernodes (each supernode contains one node and snv is the weight of that node)
			ArrayList<String> questionable=new ArrayList<String>();
			ArrayList<String> ms_list=new ArrayList<String>();
			ArrayList<String> selected=new ArrayList<String>();

			int[] questionable_arr=new int[vertex.size()];
			int[] selected_arr=new int[vertex.size()];
			int[] ms_list_arr=new int[vertex.size()];
			// questionable node- parent map

			for(String s: vertex)
			{
				questionable.add(s);
				questionable_arr[vertex_index.get(s)]=1;
			}

			// -------------

			// ------ AIMS

			int k=0;
			selected.add("root");
			selected_arr[vertex_index.get("root")]=1;
			ms_list.add("root");
			ms_list_arr[vertex_index.get("root")]=1;
			questionable.remove("root");
			questionable_arr[vertex_index.get("root")]=0;
			k++;
			double curr_avg=1;
			loop1: while(k<limit)
			{
				Split curr_best_split=null;
				loop2: for(String node : questionable)
				{
					String parent_ms=child_of_ms(node,ms_list);
					//System.out.println("node: "+node+" parent-ms: "+parent_ms);
					if(parent_ms!=null)
					{
						String sb=search_siblings(node,questionable,ms_list,selected,node_parent,curr_avg,vertex,weights,selected_arr,vertex_index);
						if(sb!=null)
						{
							if(is_subclass(node,sb)||is_subclass(sb,node))
								System.out.println("node: "+node+" sibling: "+sb);
							ArrayList<String> all_nodes_in_split=get_all_nodes_in_split(node,sb,node_parent,selected_arr, vertex_index);
							double nsnv=(double)(new_total_weight(all_nodes_in_split,vertex,weights)+curr_avg*selected.size())/(selected.size()+all_nodes_in_split.size());
							ArrayList<String> tmp_ms_list=new ArrayList<String>();
							tmp_ms_list.add(node);
							tmp_ms_list.add(sb);
							if(curr_best_split==null||curr_best_split.new_curr_best<nsnv)
							{
								curr_best_split=new Split(all_nodes_in_split,tmp_ms_list,parent_ms,nsnv);
							}
						}
					}
					else
					{
						ArrayList<String> all_nodes_in_split=get_all_nodes_in_split(node,null,node_parent,selected_arr, vertex_index);
						double nsnv=(double)(new_total_weight(all_nodes_in_split,vertex,weights)+curr_avg*selected.size())/(selected.size()+all_nodes_in_split.size());
						ArrayList<String> tmp_ms_list=new ArrayList<String>();
						tmp_ms_list.add(node);
						if(curr_best_split==null||curr_best_split.new_curr_best<nsnv)
						{
							curr_best_split=new Split(all_nodes_in_split,tmp_ms_list,parent_ms,nsnv);
						}
					}
				}
				if(curr_best_split!=null)
				{
					curr_avg=curr_best_split.new_curr_best;
					for(String node: curr_best_split.nodes)
					{
						if(!selected.contains(node))
							selected.add(node);
						selected_arr[vertex_index.get(node)]=1;
					}
					if(curr_best_split.parent!=null&&ms_list.contains(curr_best_split.parent))
					{
						ms_list.remove(curr_best_split.parent);
						k--;
					}
					for(String ms: curr_best_split.ms)
					{
						ms_list.add(ms);
						k++;
					}
					/*System.out.println("ms-nodes: ");
					for(String ms: ms_list)
					{
						System.out.print(ms+" , ");
					}
					System.out.println();
					System.out.println("K is - "+k);*/
					//System.out.println("Q size is - "+questionable.size());
					update_pr(selected.size(),test_ex_no,pr_store_list,selected,vertex,test_data_this_ex);
					if(selected.size()==(vertex.size()-1))
					{
						continue main_loop;
					}
					for(String sn: curr_best_split.nodes)
					{
						questionable.remove(sn);
					}
				}
				else
				{
					break loop1;
				}
			}

			// --for loop end // all examples
		}
		for(int k=0;k<pr_store_list.size();k++)
		{
			//pr_store_list.get(k).tp-=result[0].length;
			double tp=pr_store_list.get(k).tp;
			double fp=pr_store_list.get(k).fp;
			double fn=pr_store_list.get(k).fn;
			curve_point pt=new curve_point(tp/(tp+fp),tp/(tp+fn));
			prc.add(pt);
		}
		return prc;
	}

	public HashMap<String,String> create_parent_map(ArrayList<String> vertex)
	{
		HashMap<String,String> map=new HashMap<String,String>();
		for(String s: vertex)
		{
			if(s.charAt(0)=='r')
			{
				//
			}
			else if(s.charAt(0)!='r'&&s.length()<3)
			{
				map.put(s, "root");
			}
			else if(s.charAt(0)!='r'&&s.length()>3)
			{
				String parent=s.substring(0,s.length()-3);
				//System.out.println(s+" - "+parent);
				map.put(s, parent);
			}
		}
		return map;
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

	public boolean is_subclass(String c1,String c2)
	{
		if(c2.contains("root"))
		{
			return true;
		}
		else if(c1.length()<=c2.length())
		{
			return false;
		}
		else
		{
			for(int i=0;i<c2.length()&&i<c1.length();i++)
			{
				if(c1.charAt(i)!=c2.charAt(i))
				{
					return false;
				}
			}
			if(c1.charAt(c2.length())=='.')
			{
				return true;
			}
		}
		return false;
	}

	public void update_pr(int k,int test_ex_no,ArrayList<pr_store> pr_store_list,ArrayList<String> selected,ArrayList<String> vertex,int[] test_data_this_ex)
	{
		try {
			for(String s : selected)
			{
				if(test_data_this_ex[vertex.indexOf(s)]==1)
				{
					pr_store_list.get(k).tp++;
				}
				else
				{
					pr_store_list.get(k).fp++;
				}
			}
			for(int l=0;l<test_data[test_ex_no].length;l++)
			{
				if(test_data_this_ex[l]==1)
				{
					if(!selected.contains(vertex.get(l)))
					{
						pr_store_list.get(k).fn++;
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("Error at k="+k);
			e.printStackTrace();
		}
	}

	public double new_total_weight(ArrayList<String> nodes,ArrayList<String> vertex,double[] weights)
	{
		double avg=0;
		for(String tmp : nodes)
		{
			try {
				avg+=weights[vertex.indexOf(tmp)];
			}
			catch(Exception e)
			{
				System.out.println("Node -- "+tmp);
				System.exit(0);
			}
		}
		return avg;
	}

	public ArrayList<String> get_all_nodes_in_split(String s1,String s2,HashMap<String,String> map,int[] selected_arr,HashMap<String,Integer> vertex_index)
	{
		ArrayList<String> nodes=new ArrayList<String>();
		int[] nodes_arr=new int[selected_arr.length];
		String parent=s1;
		nodes.add(s1);
		nodes_arr[vertex_index.get(s1)]=1;
		while(parent!=null)
		{
			try {
				parent=map.get(parent);
				//System.out.println("1- parent - "+parent);
				if(parent!=null&&nodes_arr[vertex_index.get(parent)]==0&&selected_arr[vertex_index.get(parent)]==0)
				{
					//System.out.println("parent - "+parent);
					nodes.add(parent);
					nodes_arr[vertex_index.get(parent)]=1;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.out.print("s1: "+s1+" Null - "+parent);
				System.exit(0);
			}
		}
		if(s2!=null)
		{
			parent=s2;
			nodes.add(s2);
			nodes_arr[vertex_index.get(s2)]=1;
			while(parent!=null)
			{
				parent=map.get(parent);
				//System.out.println("2- parent - "+parent);
				if(parent!=null&&nodes_arr[vertex_index.get(parent)]==0&&selected_arr[vertex_index.get(parent)]==0)
				{
					//System.out.println("parent - "+parent);
					nodes.add(parent);
					nodes_arr[vertex_index.get(parent)]=1;
				}
			}
		}
		return nodes;
	}


	public String search_siblings(String s,ArrayList<String> questionable,ArrayList<String> ms_list,ArrayList<String> selected,HashMap<String,String> map,double curr_avg,ArrayList<String> vertex,double[] weights,int[] selected_arr,HashMap<String,Integer> vertex_index)
	{
		String parent=child_of_ms(s,ms_list);
		double max_snv=0;
		String best_sibling=null;
		for(String sb : questionable)
		{
			if(sb!=s&&(child_of_ms(sb,ms_list)==parent)&&!is_subclass(sb,s)&&!is_subclass(s,sb))
			{
				ArrayList<String> all_nodes_in_split=get_all_nodes_in_split(s,sb,map,selected_arr,vertex_index);
				double nsnv=(double)(new_total_weight(all_nodes_in_split,vertex,weights)+curr_avg*selected.size())/(selected.size()+all_nodes_in_split.size());
				if(nsnv>max_snv)
				{
					max_snv=nsnv;
					best_sibling=sb;
				}
			}
		}
		return best_sibling;
	}

	public String child_of_ms(String s,ArrayList<String> ms)
	{
		String tmp=null;
		int count=0;

		for(String selected_ms_node: ms)
		{
			if(is_subclass(s,selected_ms_node))
			{
				count++;
				if(count>1&&(tmp!=selected_ms_node))
				{
					System.out.println("node: "+s);
					System.out.println("Ms-nodes: "+tmp+" -- "+selected_ms_node);
					System.out.println("----ERROR");
					System.exit(0);
				}
				tmp=selected_ms_node;
			}
		}
		return tmp;
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
