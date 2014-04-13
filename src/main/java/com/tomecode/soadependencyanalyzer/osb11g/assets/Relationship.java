package com.tomecode.soadependencyanalyzer.osb11g.assets;

/**
 * (c) Copyright Tomecode.com, 2010. All rights reserved.
 * 
 * Relationship between artifacts
 * 
 * @author Tomas Frastia
 * @see http://www.tomecode.com
 *      http://code.google.com/p/bpel-esb-dependency-analyzer/
 */
public final class Relationship {

	private final OsbAsset source;

	private OsbAsset target;

	private final String extRef;

	public Relationship(OsbAsset source, OsbAsset target, String extRef) {
		this.source = source;
		this.extRef = extRef;
		this.target = target;
	}

	public final OsbAsset getSource() {
		return source;
	}

	public final OsbAsset getTarget() {
		return target;
	}

	public final void setTarget(OsbAsset target) {
		this.target = target;
	}

	public final String getExtRef() {
		return extRef;
	}

}
