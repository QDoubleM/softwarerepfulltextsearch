package fulltextsearch;

import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.TopDocs;

public interface SearchHelper {
	public TopDocs queryPaser(String queryContent) throws IOException, ParseException;
	public TopDocs termQuery(String queryContent) throws IOException, ParseException;
	public void booleanQuery();
	public void multiTermQuery();
}
