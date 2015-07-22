package co.nordlander.activemft.filters;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;

/**
 * An {@link FileFilter} that filters based on fileage and filename pattern.
 */
public class FileReceiveFilter extends AbstractFileFilter {
	
	protected String pattern;
	protected Long fileage;
	
	public FileReceiveFilter(Long fileage, String pattern){
		this.pattern = pattern;
		this.fileage = fileage;
	}
	
	@Override
	public boolean accept(File file){
		return !file.getName().endsWith(".xfer") &&
	      FilenameUtils.wildcardMatch(file.getName(), pattern, IOCase.INSENSITIVE) &&
	      FileUtils.isFileOlder(file, System.currentTimeMillis() - fileage);
	}
}
