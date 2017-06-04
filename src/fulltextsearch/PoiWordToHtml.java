package fulltextsearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.converter.core.BasicURIResolver;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class PoiWordToHtml implements WordsToHtml {
	
	@Override
	public void WordToHtml(String wordfilepath, String htmlfilepath)throws Exception {
		
		String filename = new File(wordfilepath).getName();
		htmlfilepath = htmlfilepath+"/"+filename.substring(0, filename.lastIndexOf("."))+".html";
		
		File wordfile = new File(wordfilepath);
		try {
			if (wordfile.getName().endsWith("doc")||wordfile.getName().endsWith("wps")) 		
				docToHtml(wordfilepath, htmlfilepath);
			else if (wordfile.getName().endsWith("docx")) 
				docxToHtml(wordfilepath, htmlfilepath);
			System.out.println("转换完成");
			} catch (TransformerException e) {
			e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}	
	}
	
	public static void docToHtml(String docfilepath, String htmlfilepath)throws TransformerException, IOException,ParserConfigurationException {

		final String parentpath = new File(htmlfilepath).getParent();
		final String filename = new File(docfilepath).getName();
		HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(docfilepath));
		WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());	
		
		wordToHtmlConverter.setPicturesManager(new PicturesManager() {  
			public String savePicture(byte[] content, PictureType pictureType,String suggestedName, float widthInches, float heightInches) {
				FileOutputStream fileoutputstream = null;
				suggestedName = filename.substring(0, filename.lastIndexOf("."))+ suggestedName;
				try {
					fileoutputstream = new FileOutputStream(parentpath +"/"+ suggestedName);
					fileoutputstream.write(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return suggestedName;//返回img标签属性
			}
		});
		wordToHtmlConverter.processDocument(wordDocument);
		
		OutputStream outputstream = new FileOutputStream(new File(htmlfilepath));
		DOMSource domSource = new DOMSource(wordToHtmlConverter.getDocument());
		StreamResult streamResult = new StreamResult(outputstream);
		
		Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.setOutputProperty(OutputKeys.METHOD, "html");
		serializer.transform(domSource, streamResult);
		outputstream.close();
	}
	
	public static void docxToHtml(String docxFilePath,String htmlFilePath) throws Exception {
		String docxImagesPath = new File(htmlFilePath).getParent();
		OutputStreamWriter outputStreamWriter = null;
		try {
			XWPFDocument document = new XWPFDocument(new FileInputStream(docxFilePath));
			
			XHTMLOptions options = XHTMLOptions.create().indent(4);			
			options.setExtractor(new FileImageExtractor(new File(docxImagesPath)));	//保存图片		
			options.URIResolver(new BasicURIResolver("."));//设置<img>标签属性
			
			outputStreamWriter = new OutputStreamWriter(new FileOutputStream(htmlFilePath), "utf-8");
			XHTMLConverter xhtmlConverter = (XHTMLConverter) XHTMLConverter.getInstance();
			xhtmlConverter.convert(document, outputStreamWriter, options);
		} finally {
			if (outputStreamWriter != null) {
				outputStreamWriter.close();
			}
		}
	}

		
}
