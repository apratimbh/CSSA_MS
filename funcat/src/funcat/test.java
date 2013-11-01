package funcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;

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

public class test {
	public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, IOException, OWLOntologyCreationException
	{
		ArrayList<String> vertex=(ArrayList<String>) new ObjectInputStream(new FileInputStream("vertex2")).readObject();
		//System.out.println(vertex.size());
		//Collections.sort(vertex);

		File file = new File("E:/ontologies/cellcycle.owl"); //go_daily-termdb  treeOnt
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology go = manager.loadOntologyFromOntologyDocument(file);
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		System.out.println("Loaded ontology: " + go.getOntologyID().toString());
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		ConsoleProgressMonitor progressMonitor = new ConsoleProgressMonitor();
		OWLReasonerConfiguration config = new SimpleConfiguration(progressMonitor);
		OWLReasoner reasoner = reasonerFactory.createReasoner(go, config);
		OWLDataFactory factory = manager.getOWLDataFactory();
		double[] stat=new double[11];
		for(int i=0;i<stat.length;i++)
		{
			stat[i]=0;
		}
		
		for(String temp : vertex)
		{
			OWLClass vertex_c = dataFactory.getOWLClass(IRI.create(go.getOntologyID().getOntologyIRI().toString()+"#"+temp));
			//OWLClass vertex_c = dataFactory.getOWLClass(IRI.create("http://purl.org/obo/owl/GO#"+temp));
			NodeSet<OWLClass> set=reasoner.getSuperClasses(vertex_c, true);
			int c_temp=0;
			for(Node<OWLClass> cls : set)
			{
				if(!cls.isTopNode())
				{
					c_temp++;
					
					String news=cls+"";
		            String[] part=news.split(" ");
		            part[1]=part[1].replaceAll("^<", "").replaceAll(">$", "").split("#")[1];
		            System.out.println(part[1]);
		            
				}
			}
			if(c_temp<10)
			{
				stat[c_temp]++;
			}
			else
			{
				stat[10]++;
			}
		}
		for(int i=0;i<stat.length;i++)
		{
			stat[i]=(stat[i]/vertex.size())*100;
		}
		for(int i=0;i<stat.length;i++)
		{
			System.out.println("Parents "+i+" = "+stat[i]+"%");
		}
	}
}