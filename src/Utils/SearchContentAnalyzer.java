package Utils;

import java.util.ArrayList;
import java.util.List;

public class SearchContentAnalyzer {
	/**
	 * 针对数据库表查询内容进行处理
	 * @param content 从前台接收到的一整个查询内容，格式为 tablename columnname:content;columnname:content;..|..
	 * @return 
	 */
	public List<SearchContent> tableContentHandler(String content){
		
		StringHandlerUtil stringHandler = new StringHandlerUtil();
		content = stringHandler.removeEndCharacter(content);//移除最后一个无用的“|”
		String[] contentArray = stringHandler.String2Array(content, "\\|");//将接收到的内容进行切分“|”；分成多个表
		List<SearchContent> searchContentList = new ArrayList<SearchContent>();
		for(int i = 0;i<contentArray.length;i++){
			SearchContent searchContent = new SearchContent();
			String[] tableArray = stringHandler.String2Array(contentArray[i], "\\s");
			searchContent.setScope(tableArray[0]);//设置查询的对象表
			if(stringHandler.isEndCharacter(tableArray[1], ";")){
				tableArray[1] = stringHandler.removeEndCharacter(tableArray[1]);
			}
			searchContent.setContent(tableArray[1]);
			searchContentList.add(searchContent);
		}
		return searchContentList;
	}
}
