package com.hearthproject.oneclient.fx.nodes;

import com.hearthproject.oneclient.util.curse.CurseElement;
import com.hearthproject.oneclient.util.curse.CurseUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class CurseMod extends CurseTile {
	public CurseMod(CurseElement element) {
		super(element);
	}

	@Override
	public void install() {
		try {
			String s = CurseUtils.getLocationHeader(element.getUrl());
			String a = s + "/files/latest/download";
			String b = CurseUtils.getLocationHeader(a);
			System.out.println(b);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
}
