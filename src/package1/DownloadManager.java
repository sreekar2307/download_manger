package package1;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
/**
 * @short The Main GUI class 
 * @author Sreekar Reddy, C. Rohith, Althaf Md.
 *
 */

public class DownloadManager extends JFrame implements Observer{

  private JTextField addTextField; ///< Add download text field.
  private DownloadTableModel tableModel; ///<  Download table's data model.
  private JTable table;  ///< Table showing downloads.
  private JButton pauseButton, resumeButton; ///< These are the buttons for managing the selected download.
  private JButton cancelButton, clearButton; ///< These are the buttons for managing the selected download. 
  private Download selectedDownload; ///< Currently selected download.
  private boolean clearing;  ///< Flag for whether or not table selection is being cleared.
  private JMenuBar menuBar;  ///< menu bar on the JFrame
  private JMenu fileMenu; ///< menu option FILE
  private JMenuItem fileExitMenuItem; ///< menuItem EXIT presented when clicked FILE menu

  /**
   * @breif constructor of the main class which is the JFrame
   * 
   */
  public DownloadManager(){
    setTitle("Download Manager");
    setSize(640,480);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        actionExit();
      }

    });

    /**
     * Code for menu bar and Exit MenuItem
     */
    menuBar = new JMenuBar();
    fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileExitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
    fileExitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        actionExit();
      }
    });
    fileMenu.add(fileExitMenuItem);
    menuBar.add(fileMenu);
    setJMenuBar(menuBar);


    /**
     * Setting up the Panel on the JFrame
     */

    JPanel addPanel = new JPanel();
    addTextField = new JTextField(30);
    addPanel.add(addTextField);
    JButton addButton = new JButton("Add Download");
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        actionAdd();
      }
    });
    addPanel.add(addButton);

    /**
     * Setting up the Downloads JTable on the JFrame
     */
    tableModel = new DownloadTableModel();
    table = new JTable(tableModel);
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {    
      @Override
      public void valueChanged(ListSelectionEvent e) {
        tableSelectionChanged();
      }
    });

    /**
     * Allow only one row at a time to be selected
     */
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    /**
     * setting up the progress bar column on the JTable
     */
    ProgressRenderer renderer = new ProgressRenderer(0,100);
    renderer.setStringPainted(true);
    table.setDefaultRenderer(JProgressBar.class, renderer);
    table.setRowHeight((int)renderer.getPreferredSize().getHeight());

    /**
     * Setting up the JPanel in which the JTable will be present
     */
    JPanel  downloadsPanel = new JPanel();
    downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
    downloadsPanel.setLayout(new BorderLayout());
    downloadsPanel.add(new JScrollPane(table),BorderLayout.CENTER);

    /**
     * Setting Up Buttons Panel
     */
    JPanel buttonsPanel = new JPanel();
    /*
     * Pause Button
     */
    pauseButton = new JButton("Pause");
    pauseButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {  
        actionPause();
      }

    });
    pauseButton.setEnabled(false);

    /*
     * Resume Button
     */

    resumeButton = new JButton("Resume");
    resumeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {  
        actionResume();
      } 
    });
    resumeButton.setEnabled(false);

    /*
     * cancel Button
     */

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {  
        actionCancel();
      } 
    });
    cancelButton.setEnabled(false);   

    /*
     * Clear Button
     */

    clearButton = new JButton("Clear");
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {  
        actionClear();
      } 
    });
    clearButton.setEnabled(false);

    /**
     * adding all the buttons to the buttons panel
     */
    buttonsPanel.add(pauseButton);
    buttonsPanel.add(resumeButton);
    buttonsPanel.add(clearButton);
    buttonsPanel.add(cancelButton);

    /**
     * Adding all Panels to the final GUI
     */
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(addPanel, BorderLayout.NORTH);
    getContentPane().add(downloadsPanel,BorderLayout.CENTER);
    getContentPane().add(buttonsPanel,BorderLayout.SOUTH);
  }

  /**
   * @short This action is triggered when a Download is selected in the JTable from the GUI
   */

  private void tableSelectionChanged() {
    // TODO Auto-generated method stub
    /**
     * Check if the none other Download is selected previously if so remove the DownloadManger as its observer
     */
    if(selectedDownload!=null)
      selectedDownload.deleteObserver(DownloadManager.this);
    /**
     * Add DownloadManager as the observer for the selected Download object 
     */
    
    if(!clearing && table.getSelectedRow() >-1) {
      selectedDownload = tableModel.getDownload(table.getSelectedRow());
      selectedDownload.addObserver(DownloadManager.this);
      updateButtons();
    }
  }
  
  /**
   * @short Pause the download 
   */
  
  private void actionPause() {
    // TODO Auto-generated method stub
    selectedDownload.pause();
    updateButtons();

  }
  
  /**
   *  @short Resume the download Once paused
   */
  
  private void actionResume() {
    selectedDownload.resume();
    updateButtons(); 
  }
  
  /**
   * @short  Clear the selected download from the JTable 
   */
  
  private void actionClear() {
    clearing =true;
    tableModel.clearDownload(table.getSelectedRow());
    clearing =false;
    selectedDownload=null;
    updateButtons();
  }
  
  /**
   * @short  Cancel the selected download from the JTable 
   */
  
  private void actionCancel() {
    selectedDownload.cancel();
    updateButtons();
  }
  
  /**
   * @short Handles the Logic when the buttons should be enabled and when they should be disabled at various
   * @short states for the downloading file can get into
   */
  
  private void updateButtons() {
    // TODO Auto-generated method stub
    if(selectedDownload!=null)
    {
      int status = selectedDownload.getStatus();
      switch (status)
      {
      case Download.DOWNLOADING:
        pauseButton.setEnabled(true);
        resumeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        clearButton.setEnabled(false);
        break;
      case Download.PAUSED:
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        cancelButton.setEnabled(true);
        clearButton.setEnabled(false);
        break;
      case Download.ERROR:
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        cancelButton.setEnabled(false);
        clearButton.setEnabled(true);
        break;
      default: // COMPLETE or CANCELLED
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);
        cancelButton.setEnabled(false);
        clearButton.setEnabled(true);
      }
    }
    else {
      // No download is selected in table.
      pauseButton.setEnabled(false);
      resumeButton.setEnabled(false);
      cancelButton.setEnabled(false);
      clearButton.setEnabled(false);
    }
  } 

  /**
   * @short This action is triggered When the application is closed this terminates the program and stops all the threads
   */

  private void actionExit() {
    System.exit(0);       
  }

  /**
   * @short This action is triggered when the The JButton addButton is clicked
   */

  private void actionAdd() {
    URL verifiedUrl = verifyURL(addTextField.getText());
    if(verifiedUrl!=null) {
      tableModel.addDownload(new Download(verifiedUrl));
      addTextField.setText(""); // reset add text field
    }else {
      JOptionPane.showMessageDialog(this,"Invalid Download URL","Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * @short this method checks weather the URL entered in the text field is a HTTP/HTTPS protocol or not 
   * @param url  extracted from the text field 
   * @return verifiedUrl
   */
  private URL verifyURL(String url) {
    /**
     *  Allow only HTTP and HTTPS URL's
     */
    if(!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://"))
      return null;

    // Verify format of URL
    URL verifiedUrl = null;
    try {
      verifiedUrl = new URL(url);
    }catch(Exception e) {
      return null;
    }
    // Make sure URL specifies a file
    if(verifiedUrl.getFile().length()<2)
      return null;
    return verifiedUrl;
  }

  @Override
  /**
   * @short This method is fired whenever Download Object is 
   */
  public void update(Observable o, Object arg) {
    // TODO Auto-generated method stub
    if(selectedDownload!=null && selectedDownload.equals(o))
      SwingUtilities.invokeLater(new Runnable(){
        @Override
        public void run() {       
          updateButtons();
        }
      });
  }

  public static void main(String args[]) throws IOException{
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        DownloadManager downloadManager = new DownloadManager();
        downloadManager.setVisible(true);
      }
    });
    //    Download test = new Download(new URL("http://www.africau.edu/images/default/sample.pdf"));

  }


}
