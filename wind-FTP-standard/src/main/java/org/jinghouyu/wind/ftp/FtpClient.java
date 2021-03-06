package org.jinghouyu.wind.ftp;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * for providing a standard interface for varies of ftp client .jar file.
 * every kind of ftp client must implement it.
 *
 * @author Simsir.L
 * modified by <person></person> on <data></date>
 *
 * |  created date  | modified date  |  modified person |
 * |         |           |        |
 * @description
 *
 * @since 1.0.0
 */
public interface FtpClient extends Closeable {
	
	/**
	 * set the transfer mode of Ftp communication
	 * @param passiveMode
	 */
	void setMode(boolean passiveMode);
	
	/**
	 * upload local file to Ftp server
	 * @param localPath       the path of local file
	 * @param serverPath      the path of server where you want to put file to.
	 * @throws FtpException   if error occurs, FtpException will be thrown up.
	 */
	void upload(String localPath, String serverPath) throws FtpException, IOException, FileNotFoundException;
	
	/**
	 * upload local stream to Ftp serve
	 * @param inputStream      the local input stream
	 * @param serverPath       the path of server where you want to put data to.
	 * @throws FtpException    if error occurs, FtpException will be thrown up.
	 */
	void upload(InputStream inputStream, String serverPath) throws FtpException, IOException;
	
	/**
	 * download a file of server to localPath
	 * @param serverPath          the file path of server which would be downloaded
	 * @param localPath           the local path where you want to put data to.
	 * @throws FtpException       if error occurs, FtpException will be thrown up.
	 */
	void download(String serverPath, String localPath) throws FtpException, IOException, FileNotFoundException;
	
	/**
	 * download a file of server to an outputstream
	 * @param serverPath         the file path of server which would be downloaded
	 * @param out                the outputstream which you want to put data to.
	 * @throws FtpException      if error occurs, FtpException will be thrown up.
	 */
	void download(String serverPath, OutputStream out) throws FtpException, IOException;
	
	/**
	 * make a directory.
	 * @param dir			   directory path
	 * @throws FtpException    if error occurs, FtpException will be thrown up.
	 */
	void mkdir(String dir) throws FtpException, IOException;
	
	/**
	 * remove a file
	 * @param filePath           file path
	 * @throws FtpException      if error occurs, FtpException will be thrown up.
	 */
	void rmFile(String filePath) throws FtpException, IOException;
	
	/**
	 * remove a directory
	 * @param dirPath          directory path
	 * @throws FtpException    if error occurs, FtpException will be thrown up.
	 */
	void rmDir(String dirPath) throws FtpException, IOException;
	
	/**
	 * get sub files info from the remote server
	 * @param serverPath
	 * @return
	 * @throws FtpException
	 * @throws IOException
	 */
	FtpFile[] getFiles(String serverPath) throws FtpException, IOException;

}
