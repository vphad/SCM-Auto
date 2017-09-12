/*
 * Below script finds SonarQube configuration in Jenkins
 * 
 * Author : Vijayanand Vitthal Phad
 * 
 */

import jenkins.*;
import jenkins.model.*
import hudson.model.*
import hudson.maven.*
import hudson.tasks.*


def output =  "Job,Sonar Configured,Command Text"

for(def item : Jenkins.instance.getAllItems(FreeStyleProject.class)) {
		if(item.getFullName().toLowerCase().contains("ahs") 
			|| item.getFullName().toLowerCase().contains("mdp")
			||  item.isDisabled() 
			|| (item.hasProperty('builders') && (null == item.builders || item.builders.isEmpty()))
			|| (item.hasProperty('scm') && !(item.getScm() instanceof hudson.scm.SubversionSCM))
			) {
			continue
		}
		
			found = false
			
			cmdText = ""
			
			// get build steps
			for (builder in item.builders){
				if(builder instanceof hudson.tasks.Shell || builder instanceof hudson.tasks.BatchFile){
					def cmdL = builder.getCommand()?.toLowerCase()
					if(cmdL?.contains('sonar-runner') || cmdL?.contains('maven-sonar-scan')){
						cmdText = cmdL
						found = true
					}
				}
			}
		
		output += "\n${item.getFullName()},${item.getAbsoluteUrl()}configure,${found},${cmdText.replaceAll(',',' ')?.replaceAll("\\r?\\n", " --linebreak-- ")}"
}


println "-" * 80

println output
println "\n\nEnd" + ("-"*80)