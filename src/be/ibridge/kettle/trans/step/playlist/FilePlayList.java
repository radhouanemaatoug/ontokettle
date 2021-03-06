package be.ibridge.kettle.trans.step.playlist;

import org.apache.commons.vfs.FileObject;

import be.ibridge.kettle.core.exception.KettleException;

public interface FilePlayList {

	boolean isProcessingNeeded(FileObject file, long lineNr, String filePart) throws KettleException;

}
