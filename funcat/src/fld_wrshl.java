import java.util.*;
import java.io.*;

import Jama.*;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
public class fld_wrshl {
	public static int[][] path,dis,is_a,part_of;
	public static void main(String[] args) throws OWLOntologyCreationException
	{
		File file = new File("/host/ontologies/cellcycle_FUN.owl"); //go_daily-termdb  treeOnt
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	 	OWLOntology go = manager.loadOntologyFromOntologyDocument(file);
	 	OWLDataFactory dataFactory = manager.getOWLDataFactory();
	 	System.out.println("Loaded ontology: " + go.getOntologyID().toString());
	 	OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
	 	ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
	 	OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
	 	OWLReasoner reasoner = reasonerFactory.createReasoner(go, config);
	 	OWLDataFactory factory = manager.getOWLDataFactory();
	 	
	 	Set<OWLObjectProperty> propset = go.getObjectPropertiesInSignature();
	 	for (OWLObjectProperty ind : propset) {
        		String news=ind+"";
        		System.out.println(news);
	 	}
        
	 	ArrayList<String> vertex=new ArrayList<String>();
	 	Set<OWLClass> ce=reasoner.getTopClassNode().getEntities();
	 	for(OWLClass c : ce)
	 	{
	 		//System.out.println(c.asOWLClass().getIRI().toString());
	 		OWLClassExpression cx=c.asOWLClass();
	 		NodeSet<OWLClass> set=reasoner.getSubClasses(cx, false);
	        for (Node<OWLClass> ind : set) {
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
	 	/*OWLClass top = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/GO#GO_0008150"));
	 	try {
	 	NodeSet<OWLClass> set=reasoner.getSubClasses(top, false);
        for (Node<OWLClass> ind : set) {
        	if(!ind.isBottomNode())
        	{
        		String news=ind+"";
        		System.out.println(news);
	            String[] part=news.split(" ");
	            part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
	            vertex.add(part[1]);
        	}
        }
	 	}
	 	catch(Exception e)
	 	{
	 	}
	 	finally {
	 		System.out.println("Vertexes: "+vertex.size());
	 	}*/
	 	System.out.println("Vertexes: "+vertex.size());
	 	int n=vertex.size();
	 	is_a=new int[n][n];
	 	part_of=new int[n][n];
	 	dis=new int[n][n];
	 	for (int[] row : dis)
	 	    Arrays.fill(row, 99999);
	 	for (int[] row : is_a)
	 	    Arrays.fill(row, 99999);
	 	for (int[] row : part_of)
	 	    Arrays.fill(row, 99999);
	 	
	 	// FILL IS A
	 	System.out.println("Loading:");
	 	int c=0,prev=0;
	 	for(String temp : vertex)
	 	{
	 		if((c*100)/vertex.size()!=prev)
	 			System.out.println((c*100)/vertex.size()+"%");
	 		prev=(c*100)/vertex.size();
	 		int idx=vertex.indexOf(temp);
	 		dis[idx][idx]=0;
	 		if(temp.contains("root"))
	 		System.out.println("Here: "+temp);
	 		//OWLClass vertex_c = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/gene_ontology_edit#"+temp));
	 		OWLClass vertex_c = dataFactory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+temp));
	 		//OWLClass vertex_c = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/GO#"+temp));
	 		NodeSet<OWLClass> set=reasoner.getSubClasses(vertex_c, true);
	 		for(Node<OWLClass> cls : set)
	 	 	{
	 	 		if(!cls.isBottomNode())
	 	 		{
	 	 			String news=cls+"";
	 	 			String[] part=news.split(" ");
	 	 			part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
	 	 			dis[idx][vertex.indexOf(part[1])]=1;
	 	 			dis[vertex.indexOf(part[1])][idx]=1;
	 	 			is_a[idx][vertex.indexOf(part[1])]=1;
	 	 			is_a[vertex.indexOf(part[1])][idx]=1;
	 	 		}
	 	 	}
	 	 	set=reasoner.getSuperClasses(vertex_c, true);
	 	 	for(Node<OWLClass> cls : set)
	 	 	{
	 	 		if(!cls.isTopNode())
	 	 		{
	 	 			String news=cls+"";
	 	 			String[] part=news.split(" ");
	 	 			part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
	 	 			dis[idx][vertex.indexOf(part[1])]=1;
	 	 			dis[vertex.indexOf(part[1])][idx]=1;
	 	 			is_a[idx][vertex.indexOf(part[1])]=1;
	 	 			is_a[vertex.indexOf(part[1])][idx]=1;
	 	 		}
	 	 	}
	 	 	c++;
	 	}
	 	
	 	for (int i=0; i<n; i++) 
  		{
    		for (int j=0; j<n;j++) 
    		{
    			is_a[i][j]=dis[i][j];
      		}
  		}
	 	
	 	// PART OF FILL
	 	ArrayList<OWLClass> n_cls=new ArrayList<OWLClass>();
	 	c=0;prev=0;
	 	System.out.println("Adding axioms");
	 	for(String temp : vertex)
	 	{
	 		if((c*100)/vertex.size()!=prev)
	 			System.out.println((c*100)/vertex.size()+"%");
	 		prev=(c*100)/vertex.size();
		 	/*OWLObjectProperty role = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owl/PART_OF"));
		 	OWLClass s_class = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/gene_ontology_edit#"+temp));
		 	OWLClassExpression hasRoleSome = factory.getOWLObjectSomeValuesFrom(role, s_class);
		 	OWLClass newClass = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/gene_ontology_edit#"+temp+"n"));
		 	//OWLObjectProperty role = factory.getOWLObjectProperty(IRI.create("http://purl.org/obo/owl/part_of"));
	 		/*OWLObjectProperty role = factory.getOWLObjectProperty(IRI.create("http://purl.obolibrary.org/obo/RO_0002213"));
		 	OWLClass s_class = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/GO#"+temp));
		 	OWLClassExpression hasRoleSome = factory.getOWLObjectSomeValuesFrom(role, s_class);
		 	OWLClass newClass = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/GO#"+temp+"n"));*/
	 		OWLObjectProperty role = factory.getOWLObjectProperty(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"/PART_OF"));
		 	OWLClass s_class = dataFactory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+temp));
		 	OWLClassExpression hasRoleSome = factory.getOWLObjectSomeValuesFrom(role, s_class);
		 	OWLClass newClass = dataFactory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+temp+"n"));
		 	n_cls.add(newClass);
			OWLEquivalentClassesAxiom ec=factory.getOWLEquivalentClassesAxiom(newClass,hasRoleSome); 
			AddAxiom addAxiom = new AddAxiom(go, ec);
			manager.applyChange(addAxiom);
			c++;
	 	}
	 	// REASONING
	 	System.out.println("100%\n");
	 	OWLReasonerFactory reasonerFactory1 = new JcelReasonerFactory();
	 	reasoner = reasonerFactory1.createReasoner(go);
	 	
	 	// LOADING PAIRS
	 	c=0;prev=0;
	 	System.out.println("Loading pair relations");
	 	for(OWLClass nc : n_cls)
	 	{
	 		if((c*100)/n_cls.size()!=prev)
	 			System.out.println((c*100)/n_cls.size()+"%");
	 		prev=(c*100)/vertex.size();
	 		String gene=nc.toString().replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
	 		if(gene.charAt(gene.length()-1)=='n')
	 			gene=gene.substring(0,gene.length()-1);
	 		int idx_i=vertex.indexOf(gene);
			NodeSet<OWLClass> set=reasoner.getSubClasses(nc, true);
		 	for(Node<OWLClass> cls : set)
		 	{
		 		if(!cls.isBottomNode())
		 		{
		 			String gene1=cls.toString().split(" ")[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
		 			if(!(gene1.charAt(gene1.length()-1)=='n'))
		 			{
			 			int idx_j=vertex.indexOf(gene1);
			 			//System.out.println("PART-oF>"+gene+" "+vertex.indexOf(gene)+" "+gene1+" "+vertex.indexOf(gene1));
			 			if(dis[idx_i][idx_j]!=1)
			 			{
			 				dis[idx_i][idx_j]=1;
			 				dis[idx_j][idx_i]=1;
			 				part_of[idx_i][idx_j]=1;
			 				part_of[idx_j][idx_i]=1;
			 			}
		 			}
		 		}
		 	}
		 	c++;
	 	}
	 	
	 	
	 	System.out.println("100%\n");
	 	/*System.out.print("Dist\t");
	 	for(String temp : vertex)
	 	{
	 		System.out.print(temp+"\t");
	 	}
	 	System.out.println();
	 	for (int i=0; i<n; i++) 
  		{
			System.out.print(vertex.get(i)+"\t");
    		for (int j=0; j<n;j++) 
    		{
    			System.out.print(dis[i][j]+"\t");
      		}
    		System.out.println();
  		}*/
	 	path=new int[n][n];
	 	for (int[] row : path)
	 	    Arrays.fill(row, -1);
	 	prev=0;
	 	
	 	// MAIN COMPUATION
	 	System.out.println("Calculating");
	 	for (int k=0; k<n;k++) 
	 	{
	 		int per=(k*100)/n;
	 		if(per!=prev)
	 			System.out.println(per+"%");
	 		prev=per;
      		for (int i=0; i<n; i++) 
      		{
        		for (int j=0; j<n;j++) 
        		{
        			if (dis[i][k]+dis[k][j] < dis[i][j]) 
        			{
          				dis[i][j] = dis[i][k]+dis[k][j];
          				path[i][j]=k;
          			}
          		}
      		}
    	}
	 	/*System.out.print("\n\nDist\t");
	 	for(String temp : vertex)
	 	{
	 		System.out.print(temp+"\t");
	 	}
	 	System.out.println();
	 	for (int i=0; i<n; i++) 
  		{
			System.out.print(vertex.get(i)+"\t");
    		for (int j=0; j<n;j++) 
    		{
    			System.out.print(dis[i][j]+"\t");
      		}
    		System.out.println();
  		}*/
	 	
	 	//FIND_MAX
	 	int max=0;
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
	 	max=0;
	 	for (int j=0; j<n;j++) 
		{
			if(dis[vertex.indexOf("01")][j]!=99999)
			{
    			if (dis[vertex.indexOf("01")][j]>max) 
    			{
      				max = dis[vertex.indexOf("01")][j];
      			}
			}
  		}
	 	System.out.println("Max depth: "+max+" from: 01 / "+vertex.indexOf("01"));
	 	try { 
			new ObjectOutputStream(new FileOutputStream("dis")).writeObject(dis);
			new ObjectOutputStream(new FileOutputStream("path")).writeObject(path);
			new ObjectOutputStream(new FileOutputStream("is_a")).writeObject(is_a);
			new ObjectOutputStream(new FileOutputStream("part_of")).writeObject(part_of);
			new ObjectOutputStream(new FileOutputStream("vertex1")).writeObject(vertex);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	 	//RECONSTRUCTION
	 	System.out.println("Found Max: "+max);
	 	/*System.out.println("Reconstruction");
	 	for (int i=0; i<n; i++) 
  		{
    		for (int j=0; j<n;j++) 
    		{
    			if(i!=j)
    			{
    				String a_path=GetPath(i,j);
    				System.out.println("Path from "+"<"+vertex.get(i)+">"+a_path+"<"+vertex.get(j)+">"+" Cost: "+dis[i][j]);
    				
    			}
      		}
  		}*/
	}
	public static float computeCost(int i,int j,String a_path,ArrayList<String> vertex)
	{
		float w=0;
		int prev=i;
		for(int k=0;k < a_path.length();k++)
		{
			if(Character.isDigit(a_path.charAt(k)))
			{
				String num = "";
				int l;
				for(l=k;a_path.charAt(l)!=' ';l++)
				{
					num+=a_path.charAt(l)+"";
				}
				k+=l-1;
				int idx=Integer.parseInt(num);
				if(is_a[prev][idx]==1)
				{
					w+=1;
				}
				else if(part_of[prev][idx]==1)
				{
					w+=2;
				}
				prev=idx;
			}
		}
		if(is_a[prev][j]==1)
		{
			w+=1;
		}
		else if(part_of[prev][j]==1)
		{
			w+=2;
		}
		return w;
	}
	public static String GetPath (int i, int j) {
	    if (dis[i][j] == 99999) 
	    	return "no path";
	    int intermediate = path[i][j];
	    if (intermediate < 0) return " ";
	    return GetPath(i,intermediate) + intermediate + GetPath(intermediate,j);
	}
}
