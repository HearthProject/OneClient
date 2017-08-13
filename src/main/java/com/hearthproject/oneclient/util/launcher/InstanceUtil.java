package com.hearthproject.oneclient.util.launcher;

import com.hearthproject.oneclient.json.models.launcher.Instance;
import com.hearthproject.oneclient.json.models.launcher.InstanceList;

import java.util.ArrayList;

public class InstanceUtil {

	private static InstanceList instanceList;

	//TODO
	//Load from file
	public static InstanceList getInstances(){
		if(instanceList == null){
			instanceList = new InstanceList();
			instanceList.instances = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				instanceList.instances.add(createInstance("Example " + i));
			}
			return instanceList;
		}
		return instanceList;
	}

	private static Instance createInstance(String name) {
		Instance instance = new Instance();
		instance.name = name;
		return instance;
	}

}
