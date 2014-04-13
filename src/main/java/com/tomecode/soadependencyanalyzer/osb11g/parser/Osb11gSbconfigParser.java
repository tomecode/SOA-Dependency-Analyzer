package com.tomecode.soadependencyanalyzer.osb11g.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.tomecode.soadependencyanalyzer.osb11g.assets.OsbAsset;
import com.tomecode.soadependencyanalyzer.osb11g.assets.OsbFolder;
import com.tomecode.soadependencyanalyzer.osb11g.assets.OsbProject;
import com.tomecode.soadependencyanalyzer.osb11g.assets.Relationship;
import com.tomecode.soadependencyanalyzer.osb11g.assets.SbConfig;

/**
 * (c) Copyright Tomecode.com, 2010. All rights reserved.
 * 
 * SBconfig parser for Oracle Service Bus 10g/11g
 * 
 * @author Tomas Frastia
 * @see http://www.tomecode.com
 *      http://code.google.com/p/bpel-esb-dependency-analyzer/
 */
public final class Osb11gSbconfigParser {

	private static final Logger log = Logger.getLogger(Osb11gSbconfigParser.class);

	private JarFile jarFile;
	private SbConfig project;
	private final Hashtable<OsbAsset, List<String>> dependencyTable;

	/**
	 * Constructor - initialize parsers
	 * 
	 * @throws IOException
	 */
	public Osb11gSbconfigParser(File jar) throws Exception {
		this.jarFile = new JarFile(jar);
		this.project = new SbConfig(jar);
		this.dependencyTable = new Hashtable<OsbAsset, List<String>>();
	}

	/**
	 * parse SBConfig file
	 * 
	 * @return
	 */
	public final SbConfig parse() {

		try {

			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (parseExportInfo(entry)) {
					break;
				}
			}
			resolveDependencies();
		} catch (Exception e) {
			log.error("Failed parse SBConfig[" + jarFile.getName() + "], reason: " + e.getMessage(), e);
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (Exception e) {
				}
			}
		}

		return project;
	}

	private final void resolveDependencies() {
		for (Entry<OsbAsset, List<String>> dep : this.dependencyTable.entrySet()) {
			for (String extRef : dep.getValue()) {

				OsbAsset depRef = this.project.findRef(extRef);
				dep.getKey().addDependency(new Relationship(dep.getKey(), depRef, extRef));
			}
		}
	}

	/**
	 * parse content of ExportInfo
	 * 
	 * @param entry
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private final boolean parseExportInfo(JarEntry entry) {
		try {
			Element element = parseXml(jarFile.getInputStream(entry));
			List<Element> exportedItemInfos = element.elements("exportedItemInfo");
			for (Element exportedItemInfo : exportedItemInfos) {

				String jarentryname = getEntryName(exportedItemInfo);
				// parse entry name
				ListIterator<String> it = Arrays.asList(jarentryname.split("/")).listIterator();
				//
				OsbAsset parent = null;
				while (it.hasNext()) {
					if (it.nextIndex() == 0) {
						parent = findOrCreateOsbProject(it.next());
					} else {
						String id = it.next();
						if (it.hasNext()) {
							parent = findOrCreateOsbFolder(parent, id);
						} else {
							findOrCreateOsbAsset(parent, id, exportedItemInfo);
						}
					}
				}

			}
		} catch (Exception e) {
			log.error("Failed parse Entry[" + entry.getName() + "], reason: " + e.getMessage(), e);
		}
		return true;
	}

	/**
	 * find or create OSB asset
	 * 
	 * @param parent
	 * @param entryName
	 * @param exportedItemInfo
	 */
	private final void findOrCreateOsbAsset(OsbAsset parent, String entryName, Element exportedItemInfo) { //
		Attribute typeId = exportedItemInfo.attribute("typeId");

		if (!"LocationData".equals(typeId.getValue())) {

			OsbAsset artifact = parent.findArtifact(entryName);
			if (artifact == null) {
				artifact = new OsbAsset(entryName); //

				Attribute instanceId = exportedItemInfo.attribute("instanceId");

				artifact.setInstanceId(instanceId.getValue());
				artifact.setTypeId(typeId.getValue());

				parent.addChild(artifact);

				List<String> deps = this.dependencyTable.get(artifact);
				if (deps == null) {
					this.dependencyTable.put(artifact, parsExtRef(exportedItemInfo));
				}

			}

		}
	}

	/**
	 * find or create {@link OsbFolder}
	 * 
	 * @param parent
	 * @param folderName
	 * @return
	 */
	private final OsbAsset findOrCreateOsbFolder(OsbAsset parent, String folderName) {
		OsbAsset child = parent.findArtifact(folderName);
		if (child == null) {
			child = new OsbFolder(folderName);
			parent.addChild(child);
		}
		return child;
	}

	/**
	 * find or create {@link OsbProject}
	 * 
	 * @param projectName
	 * @return
	 */
	private final OsbAsset findOrCreateOsbProject(String projectName) {
		OsbAsset parent = this.project.findArtifact(projectName);
		if (parent == null) {
			parent = new OsbProject(projectName);
			this.project.addChild(parent);
		}
		return parent;
	}

	/**
	 * return the entry name
	 * 
	 * @param exportedItemInfo
	 * @return
	 */
	private final String getEntryName(Element exportedItemInfo) {
		String name = parseProperty(exportedItemInfo, "jarentryname");
		int index = name.indexOf("/_projectdata.LocationData");
		if (index != -1) {
			name.substring(0, index);
		}
		index = name.indexOf("/_folderdata.LocationData");
		if (index != -1) {
			name.substring(0, index);
		}
		return name;
	}

	private final String parseProperty(Element exportedItemInfo, String attrName) {
		@SuppressWarnings("unchecked")
		List<Element> els = exportedItemInfo.element("properties").elements();
		for (Element e : els) {
			Attribute attribute = e.attribute("name");
			if (attribute != null) {
				if (attrName.equals(attribute.getValue())) {
					return e.attribute("value").getValue();
				}
			}
		}
		return null;
	}

	private final List<String> parsExtRef(Element exportedItemInfo) {
		List<String> extRefs = new ArrayList<String>();

		@SuppressWarnings("unchecked")
		List<Element> els = exportedItemInfo.element("properties").elements();
		for (Element e : els) {
			Attribute attribute = e.attribute("name");
			if (attribute != null) {
				if ("extrefs".equals(attribute.getValue())) {
					extRefs.add(e.attribute("value").getValue());
				}
			}
		}
		return extRefs;
	}

	private final Element parseXml(InputStream inputStream) throws Exception {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(inputStream);
		return document.getRootElement();
	}

}
