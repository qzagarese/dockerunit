package com.github.qzagarese.dockerunit.annotation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.qzagarese.dockerunit.annotation.ExtensionInterpreter;
import com.github.qzagarese.dockerunit.annotation.Volume;

public class VolumeExtensionInterpreter implements ExtensionInterpreter<Volume> {

    @Override
    public CreateContainerCmd build(CreateContainerCmd cmd, Volume v) {
    	Bind[] binds = cmd.getBinds();
    	
    	String hostPath = v.useClasspath() 
    			? Thread.currentThread().getContextClassLoader()
    					.getResource(v.host()).getPath()
    			: v.host();
    	
    	Bind bind = new Bind(hostPath, 
    			new com.github.dockerjava.api.model.Volume(v.container()), 
    			AccessMode.fromBoolean(v.accessMode().equals(Volume.AccessMode.RW)));
    	
    	List<Bind> bindsList = new ArrayList<>();
    	if(binds != null) {
    		bindsList.addAll(Arrays.asList(binds));
    	} 
   		bindsList.add(bind);  	
        return cmd.withBinds(bindsList);
    }

}
