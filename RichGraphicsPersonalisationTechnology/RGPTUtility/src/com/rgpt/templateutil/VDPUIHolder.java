package com.rgpt.templateutil;

import java.awt.geom.Rectangle2D;

public class VDPUIHolder {
	public VDPFieldInfo m_VDPFieldInfo;
	public Rectangle2D.Double m_UIBounds;

	public VDPUIHolder(VDPFieldInfo vdpField, Rectangle2D.Double uiBounds) {
		m_VDPFieldInfo = vdpField;
		m_UIBounds = uiBounds;
	}
}
