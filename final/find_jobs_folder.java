import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

import hudson.scm.*
import hudson.tasks.*
import com.cloudbees.hudson.plugins.folder.*

folderPath = 'DMTS/ECM-DM'

jen = Jenkins.instance.getItemByFullName(folderPath)
println "Url,Name,Current workspace,Updated Workspace"

jen.getItems().each{
	if(it instanceof Folder){
		processFolder(it)
	}else{
		processJob(it)
	}
}

void processJob(Item job){
	 def conf = job.getConfigFile().asString()
	 
	 if(conf.contains('MoodysParent')){
		 println job.getAbsoluteUrl() + "configure"
	 }
}

void processFolder(Item folder){
	folder.getItems().each{
		if(it instanceof Folder){
			processFolder(it)
		}else{
			processJob(it)
		}
	}
}

println ""