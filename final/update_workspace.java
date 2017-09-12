import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

import hudson.scm.*
import hudson.tasks.*
import com.cloudbees.hudson.plugins.folder.*

folderPath = 'Ratings/AccuRate/Ratings-2017-MNE-Q3(466)_ARA'
oldVal = "466"
newVal = "536"

// set to true to update the workspace; else keep it to false for report
update = false

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
	 if(job.hasProperty("customWorkspace") && job.getCustomWorkspace()!=null && job.getCustomWorkspace().trim() !="") {
		 def old = job.getCustomWorkspace().trim()
		 def updatePath = old.replace(oldVal, newVal)
		 
		 if(update){
			job.setCustomWorkspace(updatePath)
			job.save()
		 }
		println "${job.getAbsoluteUrl()}configure,${job.getFullName()},${old},${updatePath}"
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