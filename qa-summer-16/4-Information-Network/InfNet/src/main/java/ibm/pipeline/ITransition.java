package ibm.pipeline;

import ibm.pipeline.Pipeline.TransitionType;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
@Deprecated
public interface ITransition {

	public void run();

	public TransitionType getTransitionType();

	public void setRootFolder(String rootFolder);
}
