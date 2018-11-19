package download.manager;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
/**
 * this class helps in giving out the progress of the downloading file a part of JTable in the GUI.
 * @author Sreekar Reddy, C. Rohith, Althaf Md.
 *
 */
public class ProgressRenderer extends JProgressBar implements TableCellRenderer {

  /**
   * Constructor to the class.
   * @param min This value will always be zero
   * @param max This value will be the size of the downloading file
   */
  public ProgressRenderer(int min, int max) {
    super(min,max);
  }
  /**
   * Sets the value of the JProgress bar.
   * @return JprogressBar that gives the percentage of downloaded
   */

  public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus,
      int row, int column) {
    setValue((int) ((Float) value).floatValue());
    return this;
  }
}
