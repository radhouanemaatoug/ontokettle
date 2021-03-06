package be.ibridge.kettle.trans.step.playlist;

import java.io.IOException;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.step.errorhandling.AbstractFileErrorHandler;

public class FilePlayListReplayErrorFile extends FilePlayListReplayFile {

	private FileObject errorFile;

	public FilePlayListReplayErrorFile(FileObject errorFile, FileObject processingFile) {
		super(processingFile, AbstractFileErrorHandler.NO_PARTS);
		this.errorFile = errorFile;
	}

	public boolean isProcessingNeeded(FileObject file, long lineNr, String filePart)
			throws KettleException {
        try
        {
            return errorFile.exists();
        }
        catch(IOException e)
        {
            throw new KettleException(e);
        }
	}

}
