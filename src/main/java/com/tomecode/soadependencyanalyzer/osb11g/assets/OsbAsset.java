package com.tomecode.soadependencyanalyzer.osb11g.assets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * (c) Copyright Tomecode.com, 2010. All rights reserved.
 * 
 * Basic Artifact
 * 
 * @author Tomas Frastia
 * @see http://www.tomecode.com
 *      http://code.google.com/p/bpel-esb-dependency-analyzer/
 */
public class OsbAsset {

	private final String id;

	private String typeId;

	private String instanceId;

	// TREEE
	protected final Set<OsbAsset> childs;

	// dependencies
	private Set<Relationship> depenencies;

	/**
	 * OsbArtifact
	 * 
	 * @param id
	 */
	public OsbAsset(String id) {
		this.id = id;
		this.childs = new HashSet<OsbAsset>();
		this.depenencies = new HashSet<Relationship>();
	}

	public final Set<Relationship> getDepenencies() {
		return depenencies;
	}

	public final void setDepenencies(Set<Relationship> depenencies) {
		this.depenencies = depenencies;
	}

	public final String getId() {
		return this.id;
	}

	public final Set<OsbAsset> getChilds() {
		return childs;
	}

	public final OsbAsset findArtifact(String artifactName) {
		for (OsbAsset child : this.childs) {
			if (child.getId().equals(artifactName)) {
				return child;
			}
		}
		return null;
	}

	public final void addChild(OsbAsset osbArtifact) {
		this.childs.add(osbArtifact);
	}

	public final OsbAsset findOrCreateArtifact(String artifactName) {
		Iterator<OsbAsset> iterator = this.childs.iterator();
		while (iterator.hasNext()) {
			OsbAsset artifact = iterator.next();
			if (artifact.getId().equals(artifactName)) {
				return artifact;
			}
		}
		OsbAsset osbArtifact = new OsbAsset(artifactName);
		this.childs.add(osbArtifact);
		return osbArtifact;
	}

	public void print(OutputStream os) throws IOException {
		printArtifacts("", true, os);
		os.write(("ID: " + id + "\n").toString().getBytes());

		for (OsbAsset osbArtifact : childs) {
			osbArtifact.print(os);
		}
	}

	private final void printArtifacts(String prefix, boolean isTail, OutputStream outputStream) throws IOException {
		outputStream.write((prefix + (isTail ? "└── " : "├── ") + getId()).getBytes());
		outputStream.write("\n".getBytes());

		List<OsbAsset> listElements = new ArrayList<OsbAsset>(childs);
		for (int i = 0; i < listElements.size() - 1; i++) {
			listElements.get(i).printArtifacts(prefix + (isTail ? "    " : "│   "), false, outputStream);
		}
		if (listElements.size() >= 1) {
			listElements.get(childs.size() - 1).printArtifacts(prefix + (isTail ? "    " : "│   "), true, outputStream);
		}
	}

	public final String toString() {
		return getId();
	}

	public final void addDependency(Relationship relationship) {
		this.depenencies.add(relationship);
	}

	public final String getTypeId() {
		return typeId;
	}

	public final void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public final String getInstanceId() {
		return instanceId;
	}

	public final void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public final OsbAsset findRef(String extRef) {
		String typeId = extRef.substring(0, extRef.indexOf("$"));
		String instanceId = extRef.substring(extRef.indexOf("$") + 1).replace("$", "/");

		return findRef2(instanceId, typeId);
	}

	private final OsbAsset findRef2(String searchInstanceId, String searchTypeId) {
		if (this.instanceId != null && this.instanceId.equals(searchInstanceId) && this.typeId != null && this.typeId.equals(searchTypeId)) {
			return this;
		}
		for (OsbAsset child : this.childs) {
			OsbAsset extRefChild = child.findRef2(searchInstanceId, searchTypeId);
			if (extRefChild != null) {
				return extRefChild;
			}
		}
		return null;
	}

	public final List<OsbAsset> findServiceByTypeId(String typeId) {
		List<OsbAsset> osbArtifacts = new ArrayList<OsbAsset>();
		findByTypeId(typeId, osbArtifacts);
		return osbArtifacts;
	}

	public final List<String> findServiceTypes() {
		List<String> types = new ArrayList<String>();
		findAllTypes(types);
		return types;
	}

	private final void findAllTypes(List<String> types) {
		if (typeId != null) {
			if (!types.contains(typeId)) {
				types.add(typeId);
			}
		}

		for (OsbAsset child : this.childs) {
			child.findAllTypes(types);
		}
	}

	public final List<OsbAsset> findAllArtifacts() {
		List<OsbAsset> osbArtifacts = new ArrayList<OsbAsset>();
		for (String typeId : findServiceTypes()) {
			findByTypeId(typeId, osbArtifacts);
		}
		return osbArtifacts;
	}

	public final void findByTypeId(String searchTypeId, List<OsbAsset> osbArtifacts) {
		if (this.typeId != null && this.typeId.equals(searchTypeId)) {
			osbArtifacts.add(this);
		}
		for (OsbAsset child : this.childs) {
			child.findByTypeId(searchTypeId, osbArtifacts);
		}
	}
}
