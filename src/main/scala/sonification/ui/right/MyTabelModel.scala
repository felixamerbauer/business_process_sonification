package sonification.ui.right

import javax.swing.table.AbstractTableModel

class MyTableModel(initialData: Array[Array[String]], columns: Array[String]) extends AbstractTableModel {
  private var data = initialData

  override def getRowCount = data.length

  override def getColumnCount = columns.length

  override def getColumnClass(columnIndex: Int) = classOf[String]

  override def getColumnName(columnIndex: Int) = columns(columnIndex)

  override def getValueAt(rowIndex: Int, columnIndex: Int) = data(rowIndex)(columnIndex)

  def update(newData: Array[Array[String]]) {
    //    logger.debug(s"update ${newData.map(_.mkString(",")).mkString(" -> ")}")
    data = newData
    fireTableDataChanged()
  }
}
