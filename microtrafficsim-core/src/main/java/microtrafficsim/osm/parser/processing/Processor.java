package microtrafficsim.osm.parser.processing;

import microtrafficsim.osm.parser.Parser;
import microtrafficsim.osm.parser.base.DataSet;


/**
 * Processing unit to perform actions on the parsed and abstracted DataSet.
 * The Processor is responsible for calling the necessary FeatureGenerators.
 * 
 * @author Maximilian Luz
 */
public interface Processor {
	
	/**
	 * Executes this processing unit.
	 * 
	 * @param parser	the parser on which the Processor should be executed.
	 * @param dataset	the DataStore on which the Processor should be executed.
	 */
	void execute(Parser parser, DataSet dataset);
}
