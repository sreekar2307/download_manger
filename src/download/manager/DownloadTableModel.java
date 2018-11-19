package download.manager;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JProgressBar;

import javax.swing.table.AbstractTableModel;

/**
 * @brief This class represents the model of the JTable present in the final GUI
 * @author Sreekar Reddy, C. Rohith, Althaf Md.
 */
public class DownloadTableModel extends AbstractTableModel implements Observer{

	public static final String[] columnNames = {"URL","Size","Progress","Status"}; ///< All the column names in the JTable in the GUI 

	private static final Class[] columnClasses = {String.class,String.class,JProgressBar.class,String.class}; ///< which class does each column belong in the JTable
	
	private ArrayList<Download> downloadList = new ArrayList<>(); ///< The ArrayList containing the list of currently downloading files in the JTable
	
    public void addDownload(Download download) {
    	download.addObserver(this);
    	downloadList.add(download);
    	
    	fireTableRowsInserted(getRowCount()-1,getRowCount()-1);
    }
    public Download getDownload(int row) {
    	return downloadList.get(row);
    }
    public void clearDownload(int row) {
    	downloadList.remove(row);
    	/**
    	 * Rectified using debugging tool
    	 */
    	fireTableRowsDeleted(row,row);
    }
	@Override
	public int getRowCount() {
		return downloadList.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	@Override
    public String getColumnName(int col) {
    	return columnNames[col];
    }
	@Override
    public Class<?> getColumnClass(int col){
    	return  columnClasses[col];
    }
	@Override
	public Object getValueAt(int row, int col) {
		Download download =downloadList.get(row);
		switch(col) {
		case 0: return download.getUrl();
		case 1: int size = download.getSize();
				return (size==-1)? "":Integer.toString(size); 
		case 2:return  Float.valueOf(download.getProgress());
		case 3:return Download.STATUSES[download.getStatus()];
		default: download.error(); 
		}
		return "";
	}

	@Override
	public void update(Observable o, Object arg) {
		
		int index = downloadList.indexOf(o);
		
		fireTableRowsUpdated(index,index);	
	}

}
