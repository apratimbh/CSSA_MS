package master;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
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
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import pr_curve.curve;
import pr_curve.curve_point;
import funcat.aims;
import funcat.aims_fast;
import funcat.aims_faster;
import funcat.aims_ms;
import funcat.aims_selected;
import funcat.cssa;
import funcat.cssa2;
import funcat.cssa_fast;
import funcat.cssa_ms_fast;
import funcat.cssa_ms_new;
import funcat.inde_set_fast;
import funcat.inde_set_ms;
import funcat.inde_set_new;
import funcat.inde_set_selected;

public class master 
{
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;

	public void create_new_ontology(String inputfile,String ontology_name,String ontology_file_name) throws OWLOntologyCreationException, IOException
	{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		BufferedReader br=null;
		IRI ontologyIRI = IRI.create("http://www.co-ode.org/ontologies/"+ontology_name+".owl");
		IRI documentIRI = IRI.create("file:/"+ontology_file_name);
		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, documentIRI);
		manager.addIRIMapper(mapper);
		OWLOntology ontology = manager.createOntology(ontologyIRI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);
		OWLClass g1,g2,root;


		try {
			br = new BufferedReader(new FileReader(inputfile));
			String line;
			root=factory.getOWLClass(IRI.create(ontologyIRI + "#root"));
			while((line = br.readLine())!=null)
			{
				if(line.contains("hierarchical"))
				{
					line=line.replaceAll(" +", " ");
					line=line.split(" ")[3];
					String[] part=line.split(",");
					for(String gene : part)
					{

						if(gene.contains("/"))
						{
							gene=gene.replaceAll("/",	 ".");
							String top=get_parent(gene);
							String bottom=gene;
							g1=factory.getOWLClass(IRI.create(ontologyIRI + "#"+top));
							g2=factory.getOWLClass(IRI.create(ontologyIRI + "#"+bottom));
						}
						else
						{
							g1=root;
							g2=factory.getOWLClass(IRI.create(ontologyIRI + "#"+gene));
						}
						OWLAxiom axiom = factory.getOWLSubClassOfAxiom(g2, g1);
						AddAxiom addAxiom = new AddAxiom(ontology, axiom);
						manager.applyChange(addAxiom);
					}
					break;
				}
			}
			manager.saveOntology(ontology);
		} catch (Exception e) {
			System.out.println("Exception: "+(e.getMessage()));
			e.printStackTrace();
		}
	}

	public void create_ontology_vector(ArrayList<String> vertex,String ontology_name) throws IOException
	{
		BufferedWriter bw=new BufferedWriter(new FileWriter("E:\\CSSA\\"+ontology_name+".tree"));
		for(String s:vertex)
		{
			if(s.equals("root"))
			{
				bw.write("-1\n");
				System.out.println("Here: root> "+s);
			}
			else if(s!="root"&&s.contains("."))
			{
				bw.write((vertex.indexOf(get_parent(s))+1)+"\n");
			}
			else if(s!="root"&&!s.contains("."))
			{
				bw.write((vertex.indexOf("root")+1)+"\n");
			}
			else
			{
				bw.write("-1\n");
				System.out.println("Here: root> "+s);
			}
		}
		bw.close();
	}

	public int get_number_of_attr(String filename)
	{
		BufferedReader br = null;
		int c=0;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line;
			String data="@DATA";
			String attr="@ATTRIBUTE";
			loop: while((line = br.readLine())!=null)
			{
				if(line.contains(attr)||line.contains(attr.toLowerCase()))
				{
					c++;
				}
				if(line.contains(data)||line.contains(data.toLowerCase()))
				{
					break loop;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return (c-1);
	}

	public String get_parent(String s)
	{
		if(s!="root"&&s.contains("."))
			return s.substring(0,s.lastIndexOf('.'));
		else
			return "root";
	}

	public ArrayList<String> create_vertex_list()
	{
		go.getClassesInSignature();
		ArrayList<String> vertex=new ArrayList<String>();
		Set<OWLClass> ce=go.getClassesInSignature();
		System.out.println("Printing nodes: ");
		for(OWLClass c : ce)
		{
			//System.out.println(c.toString());
			String part=c.toString();
			part=part.replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
			vertex.add(part);
			//System.out.println(part);
			//System.out.println(c.asOWLClass().getIRI().toString());
			/*OWLClassExpression cx=c.asOWLClass();
			NodeSet<OWLClass> set=reasoner.getSubClasses(cx, false);
			for (Node<OWLClass> ind : set)
			{
				if(!ind.isBottomNode())
				{
					String news=ind+"";
					//System.out.println(news);
					String[] part=news.split(" ");
					part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
					vertex.add(part[1]);
					System.out.println(part[1]);
				}
			}*/
		}
		vertex.remove(vertex.indexOf("root"));
		vertex.add(0,"root");
		for(String s:vertex)
		{
			//System.out.println(s);
		}
		//System.exit(0);
		return vertex;
	}

	public void create_fv_arr(String outputfile,ArrayList<String> vertex)
	{
		//System.out.println("Creating kernel file----");
		double[][] fv_arr=new double[vertex.size()][vertex.size()];
		//System.out.println(".");
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
			NodeSet<OWLClass> set=reasoner.getSuperClasses(t_cls, false);
			//System.out.println("\nLabel: "+temp+"\tSuperClasses:");
			for(Node<OWLClass> cls : set)
			{
				if(!cls.isTopNode())
				{
					String news=cls+"";
					news=news.split(" ")[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
					//System.out.println("\t\t\t"+news);
					if(vertex.contains(news))
					{
						int i=vertex.indexOf(news);
						int j=vertex.indexOf(temp);
						if(1>vec.get(vertex.indexOf(news)))
						{
							vec.set(vertex.indexOf(news),new Double(1));
						}
					}
				}
			}
			int c=0;
			for(double d : vec)
			{
				fv_arr[vertex.indexOf(temp)][c]=d;
				c++;
			}
			p++;
		}
		System.out.println("100%\n");
		PrintWriter pr;
		try {
			pr = new PrintWriter(outputfile);
			for (int i=0; i<fv_arr.length ; i++)
			{
				String temp=Arrays.toString(fv_arr[i]);
				pr.println(temp.substring(1, temp.length()-1).replaceAll(",", ""));
			}
			pr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}    
	}

	public int get_index_cat(String in)
	{
		int idx=-1;
		for(int i=0;i<in.length();i++)
		{
			if(Character.isAlphabetic(in.charAt(i)))
			{
				if(idx<0)
					idx=i;
				else
				{
					System.out.println("ERROR: multiple cat "+in.charAt(i));
					System.out.println(in);
					if(in.charAt(i-1)=='-'&&Character.isAlphabetic(in.charAt(i-2)))
					{
						idx=-2;
					}
					//System.exit(0);
				}
			}
		}
		return idx;
	}

	public String convert_cat_data_amg(String in)
	{
		int idx=-1;
		for(int i=0;i<in.length();i++)
		{
			if(Character.isAlphabetic(in.charAt(i)))
			{
				if(idx<0)
					idx=i;
				else
				{
					System.out.println("Repaing multiple cat");
					System.out.println(in);
					if(in.charAt(i-1)=='-'&&Character.isAlphabetic(in.charAt(i-2)))
					{
						in=in.substring(0,i-2)+"0"+in.substring(i+1);
					}
					//System.exit(0);
				}
			}
		}
		return in;
	}

	public String convert_cat_data(String in,HashMap<Character,Integer> map,int idx)
	{
		String out="";
		if(!map.containsKey(in.charAt(idx)))
		{
			System.out.println("ERROR");
			System.exit(0);
		}
		int c=map.get(in.charAt(idx));
		out =in.substring(0,idx)+c+in.substring(idx+1);
		return out;
	}

	public void convert_arff_file(ArrayList<String> vertex,String inputfile,String outputfile,String onto_name)
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(inputfile));
			bw = new BufferedWriter(new FileWriter(outputfile));
			String line;
			int c=1;
			boolean flag=true;
			ArrayList<String> predicted_classes = new ArrayList<String>(vertex);
			l1: while((line = br.readLine())!=null)
			{
				if(!line.isEmpty())
				{
					if(flag)
					{
						if(!line.contains("hierarchical"))
						{
							//
						}
						else
							flag=false;
					}
					else
					{
						break l1;
					}
				}
			}
			String[] gene=null;
			String out="";
			String label_vec="";
			//bw.write("\n@data\n\n");
			bw.flush();
			c=0;
			int cat_count=0;
			HashMap<Character,Integer> cat_map=new HashMap<Character,Integer>();
			boolean flag1=onto_name.contains("expr")||onto_name.contains("spo");
			boolean flag2=onto_name.contains("pheno");
			while((line = br.readLine())!=null)
			{
				if(!line.isEmpty()&&!line.contains("data")&&!line.contains("DATA"))
				{
					//System.out.println(line+"\n");
					//System.out.println("\n--------------------\nTaining example: "+(++c)+"\n-----------------");
					out="";
					label_vec="";
					if(line.contains("mit"))
					{
						line=line.replaceAll("mit", "17");
					}
					if(flag1)
					{
						line=line.replaceAll("no", "0");
						line=line.replaceAll("yes", "0");
					}
					if(flag2)
					{
						line=line.replaceAll("w", "1");
						line=line.replaceAll("n", "2");
						line=line.replaceAll("s", "3");
						line=line.replaceAll("r", "4");
					}
					line=line.replaceAll("\\?", "-99999999");
					String[] part=line.split(",");
					String gene_l=part[part.length-1];
					out=line.substring(0,line.length()-1-gene_l.length());
					int idx=get_index_cat(out);
					if(idx>=0)
					{
						if(cat_map.containsKey(out.charAt(idx)))
						{
							out=convert_cat_data(out,cat_map,idx);
						}
						else
						{
							cat_count++;
							cat_map.put(out.charAt(idx), cat_count);
							out=convert_cat_data(out,cat_map,idx);
						}
					}
					if(idx==-2)
					{
						out=convert_cat_data_amg(out);
					}
					//System.out.println(gene_l);
					gene=gene_l.replace(".", "").split("@");
					ArrayList<Double> vec=new ArrayList<Double>();
					for(String temp : predicted_classes)
					{
						vec.add(new Double(0));
					}
					boolean f=false;
					int x=0;
					for(String temp : gene)
					{
						temp=temp.replaceAll("/",".");
						boolean fflag=true;
						//System.out.println("Temp> "+gene.length);
						int y=predicted_classes.indexOf(temp);
						//System.out.println(""+temp);
						vec.set(predicted_classes.indexOf(temp),new Double(1));
					}
					label_vec=",";
					//System.out.println();
					for(double d : vec)
					{
						label_vec+=(int)d+",";
					}
					//System.out.println("-----------");
					vec.clear();
					label_vec=label_vec.substring(0,label_vec.length()-1);
					//System.out.println(label_vec);
					bw.write(out+label_vec+"\n");
					bw.flush();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void convert_arff_file_expand(ArrayList<String> vertex,String inputfile,String outputfile,String onto_name)
	{
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new FileReader(inputfile));
			bw = new BufferedWriter(new FileWriter(outputfile));
			String line;
			int c=1;
			boolean flag=true;
			ArrayList<String> predicted_classes = new ArrayList<String>(vertex);
			l1: while((line = br.readLine())!=null)
			{
				if(!line.isEmpty())
				{
					if(flag)
					{
						if(!line.contains("hierarchical"))
						{
							//
						}
						else
							flag=false;
					}
					else
					{
						break l1;
					}
				}
			}
			String[] gene=null;
			String out="";
			String label_vec="";
			//bw.write("\n@data\n\n");
			bw.flush();
			c=0;
			int cat_count=0;
			boolean flag1=onto_name.contains("expr")||onto_name.contains("spo");
			boolean flag2=onto_name.contains("pheno");
			HashMap<Character,Integer> cat_map=new HashMap<Character,Integer>();
			while((line = br.readLine())!=null)
			{
				if(!line.isEmpty()&&!line.contains("data")&&!line.contains("DATA"))
				{
					//System.out.println(line+"\n");
					//System.out.println("\n--------------------\nTaining example: "+(++c)+"\n-----------------");
					out="";
					label_vec="";
					if(line.contains("mit"))
					{
						line=line.replaceAll("mit", "17");
					}
					if(flag1)
					{
						line=line.replaceAll("no", "0");
						line=line.replaceAll("yes", "1");
					}
					if(flag2)
					{
						line=line.replaceAll("w", "1");
						line=line.replaceAll("n", "2");
						line=line.replaceAll("s", "3");
						line=line.replaceAll("r", "4");
					}
					line=line.replaceAll("\\?", "-99999999");
					String[] part=line.split(",");
					String gene_l=part[part.length-1];
					out=line.substring(0,line.length()-1-gene_l.length());
					int idx=get_index_cat(out);
					if(idx>=0)
					{
						if(cat_map.containsKey(out.charAt(idx)))
						{
							out=convert_cat_data(out,cat_map,idx);
						}
						else
						{
							cat_count++;
							cat_map.put(out.charAt(idx), cat_count);
							out=convert_cat_data(out,cat_map,idx);
						}
					}
					if(idx==-2)
					{
						out=convert_cat_data_amg(out);
					}
					//System.out.println(gene_l);
					gene=gene_l.replace(".", "").split("@");
					ArrayList<Double> vec=new ArrayList<Double>();
					for(String temp : predicted_classes)
					{
						vec.add(new Double(0));
					}
					boolean f=false;
					int x=0;
					for(String temp : gene)
					{
						temp=temp.replaceAll("/",".");
						boolean fflag=true;
						//System.out.println("Temp> "+gene.length);
						int y=predicted_classes.indexOf(temp);
						//System.out.println(""+temp);
						vec.set(predicted_classes.indexOf(temp),new Double(1));
						//OWLClass t_cls = factory.getOWLClass(IRI.create("http://purl.org/obo/owl/gene_ontology_edit#"+temp));
						OWLClass t_cls = factory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+temp));
						NodeSet<OWLClass> set=reasoner.getSuperClasses(t_cls, false);
						//System.out.println("\nLabel: "+temp+"\tSuperClasses:");
						for(Node<OWLClass> cls : set)
						{
							if(!cls.isTopNode())
							{
								String news=cls+"";
								news=news.split(" ")[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
								if(predicted_classes.contains(news))
								{
									int i=vertex.indexOf(news);
									int j=vertex.indexOf(temp);
									if(1>vec.get(predicted_classes.indexOf(news)))
									{
										int m1=vertex.indexOf(temp);
										int n1=vertex.indexOf(news);
										vec.set(predicted_classes.indexOf(news),new Double(1));
										//System.out.println("Super class: "+news+" index: "+predicted_classes.indexOf(news));
										fflag=false;
									}
								}
							}
						}
						//if(fflag)
						//System.out.println(temp);
					}
					label_vec=",";
					//System.out.println();
					for(double d : vec)
					{
						label_vec+=(int)d+",";
					}
					//System.out.println("-----------");
					vec.clear();
					label_vec=label_vec.substring(0,label_vec.length()-1);
					//System.out.println(label_vec);
					bw.write(out+label_vec+"\n");
					bw.flush();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void output_curve(curve cv,String outputfile)
	{
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputfile));
			for(curve_point p : cv.point_list)
			{
				System.out.println("p: "+p.precision+" r: "+p.recall);
				bw.write(p.precision+"\t"+p.recall+"\n");
			}
			bw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void merge_files(String input1,String input2,String output)
	{
		BufferedReader br1 = null;
		BufferedReader br2 = null;
		BufferedWriter bw = null;
		try {
			br1 = new BufferedReader(new FileReader(input1));
			br2 = new BufferedReader(new FileReader(input2));
			bw = new BufferedWriter(new FileWriter(output));
			String line;
			boolean flag=true;
			while((line = br1.readLine())!=null)
			{
				bw.write(line+"\n");
			}
			br1.close();
			while((line = br2.readLine())!=null)
			{
				bw.write(line+"\n");
			}
			br2.close();
			bw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void main() throws OWLOntologyCreationException, IOException
	{
		String[] onto_names={"cellcycle"/*"pheno","gasch1","gasch2","eisen","expr","derisi","spo","seq","cellcycle","church"*/};
		String matlab_folder="E:/test/";
		for(int i=0;i<onto_names.length;i++)
		{
			System.out.println("Computing; "+onto_names[i]);
			String matlab_folder1=matlab_folder+onto_names[i];
			String ontology_name=onto_names[i]; 
			String ontology_file_name=matlab_folder1+"/"+onto_names[i]+"_FUN.owl";
			String fv_arr_file=matlab_folder1+"/fv_arr_"+onto_names[i]+".txt";
			String train_file=matlab_folder1+"/"+onto_names[i]+"_FUN.train.arff";
			String valid_file=matlab_folder1+"/"+onto_names[i]+"_FUN.valid.arff";
			String test_file=matlab_folder1+"/"+onto_names[i]+"_FUN.test.arff";
			String converted_train_file=matlab_folder1+"/"+onto_names[i]+"_FUN.train.converted.arff";
			String converted_vaild_file=matlab_folder1+"/"+onto_names[i]+"_FUN.valid.converted.arff";
			String combined_train_file=matlab_folder1+"/"+onto_names[i]+"_FUN.train.combined.arff";
			String converted_test_file=matlab_folder1+"/"+onto_names[i]+"_FUN.test.converted.arff";
			String expanded_test_file=matlab_folder1+"/"+onto_names[i]+"_FUN.test.expanded.arff"; // required to check the results in cssa_ms_new.jav and inde_set_new.java
			String result_file=matlab_folder1+"/result1_"+onto_names[i]+".txt";
			int no_attr=get_number_of_attr(train_file);
			System.out.println("Number of attr: "+no_attr);

			System.out.println("Creating ontology file");
			create_new_ontology(matlab_folder1+"/"+onto_names[i]+"_FUN.train.arff", ontology_name,ontology_file_name);
			manager = OWLManager.createOWLOntologyManager();
			go = manager.loadOntologyFromOntologyDocument(new File(ontology_file_name));
			System.out.println("Loaded ontology: " + go.getOntologyID().toString());
			OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
			OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
			reasoner = reasonerFactory.createReasoner(go, config);
			factory = manager.getOWLDataFactory();

			ArrayList<String> vertex=create_vertex_list();

			//create_ontology_vector(vertex,onto_names[i]);
			
			File result=new File(result_file);
			if(!result.exists())
			{
				System.out.println("Creating kernel file");
				create_fv_arr(fv_arr_file,vertex);
				System.out.println("Creating training file");
				convert_arff_file_expand(vertex,train_file,converted_train_file,onto_names[i]);
				System.out.println("Creating validation file");
				convert_arff_file_expand(vertex,valid_file,converted_vaild_file,onto_names[i]);
				System.out.println("Creating test file");
				convert_arff_file(vertex,test_file,converted_test_file,onto_names[i]);
				System.out.println("Creating merged file");
				merge_files(converted_train_file,converted_vaild_file,combined_train_file);
				System.out.println("Creating expanded file");
				convert_arff_file_expand(vertex,test_file,expanded_test_file,onto_names[i]);

				//String[] cmd = { "matlab", "/r", "\"cd('"+matlab_folder+"');file1="+fv_arr_file+";\"" };
				//String[] cmd = { "matlab", "/r", "\"cd('"+matlab_folder+"')\"" };

				//String[] cmd = { "matlab", "/r", "\"cd('"+matlab_folder+"');cnum="+no_attr+";file1='"+fv_arr_file+"';file2='"+combined_train_file+"';file3='"+converted_test_file+"';file4='"+result_file+"';KPCA_final\"" };

				String[] cmd = { "matlab", "/r", "\"cd('"+matlab_folder+"');cnum="+no_attr+";file1='"+fv_arr_file+"';file2='"+combined_train_file+"';file3='"+converted_test_file+"';file4='"+result_file+"';KPCA_final\"" };
				Process p;
				try {
					p = Runtime.getRuntime().exec(cmd);
					p.waitFor();
				} catch (Exception e) {
					e.printStackTrace();
				}


				File result1=new File(result_file);
				while(!result1.exists())
				{
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//System.exit(0);
			
			cssa2 cs2=new cssa2();
			curve cssa2_curve=cs2.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr,200);
			output_curve(cssa2_curve,matlab_folder1+"/"+onto_names[i]+"_curve_cssa_nxt.txt");
			
			aims_selected ams=new aims_selected();
			curve am_curve=ams.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr, 100);
			output_curve(am_curve,matlab_folder1+"/"+onto_names[i]+"_curve_aims_selected_nxt.txt");
			
			/*cssa_fast cs=new cssa_fast();
			curve cssa_curve=cs.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr,100);
			output_curve(cssa_curve,matlab_folder1+"/"+onto_names[i]+"_curve_cssa.txt");

			aims_ms am=new aims_ms();
			curve am_curve=am.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr, 100);
			output_curve(am_curve,matlab_folder1+"/"+onto_names[i]+"_curve_aims_ms.txt");

			/*aims_selected ams=new aims_selected();
			am_curve=ams.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr, 100);
			output_curve(am_curve,matlab_folder1+"/"+onto_names[i]+"_curve_aims_selected.txt");*/

			/*inde_set_ms isms=new inde_set_ms();
			curve inde_curve_ms=isms.main(result_file, expanded_test_file, ontology_file_name, vertex, no_attr,100);
			output_curve(inde_curve_ms,matlab_folder1+"/"+onto_names[i]+"_curve_inde_fast_ms.txt");*/

			/*inde_set_selected issl=new inde_set_selected();
			curve inde_curve_sl=issl.main(result_file, expanded_test_file, ontology_file_name, vertex, no_attr,100);
			output_curve(inde_curve_sl,matlab_folder1+"/"+onto_names[i]+"_curve_inde_fast_selected.txt");*/

			/*File file4=new File(matlab_folder1+"/"+onto_names[i]+"_curve_cssa2.txt");
			if(!file4.exists())
			{
				cssa2 cs=new cssa2();
				curve cssa_curve=cs.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr,100);
				output_curve(cssa_curve,matlab_folder1+"/"+onto_names[i]+"_curve_cssa2.txt");
			}

			File file2=new File(matlab_folder1+"/"+onto_names[i]+"_curve_cssa.txt");
			if(!file2.exists())
			{
				cssa_fast cs=new cssa_fast();
				curve cssa_curve=cs.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr,100);
				output_curve(cssa_curve,matlab_folder1+"/"+onto_names[i]+"_curve_cssa.txt");
			}
			aims_faster am=new aims_faster();
			curve am_curve=am.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr, 100);
			output_curve(am_curve,matlab_folder1+"/"+onto_names[i]+"_curve_aims_faster_ms.txt");

			cssa_fast cs=new cssa_fast();
			curve cssa_curve=cs.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr, 500);
			output_curve(cssa_curve,matlab_folder1+"/"+onto_names[i]+"_curve_cssa.txt");

			cssa_ms_fast cms=new cssa_ms_fast();
			curve cssa_ms_curve=cms.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr, 100);
			output_curve(cssa_ms_curve,matlab_folder1+"/"+onto_names[i]+"_curve_cssa_ms.txt");

			File file3=new File(matlab_folder1+"/"+onto_names[i]+"_curve_inde_fast_new.txt");
			if(!file3.exists())
			{
				inde_set_fast isn=new inde_set_fast();
				curve inde_curve=isn.main(result_file, expanded_test_file, ontology_file_name, vertex, no_attr,70);
				output_curve(inde_curve,matlab_folder1+"/"+onto_names[i]+"_curve_inde_fast_new.txt");
			}*/


		}

		/*cssa_ms_fast cms=new cssa_ms_fast();
		curve cssa_ms_curve=cms.main(result_file, expanded_test_file, ontology_file_name, vertex,no_attr, 50);*/
	}

	public static void main(String[] args)
	{
		master om=new master();
		try {
			om.main();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
