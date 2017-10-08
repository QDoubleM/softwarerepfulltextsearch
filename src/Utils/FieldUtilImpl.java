package Utils;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexableField;

public class FieldUtilImpl implements FieldUtil {

	@Override
	public IndexableField varCharOrChar(String fieldName, String fieldValue,Store storeType) {
		
		Field textField = new TextField(fieldName, fieldValue,storeType);
		return textField;
		
	}

	@Override
	public IndexableField stringField(String fieldName, String fieldValue, Store storeType) {
		
		Field stringField = new StringField(fieldName, fieldValue,storeType);
		return stringField;
	}

	@Override
	public IndexableField storedField(String fieldName, String fieldValue) {
		
		Field storedField = new StoredField(fieldName,fieldValue);
		return storedField;
	}

	@Override
	public IndexableField textField(String fieldName, String fieldValue, Store storeType) {
		
		Field textField = new TextField(fieldName, fieldValue,storeType);
		return textField;
	}
	
	@Override
	public IndexableField intField(String fieldName, int fieldValue,Store storeType) {
		
		Field intField = new IntField(fieldName, fieldValue, storeType);
		return intField;
	}
	
	@Override
	public IndexableField longField(String fieldName, long fieldValue,Store storeType) {
		
		Field longField = new LongField(fieldName, fieldValue, storeType);
		return longField;
	}
}
