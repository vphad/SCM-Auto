import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

import hudson.scm.*
import hudson.tasks.*
import com.cloudbees.hudson.plugins.folder.*

folderPath = 'Ratings/AccuRate/Ratings-2017-MNE-Q3(466)'

jen = Jenkins.instance.getItemByFullName(folderPath)

jen.getItems().each{
	if(it instanceof Folder){
		processFolder(it)
	}else{
		processJob(it)
	}
}

void processJob(Item job){
  if(job.isDisabled()){
    return
  }
	 def conf = job.getConfigFile().asString()
  
  println "\n\n---------\n" + job.getFullName()
  def svn =  job.getScm().getLocations().remote
  
  def sr = svn =~ /\{SVNBranch\}\/(.*)?\@.*/
  
 if(sr.getCount() > 0){
  	println "cd " + sr.getAt(0)[1]
  }


 for (builder in job.builders){
				if(builder instanceof hudson.tasks.Shell || builder instanceof hudson.tasks.BatchFile){
					cmdText = builder.getCommand()?.toLowerCase()

					if(!cmdText?.contains('mdppkg') && !cmdText?.contains('sonar-runner') && !cmdText?.contains('symstore')){
							println cmdText
					}
				}
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