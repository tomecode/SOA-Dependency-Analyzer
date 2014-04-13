package com.tomecode.soadependencyanalyzer.osb11g.assets;

import java.io.File;

/**
 * (c) Copyright Tomecode.com, 2010. All rights reserved.
 * 
 * SBConfig
 * 
 * @author Tomas Frastia
 * @see http://www.tomecode.com
 *      http://code.google.com/p/bpel-esb-dependency-analyzer/
 */
public final class SbConfig extends OsbAsset {

	private final File jarFile;

	public SbConfig(File jarFile) {
		super(jarFile.getName());
		this.jarFile = jarFile;
	}

	public final File getFile() {
		return jarFile;
	}

	public final OsbProject findOrOsbProject(String projectName) {
		for (OsbAsset child : this.childs) {
			if (child.getId().equals(projectName)) {
				return (OsbProject) child;
			}
		}

		OsbProject osbArtifact = new OsbProject(projectName);
		this.childs.add(osbArtifact);
		return osbArtifact;
	}

}
