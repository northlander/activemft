package co.nordlander.activemft.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

import co.nordlander.activemft.domain.TransferJob;

public abstract class Receiver {
	
	public abstract void receiveFiles(TransferJob job) throws FileNotFoundException, Exception;
}
