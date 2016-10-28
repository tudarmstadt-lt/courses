package ibm.pipeline;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class should represent the backend. We have various steps in which the
 * data gets processed.
 * 
 * At first we have *.html files. The goal of the backend is to process these
 * files and write a graph to the graph database. The front-end can query the
 * database and get the data as needed.
 * 
 * This class should also provide functionality to skip steps. This is
 * necessary, because IBM Bluemix Services might have limitations. E.g. Alchemy
 * allows only to process 1000 files / day.
 * 
 * 1) HTML --> 2) ExtractInformation (JSON-Files) --> 3) Nodes and Edges (JSON)
 * 4) GraphDB
 * 
 * Nomenclatur: 1 ---- a) ----> 2 ---- b) ----> 3 ---- c) ----> 4 a): Extractor
 * b): Parser c): Writer
 * 
 * Bottleneck is at transition b. You should be able to use Alchemy and/or
 * Relationship extraction
 * 
 * How skipping steps (see above) should work: - The pipeline ALWAYS works
 * through all steps with all transitions. - Transitions must be registered
 * before. - If no transition is registered the pipeline will throw an
 * exception. - Instead of registering a transition, it should be possible to
 * preload results. - For debugging purposes it should be possible to execute
 * single transitions. E.g. don't do c) and don't write to GraphDB
 * 
 * Another option which should be implemented later: The ability to write data
 * persistantly (in files) or just run the whole pipeline in memory. Also nice
 * to have: pipeline optimization: don't need to wait till all data is processed
 * in one step: If one file is finished with a) it could be processed by b). a)
 * can handle the next file in the mean time.
 * 
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 * 
 */
@Deprecated
public class Pipeline {

	/**
	 * the root dir. It contains the 3 directories below.
	 */
	private String rootDir;

	/**
	 * contains all transitions
	 */
	private Map<TransitionType, ITransition> transitions;

	private Map<TransitionType, String> preloadResultPath;

	public Pipeline(String rootDir) {
		this.rootDir = rootDir;
		transitions = new HashMap<>();
	}

	/**
	 * runs the configuration of the pipeline
	 */
	public void run() {
		if (!checkCorrectness()) {
			System.err.println("Configuration not correct.");
			return;
		}

		if (preloadResultPath != null && preloadResultPath.size() > 0) {
			if (preloadResultPath.containsKey(TransitionType.EXTRACTOR)) {
				transitions.get(TransitionType.PARSER).run();
			}
		} else {
			transitions.get(TransitionType.EXTRACTOR).run();
			transitions.get(TransitionType.PARSER).run();
		}
		transitions.get(TransitionType.WRITER).run();
	}

	public void registerTransition(ITransition transition) {
		transition.setRootFolder(rootDir);
		transitions.put(transition.getTransitionType(), transition);
	}

	/**
	 * Important notice: it is only possible to preload one result! Should be
	 * EXTRACTOR or PARSER.
	 * 
	 * @param transitionType
	 * @param resultPath
	 */
	public void preloadResults(TransitionType transitionType, String resultPath) {
		if (preloadResultPath == null || preloadResultPath.size() > 0) {
			preloadResultPath = new HashMap<>();
		}
		preloadResultPath.put(transitionType, rootDir + File.separator + resultPath);
	}

	private boolean checkCorrectness() {
		if (transitions.keySet().size() == 3) {
			return true;
		}
		if (preloadResultPath != null && preloadResultPath.size() > 0) {
			TransitionType preloadType = preloadResultPath.keySet().iterator().next();
			switch (preloadType) {
			case EXTRACTOR:
				if (transitions.containsKey(TransitionType.PARSER) && transitions.containsKey(TransitionType.WRITER)) {
					return true;
				}
				break;
			case PARSER:
				if (transitions.containsKey(TransitionType.WRITER)) {
					return true;
				}
				break;
			case WRITER:
				break;
			default:
				break;
			}
		}

		return false;
	}

	public enum TransitionType {
		EXTRACTOR, PARSER, WRITER
	}
}
