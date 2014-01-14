package file_create;
import java.util.*;
import java.io.*;

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

import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
public class create_tree {
public static void main(String[] args) throws OWLOntologyCreationException
{
	BufferedReader br;
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	IRI ontologyIRI = IRI.create("http://www.co-ode.org/ontologies/cellcycle_FUN_root.owl");
	IRI documentIRI = IRI.create("file:/E:/ontologies/cellcycle_FUN_new.owl");
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
		br = new BufferedReader(new FileReader("E:/dataset/cellcycle_FUN.train.arff"));
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
						//System.out.println(gene);
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
		
		/*for (OWLClass cls : ontology.getClassesInSignature()) {
			boolean flag=true;
			NodeSet<OWLClass> set=reasoner.getSuperClasses(cls, true);
	 		for(Node<OWLClass> scls : set)
	 	 	{
	 	 		if(!scls.isTopNode())
	 	 		{
	 	 			flag=false;
	 	 		}
	 	 	}
	 		if(flag)
	 		{
	 			OWLAxiom axiom = factory.getOWLSubClassOfAxiom(cls, root);
				AddAxiom addAxiom = new AddAxiom(ontology, axiom);
				manager.applyChange(addAxiom);
				System.out.println("Referenced class: " + cls);
	 		}
        }*/
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
}
