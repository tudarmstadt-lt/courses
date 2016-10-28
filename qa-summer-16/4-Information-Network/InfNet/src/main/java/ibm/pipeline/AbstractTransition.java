package ibm.pipeline;

import java.io.File;

/**
 * @author Information Network - Question Answering Technologies behind and with
 *         IBM Watson - SS2016
 * @author Simon S.
 */
@Deprecated
public abstract class AbstractTransition implements ITransition {

	private String rootFolder;

	protected String sourceFolder;
	protected String destinationFolder;

	public AbstractTransition(String sourceFolder, String destinationFolder) {
		this.sourceFolder = sourceFolder;
		this.destinationFolder = destinationFolder;
	}

	@Override
	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
		this.sourceFolder = this.rootFolder + File.separator + sourceFolder;
		this.destinationFolder = rootFolder + File.separator + destinationFolder;
	}
}
