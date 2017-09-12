import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*

import hudson.scm.*
import hudson.tasks.*
import com.cloudbees.hudson.plugins.folder.*

jen = Jenkins.instance

jen.getItemByFullName('/Ratings/AccuRate/Ratings-2017-Project-JBoss-Upgrade-R1(62)/').getItems().each{
	if(it instanceof Folder){
		processFolder(it)
	}else{
		processJob(it)
	}
}

void processJob(Item job){
  def output = job.getFullName() + ","
  prop = job.getProperty(ParametersDefinitionProperty.class)
  
  if(prop != null) {
    
    // println("--- Parameters for " + item.name + " ---")    
    for(param in prop.getParameterDefinitions()) {
      try {
         output += ( "," + param.name  + "," + param.defaultValue)
      }
      catch(Exception e) {
        // println(param.name)
      }
    }

}
  
  println output
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