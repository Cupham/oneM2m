package org.eclipse.om2m.sample.echonet.echonetObjects;

import echowand.common.EOJ;
import echowand.net.Node;

public class NodeProfileObject extends eSuperClass{
	public NodeProfileObject() {
		super();
	}

	public NodeProfileObject(Node node, String name) {
		super(node, name);
	}
	public NodeProfileObject(Node node, EOJ eoj) {
		super(node, eoj);
	}
	

}
