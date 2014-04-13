package com.tomecode.soadependencyanalyzer.osb11g.assets;

/**
 * (c) Copyright Tomecode.com, 2010. All rights reserved.
 * 
 * Project in SBConfig
 * 
 * @author Tomas Frastia
 * @see http://www.tomecode.com
 *      http://code.google.com/p/bpel-esb-dependency-analyzer/
 */
public final class OsbProject extends OsbAsset {

	public OsbProject(String id) {
		super(id);
	}

	public final OsbFolder findOrCreateFolder(String folderName) {
		for (OsbAsset child : this.childs) {
			if (child.getId().equals(folderName)) {
				return (OsbFolder) child;
			}
		}

		OsbFolder osbArtifact = new OsbFolder(folderName);
		this.childs.add(osbArtifact);
		return osbArtifact;
	}

}
