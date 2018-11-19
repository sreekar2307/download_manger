package download.manager;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

/**
 * The Main GUI class. 
 * @author Sreekar Reddy, C. Rohith, Althaf Md.
 *
 */

public class DownloadManager extends JFrame implements Observer{

  private JTextField addTextField; ///< Add download text field.
  private DownloadTableModel tableModel; ///<  Download table's data model.
  private JTable table;  ///< Table showing downloads.
  private JButton pauseButton;
  private JButton resumeButton; ///< These are the buttons for managing the selected download.
  private JButton cancelButton; ///< These are the buttons for managing the selected download. 
  private JButton clearButton;
  private Download selectedDownload; ///< Currently selected download.
  private boolean clearing;  ///< Flag for whether or not table selection is being cleared.
  private JMenuBar menubar;  ///< menu bar on the JFrame
  private JMenu fileMenu; ///< menu option FILE
  private JMenuItem fileExitMenuItem; ///< menuItem EXIT presented when clicked FILE menu

  /**
   * constructor of the main class which is the JFrame.
   * 
   */
  public DownloadManager() {
    setTitle("Download Manager");
    setSize(640,480);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        actionExit();
      }
    });

    /**
     * Code for menu bar and Exit MenuItem
     */
    menubar = new JMenuBar();
    fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileExitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
    fileExitMenuItem.addActionListener(e -> actionExit());
    fileMenu.add(fileExitMenuItem);
    menubar.add(fileMenu);
    setJMenuBar(menubar);


    /**
     * Setting up the Panel on the JFrame.
     */

    JPanel addPanel = new JPanel();
    addTextField = new JTextField(30);
    addPanel.add(addTextField);
    JButton addButton = new JButton("Add Download");
    addButton.addActionListener(e -> actionAdd());
    addPanel.add(addButton);

    /**
     * Setting up the Downloads JTable on the JFrame.
     */
    tableModel = new DownloadTableModel();
    table = new JTable(tableModel);
    table.getSelectionModel().addListSelectionListener(e -> tableSelectionChanged());

    /**
     * Allow only one row at a time to be selected.
     */
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    /**
     * setting up the progress bar column on the JTable.
     */
    ProgressRenderer renderer = new ProgressRenderer(0,100);
    renderer.setStringPainted(true);
    table.setDefaultRenderer(JProgressBar.class, renderer);
    table.setRowHeight((int)renderer.getPreferredSize().getHeight());

    /**
     * Setting up the JPanel in which the JTable will be present.
     */
    JPanel  downloadsPanel = new JPanel();
    downloadsPanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
    downloadsPanel.setLayout(new BorderLayout());
    downloadsPanel.add(new JScrollPane(table),BorderLayout.CENTER);

    /*
     * Pause Button
     */
    pauseButton = new JButton("Pause");
    pauseButton.addActionListener(e -> actionPause());
    pauseButton.setEnabled(false);

    /*
     * Resume Button
     */

    resumeButton = new JButton("Resume");
    resumeButton.addActionListener(e -> actionResume());
    resumeButton.setEnabled(false);

    /*
     * cancel Button
     */

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> actionCancel());
    cancelButton.setEnabled(false);   

    /*
     * Clear Button
     */

    clearButton = new JButton("Clear");
    clearButton.addActionListener(e -> actionClear());
    clearButton.setEnabled(false);

    /**
     * adding all the buttons to the buttons panel
     */
    /**
     * Setting Up Buttons Panel.
     */
    JPanel buttonsPanel = new JPanel();
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
   * This action is triggered when a Download is selected in the JTable from the GUI.
   */

  private void tableSelectionChanged() {
    /**
     * Check if the none other Download is selected previously
     *  if so remove the DownloadManger as its observer
     */
    if (selectedDownload != null) {
      selectedDownload.deleteObserver(DownloadManager.this);
    }  
    /**
     * Add DownloadManager as the observer for the selected Download object 
     */
    
    if (!clearing && table.getSelectedRow() >  -1) {
      selectedDownload = tableModel.getDownload(table.getSelectedRow());
      selectedDownload.addObserver(DownloadManager.this);
      updateButtons();
    }
  }
  
  /**
   * Pause the download. 
   */
  
  private void actionPause() {
    selectedDownload.pause();
    updateButtons();

  }
  
  /**
   * Resume the download Once paused.
   */
  
  private void actionResume() {
    selectedDownload.resume();
    updateButtons(); 
  }
  
  /**
   * Clear the selected download from the JTable. 
   */
  
  private void actionClear() {
    clearing = true;
    tableModel.clearDownload(table.getSelectedRow());
    clearing = false;
    selectedDownload = null;
    updateButtons();
  }
  
  /**
   * Cancel the selected download from the JTable. 
   */
  
  private void actionCancel() {
    selectedDownload.cancel();
    updateButtons();
  }
  
  /**
   * Handles the Logic when the buttons should be enabled. 
   * and when they should be disabled at various.
   * states for the downloading file can get into.
   */
  
  private void updateButtons() {
    if (selectedDownload != null) {
      int status = selectedDownload.getStatus();
      switch (status) {
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
    } else {
      // No download is selected in table.
      pauseButton.setEnabled(false);
      resumeButton.setEnabled(false);
      cancelButton.setEnabled(false);
      clearButton.setEnabled(false);
    }
  } 

  /**
   * This action is triggered When the application is closed.
   * this terminates the program and stops all the threads
   */

  private void actionExit() {
    System.exit(0);       
  }

  /**
   * This action is triggered when the The JButton addButton is clicked.
   */

  private void actionAdd() {
    URL verifiedUrl = verifyURL(addTextField.getText());
    if (verifiedUrl != null) {
      tableModel.addDownload(new Download(verifiedUrl));
      addTextField.setText(""); // reset add text field
    } else {
      JOptionPane.showMessageDialog(this,"Invalid Download URL","Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * this method checks weather the URL entered in the text field is a HTTP/HTTPS protocol or not. 
   * @param url  extracted from the text field 
   * @return verifiedUrl
   */
  
  private URL verifyURL(String url) {
    /**
     *  Allow only HTTP and HTTPS URL's
     */
    if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
      return null;
    }  

    // Verify format of URL
    URL verifiedUrl = null;
    try {
      verifiedUrl = new URL(url);
    } catch (Exception e) {
      return null;
    }
    // Make sure URL specifies a file
    if (verifiedUrl.getFile().length() < 2) {
      return null;
    }  
    return verifiedUrl;
  }

  @Override
  /**
   * @short This method is fired whenever Download Object is 
   */
  public void update(Observable o, Object arg) {
    if (selectedDownload != null && selectedDownload.equals(o)) {
      SwingUtilities.invokeLater(() -> updateButtons());
    }  
  }
  
  /**
  * Main method the control flow starts form here.
  * @param args no command line args are required.
  */
  
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      DownloadManager downloadManager = new DownloadManager();
      downloadManager.setVisible(true);
    });
  }


}
