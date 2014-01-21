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
import funcat.cssa;
import funcat.cssa_ms_new;
import funcat.inde_set_new;

public class master 
{
	OWLOntologyManager manager;
	OWLOntology go;
	OWLReasoner reasoner;
	OWLDataFactory factory;

	public void create_new_ontology(String inputfile,String ontology_name,String ontology_file_name) throws OWLOntologyCreationException
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

	public static String get_parent(String s)
	{
		return s.substring(0,s.lastIndexOf('.'));
	}

	public ArrayList<String> create_vertex_list()
	{
		ArrayList<String> vertex=new ArrayList<String>();
		Set<OWLClass> ce=reasoner.getTopClassNode().getEntities();
		for(OWLClass c : ce)
		{
			//System.out.println(c.asOWLClass().getIRI().toString());
			OWLClassExpression cx=c.asOWLClass();
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
				}
			}
		}
		return vertex;
	}

	public void create_fv_arr(String outputfile,ArrayList<String> vertex)
	{
		System.out.println("Creating kernel file----");
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

	public void convert_arff_file(ArrayList<String> vertex,String inputfile,String outputfile)
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
			while((line = br.readLine())!=null)
			{
				if(!line.isEmpty()&&!line.contains("data")&&!line.contains("DATA"))
				{
					//System.out.println(line+"\n");
					System.out.println("\n--------------------\nTaining example: "+(++c)+"\n-----------------");
					out="";
					label_vec="";
					line=line.replaceAll("\\?", "-99999999");
					String[] part=line.split(",");
					String gene_l=part[part.length-1];
					out=line.substring(0,line.length()-1-gene_l.length());
					System.out.println(gene_l);
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
						System.out.println("Temp> "+gene.length);
						int y=predicted_classes.indexOf(temp);
						System.out.println(""+temp);
						vec.set(predicted_classes.indexOf(temp),new Double(1));
					}
					label_vec=",";
					//System.out.println();
					for(double d : vec)
					{
						label_vec+=(int)d+",";
					}
					System.out.println("-----------");
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

	public void convert_arff_file_expand(ArrayList<String> vertex,String inputfile,String outputfile)
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
			while((line = br.readLine())!=null)
			{
				if(!line.isEmpty()&&!line.contains("data")&&!line.contains("DATA"))
				{
					//System.out.println(line+"\n");
					System.out.println("\n--------------------\nTaining example: "+(++c)+"\n-----------------");
					out="";
					label_vec="";
					line=line.replaceAll("\\?", "-99999999");
					String[] part=line.split(",");
					String gene_l=part[part.length-1];
					out=line.substring(0,line.length()-1-gene_l.length());
					System.out.println(gene_l);
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
						System.out.println("Temp> "+gene.length);
						int y=predicted_classes.indexOf(temp);
						System.out.println(""+temp);
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
										System.out.println("Super class: "+news+" index: "+predicted_classes.indexOf(news));
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
					System.out.println("-----------");
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
	
	public void main() throws OWLOntologyCreationException
	{
		int limit=2;
		
		String ontology_name="cellcycle0";
		String ontology_file_name="E:/ontologies/cellcycle0_FUN_new.owl";
		String fv_arr_file="E:/test/fv_arr.txt";
		String train_file="E:/test/cellcycle_FUN.train.arff";
		String test_file="E:/test/cellcycle_FUN.test.arff";
		String converted_train_file="E:/test/cellcycle_FUN.train.converted.arff";
		String converted_test_file="E:/test/cellcycle_FUN.test.converted.arff";
		String expanded_test_file="E:/test/cellcycle_FUN.test.expanded.arff"; // required to check the results in cssa_ms_new.jav and inde_set_new.java
		String result_file="E:/test/result.txt";
		String matlab_folder="E:/test";

		create_new_ontology("E:/test/cellcycle_FUN.train.arff", ontology_name,ontology_file_name);
		manager = OWLManager.createOWLOntologyManager();
		go = manager.loadOntologyFromOntologyDocument(new File(ontology_file_name));
		System.out.println("Loaded ontology: " + go.getOntologyID().toString());
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		reasoner = reasonerFactory.createReasoner(go, config);
		factory = manager.getOWLDataFactory();

		ArrayList<String> vertex=create_vertex_list();
		create_fv_arr(fv_arr_file,vertex);
		convert_arff_file(vertex,train_file,converted_train_file);
		convert_arff_file(vertex,test_file,converted_test_file);
		convert_arff_file_expand(vertex,test_file,expanded_test_file);

		//String[] cmd = { "matlab", "/r", "\"cd('"+matlab_folder+"');file1="+fv_arr_file+";\"" };
		//String[] cmd = { "matlab", "/r", "\"cd('"+matlab_folder+"')\"" };

		String[] cmd = { "matlab", "/r", "\"cd('"+matlab_folder+"');file1='"+fv_arr_file+"';file2='"+converted_train_file+"';file3='"+converted_test_file+"';file4='"+result_file+"';KPCA_final\"" };
		Process p;
		try {
			p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		File result=new File(result_file);
		while(!result.exists())
		{
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		cssa cs=new cssa();
		curve cssa_curve=cs.main(result_file, expanded_test_file, ontology_file_name, vertex, 2);
		
		cssa_ms_new cms=new cssa_ms_new();
		curve cssa_ms_curve=cms.main(result_file, expanded_test_file, ontology_file_name, vertex, 2);
		
		inde_set_new isn=new inde_set_new();
		curve inde_curve=isn.main(result_file, expanded_test_file, ontology_file_name, vertex, 2);
		
		output_curve(cssa_curve,"curve_cssa.txt");
		output_curve(cssa_ms_curve,"curve_cssa_ms.txt");
		output_curve(inde_curve,"curve_inde_set.txt");
	}

	public static void main(String[] args)
	{
		master om=new master();
		try {
			om.main();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
}
