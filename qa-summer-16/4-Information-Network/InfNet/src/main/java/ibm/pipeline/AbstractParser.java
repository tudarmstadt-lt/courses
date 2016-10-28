package ibm.pipeline;

import ibm.pipeline.Pipeline.TransitionType;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
@Deprecated
public abstract class AbstractParser extends AbstractTransition {

	public AbstractParser(String sourceFolder, String destinationFolder) {
		super(sourceFolder, destinationFolder);
	}

	@Override
	public TransitionType getTransitionType() {
		return TransitionType.PARSER;
	}

}
