package Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class TikaUtil {
	
	public static void main(String[] args) throws Exception{
		fileToTxt();
	}
	
	public static void fileToTxt() throws Exception {
		// 使用AutoDetectParser可以使用Tika根据实际情况自动转换需要使用的parser,不需要再手工指定
		Parser tikaParser = new AutoDetectParser();
		File file = new File("E:/findfile/gaigemingzikankan/新建文件夹/lucene1.doc");
		InputStream is = new FileInputStream(file);
		/*
		 * / 参数的含义为： inputStream：文件输入流
		 * ContentHandler：所有解析出来的内容会放到它的子类BodyContentHandler中 Metadata
		 * ：Tika解析文档时会生成的一组说明数据 ParseContext:用来存储需要填入的参数,最少需要设置tikaParser本身 *
		 */
		ContentHandler handler = new BodyContentHandler();

		Metadata metadata = new Metadata();
		/**
		 * 设定文档名称， 对于deprecate属性的内容表示无法通过在metadata中设置进行修改
		 **/
		metadata.add(Metadata.RESOURCE_NAME_KEY, file.getName());// 可以用来设定文档名称
		
		ParseContext parseContext = new ParseContext();
		//parseContext.set(Parser.class, tikaParser);
		tikaParser.parse(is, handler, metadata, parseContext);
		System.out.println(handler.toString());
		System.out.println("---------------------------");
		for (String name : metadata.names()) {
			System.out.println(name + "-->" + metadata.get(name));
			
		}
	}
	
}
