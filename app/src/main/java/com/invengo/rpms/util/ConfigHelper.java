package com.invengo.rpms.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
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
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;

import com.invengo.rpms.entity.*;

public class ConfigHelper {

	// 配置信息
	private ConfigEntity config_info = new ConfigEntity();

	/**
	 * 构造函数
	 */
	public ConfigHelper() {
		boolean ok;
		File sd_path;
		File file_cfg_dir;
		File file_cfg;
		FileOutputStream out;
		String str;
		FileInputStream in;

		// 获取SD卡目录
		sd_path = Environment.getExternalStorageDirectory();
		// 判断文件夹是否存在
		file_cfg_dir = new File(sd_path.getPath() + "//RPMS");
		if (!file_cfg_dir.exists() && !file_cfg_dir.isDirectory()) {
			ok = file_cfg_dir.mkdirs();
		}
		// 判断配置文件是否存在
		file_cfg = new File(file_cfg_dir.getPath(), "cfg.xml");
		if (!file_cfg.exists()) {
			System.out.println("配置文件cfg.xml不存在!");
			try {
				file_cfg.createNewFile();
				System.out.println("创建文件cfg.xml成功!");

				// 生成初始化的配置数据
				try {
					out = new FileOutputStream(file_cfg);

					// 保存默认配置
					config_info.RoleAdmin = "1,2,3,4,5,6,7,8,9,10,11,12,13";
					config_info.Role1 = "1,2,3,4,5,13";
					config_info.Role2 = "1,6,7,8,13";
					config_info.Role3 = "1,9,10,11,12,13";
					config_info.Role4 = "1,11,12,13";
					config_info.Role5 = "1,13";
					str = produce_xml_string(config_info);

					out.write(str.getBytes());
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// 解析xml文件
		try {
			in = new FileInputStream(file_cfg);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(in);
			// 获取根节点
			Element root = document.getDocumentElement();
			NodeList node = root.getChildNodes();
			if (node.getLength() > 5) {
				config_info.RoleAdmin = node.item(0).getFirstChild()
						.getNodeValue();
				config_info.Role1 = node.item(1).getFirstChild()
						.getNodeValue();
				config_info.Role2 = node.item(2).getFirstChild()
						.getNodeValue();
				config_info.Role3 = node.item(3).getFirstChild()
						.getNodeValue();
				config_info.Role4 = node.item(4).getFirstChild()
						.getNodeValue();
				config_info.Role5 = node.item(5).getFirstChild()
						.getNodeValue();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ConfigEntity get_config_info() {
		return config_info;
	}

	/**
	 * 生成xml配置文件的String数据流 Config_Info的本机ip信息不会保存
	 * 
	 * @param info
	 *            :配置信息
	 * @return xml的String数据流
	 */
	private String produce_xml_string(ConfigEntity info) {

		StringWriter stringWriter = new StringWriter();

		try {
			// 获取XmlSerializer对象
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlSerializer xmlSerializer = factory.newSerializer();
			// 设置输出流对象
			xmlSerializer.setOutput(stringWriter);

			// 开始标签
			xmlSerializer.startDocument("utf-8", true);
			xmlSerializer.startTag(null, "config");

			// 配置信息
			xmlSerializer.startTag(null, "RoleAdmin");
			xmlSerializer.text(info.RoleAdmin);
			xmlSerializer.endTag(null, "RoleAdmin");
			
			// 配置信息
			xmlSerializer.startTag(null, "Role1");
			xmlSerializer.text(info.Role1);
			xmlSerializer.endTag(null, "Role1");
			
			// 配置信息
			xmlSerializer.startTag(null, "Role2");
			xmlSerializer.text(info.Role2);
			xmlSerializer.endTag(null, "Role2");
			
			// 配置信息
			xmlSerializer.startTag(null, "Role3");
			xmlSerializer.text(info.Role3);
			xmlSerializer.endTag(null, "Role3");
			
			// 配置信息
			xmlSerializer.startTag(null, "Role4");
			xmlSerializer.text(info.Role4);
			xmlSerializer.endTag(null, "Role4");
			
			// 配置信息
			xmlSerializer.startTag(null, "Role5");
			xmlSerializer.text(info.Role5);
			xmlSerializer.endTag(null, "Role5");

			xmlSerializer.endTag(null, "config");
			xmlSerializer.endDocument();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringWriter.toString();
	}
}
