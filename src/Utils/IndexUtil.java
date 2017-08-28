package Utils;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

public class IndexUtil {
	private IndexWriter indexWriter;
	private Document document;
	public void createIndex(Field field){
		if(indexWriter != null){
			document.add(field);
		}
	}
	public void closeIndexWiter(){
		try {
			indexWriter.commit();
			indexWriter.close();
			indexWriter = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
