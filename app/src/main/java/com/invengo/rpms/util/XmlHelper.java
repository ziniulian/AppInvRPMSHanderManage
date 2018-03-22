package com.invengo.rpms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.invengo.rpms.entity.*;

public class XmlHelper {

	// 从sd卡中读取配置文件
	public static List<EPCEntity> getAllGoods()
			throws ParserConfigurationException, SAXException, IOException {
		File f = new File(android.os.Environment.getExternalStorageDirectory()
				+ "/rfidsample/DataDic.xml"); // 后面是跟你在sd卡上存放的xml路径
		String path = f.getAbsolutePath();
		File myfile = new File(path);
		if (!myfile.exists()) {
			return null;
		}

		List<EPCEntity> listEntity = new ArrayList<EPCEntity>();
		FileInputStream fileIS = new FileInputStream(path);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(fileIS);
		Element root = document.getDocumentElement();
		NodeList nodes = root.getElementsByTagName("Goods");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node item = nodes.item(i);
			String sort = "";
			String tid = "";
			String sqe = "";
			String name = "";
			Node nodeSort = item.getAttributes().getNamedItem("Sort");
			if (nodeSort!=null) {
				sort = nodeSort.getNodeValue();
			}
			Node nodeName = item.getAttributes().getNamedItem("Name");
			if (nodeName!=null) {
				name = nodeName.getNodeValue();
			}
			Node nodeTid = item.getAttributes().getNamedItem("Tid");
			if (nodeTid!=null) {
				tid = nodeTid.getNodeValue();
			}
			Node nodeSqe = item.getAttributes().getNamedItem("Sqe");
			if (nodeSqe!=null) {
				sqe = nodeSqe.getNodeValue();
			}
			
			EPCEntity entity=new EPCEntity();
			entity.setEpcData(tid);
			listEntity.add(entity);
		}

		return listEntity;
	}

}
