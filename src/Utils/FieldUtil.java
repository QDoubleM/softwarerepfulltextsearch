package Utils;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexableField;


public interface FieldUtil {
	public IndexableField varCharOrChar(String fieldName,String fieldValue,Store storeType);
	public IndexableField stringField(String fieldName,String fieldValue,Store storeType);
	public IndexableField storedField(String fieldName,String fieldValue);
	public IndexableField textField(String fieldName,String fieldValue,Store storeType);
}
