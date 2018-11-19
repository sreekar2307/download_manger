package download.manager;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

import javax.net.ssl.HttpsURLConnection;
/**
 * Ready to use.
 * @mainpage The Download manager
 * @author Sreekar Reddy, C. Rohith, Althaf Md.
 * @section intro_sec Introduction
 * This is the introduction.
 * 
 * 
 * @section Requirements_sec Requirements
 * @subsection JRE  
 * Install JRE to just use the application as it is
 * @subsection JDK
 * Install JDk to just fiddle around the code and make some changes
 * 
 * 
 * @section install_sec Installation
 * @subsection To install JDK  
 * $ sudo apt install openjdk-8-jdk
 * @subsection compile the code
 * $ javac Main.java
 * @subsection run the code
 * $ java Main
 */

public class Download  extends Observable implements Runnable {
  private URL url;  ///< URL of the file that you wish to download
  private int downloaded; ///< stores no of bytes downloaded so far
  private int size;   ///< size of the downloadable file in bytes
  private int status;  ///< status of the downloadable file range from 0-4 inclusive
  private static final int MAX_BUFFER_SIZE = 1024; 
  ///< max amount of data downloaded at an instant from the URL in bytes
  /**
   * All states the downloading life can get into.
   */
  protected static final String[] STATUSES = {"Downloading","Paused",
  "Complete","Cancelled","Error"};
  
  public static final int DOWNLOADING = 0; ///< One of the state of the downloading file 
  public static final int PAUSED = 1;///< state when the downloading file is paused
  public static final int COMPLETE = 2;
  ///< state when the downloading file is completed downloading
  public static final int CANCELLED = 3; ///< state when the downloading file is Cancelled
  public static final int ERROR = 4;  ///< stae when the downloading file enconters an error

  /**
   * Calls to the download function were the actual download starts.
   * @param url URL that needs to be downloaded
   *
   */

  public Download(URL url) {
    this.url = url;
    size = -1;
    downloaded = 0;
    status = DOWNLOADING;
    download();
  }

  /**
   * creation of new thread for each download added and starting the thread.
   */
  private void download() {
    Thread thread = new Thread(this);
    thread.start();
  }
  /**
   * Here the name of the downloaded file is given.
   * @param url  URL that needs to be downloaded
   * @return extracted file name from the URL
   */
  
  private String getFileName(URL url) {
    String fileName = url.getFile();
    return fileName.substring(fileName.lastIndexOf('/') + 1);
  }
  
  @Override
  /**
   * Thread run function which gets executed when start() method is called. Overriden method. 
   */
  public void run() { 
    RandomAccessFile file = null;  // To read and wirte to the downloaded file
    InputStream stream = null;    // Stream of data from the URL 
    try {
      HttpURLConnection connection  = (HttpURLConnection) url.openConnection(); 
      if (url.openConnection() instanceof HttpURLConnection) {
        connection  = (HttpURLConnection) url.openConnection();
      } else {
        connection  = (HttpsURLConnection) url.openConnection();
      }
      connection.setRequestProperty("Range","bytes=" + downloaded + "-");
      connection.connect();
      
      /**
       * Check the connection response if the range is in between  200-299 then allow
       */
      
      if (connection.getResponseCode() / 100 != 2) {
        error();
      }
      int contentLength  = connection.getContentLength();
      if (contentLength < 1) {
        error();
      }
      if (size == -1) {
        size = contentLength;
        stateChanged();
      }
      file = new RandomAccessFile("/home/sreekar/Downloads/" + getFileName(url),"rw");
      // by default the downloaded is stored in Downloads     
      file.seek(downloaded);
      stream = connection.getInputStream();
      while (status == DOWNLOADING) {
        byte[] buffer;  // temp place to store the data from the downloading file
        if (size - downloaded > MAX_BUFFER_SIZE) {
          buffer =  new byte[MAX_BUFFER_SIZE];
        }  else {
          buffer = new byte[size - downloaded];
        }
        int read = stream.read(buffer);
        if (read == -1) {
          break;
        }

        file.write(buffer,0,read);
        downloaded += read;
        stateChanged();
      }
      /*
       * Downloading complete
       */
      if (status == DOWNLOADING) {
        status = COMPLETE;
        stateChanged();
      }
    } catch (Exception e) {
      error();
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (Exception e) {
          error();
        }
      }
      if (stream != null) {
        try {
          stream.close();
        } catch (Exception e) {
          error();
        }
      }
    }

  }

  /**
   * This is method is hit whenever a state change is occurred. 
   */

  private void stateChanged() {
    setChanged();   
    notifyObservers();  // notify all the observers that show change has occurred 
  }

  /**
    * This is method is hit whenever a state change is occurred.
    */

  public void error() {
    status = ERROR;
    stateChanged();
  }

  /**
   * Get method for URL later used to verify URL.
   * @return url link of the downloading file
   */

  public URL getUrl() {
    return url;
  }

  /**
   * Get size of the downloading file.
   * @return size of the downloading file
   */

  public int getSize() {
    return size;
  }

  /**
   * later used by GUI to update JButtons.
   * @return current status of the downloading file
   */

  public int getStatus() {
    return status;
  }

  /**
   * Get progress of the download.
   * @return progress of download in percentage
   */

  public float getProgress() {
    /*
     * Recovered using the debugger
     */
    return ((float) downloaded / size) * 100;
  }

  /**
   * Pause the download changes the status of the download.
   * @breif pause the download calls statechanged() method
   */

  public void pause() {
    status = PAUSED;
    stateChanged();
  }

  /**
   * Resume the downloading once it is paused.
   * @breif resume the download once paused calls statechanged() method
   */

  public void resume() {
    status = DOWNLOADING;
    stateChanged();
    /**
     * Rectified using debugging tool
     */
    download();
  }

  /**
   * @breif cancel the download one of the state the downloading file can get into
   */

  public void cancel() {
	  status = CANCELLED;
	  stateChanged();
  }
}
