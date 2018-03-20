package Utils;

import java.util.List;

public class TableInfo {
	private String tableName;
	private List<List<TableColumns>> columnList;
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<List<TableColumns>> getColumn() {
		return columnList;
	}
	public void setColumn(List<List<TableColumns>> columnList) {
		this.columnList = columnList;
	}
}
