/*
 * Below script reads all the jobs and it's Sonar configuration
 * 
 * Author : Vijayanand Vitthal Phad
 */

import hudson.model.*
import hudson.maven.*
import hudson.tasks.*

def output =  "Job Name,Job Url,Node,Disabled,JDK,Maven Name,Targets,Pom,Properties,JVM Options,Private Repo,Settings,Global Settings,Custom Script"

for(def item : Jenkins.instance.getAllItems(FreeStyleProject.class)) {

	try{
			if(!item.isDisabled() && item.hasProperty('builders') && (null != item.builders)) {
			def updated = false

			def assignedNode = item.getAssignedLabel()?.getName()

			def isDisabled = item.isDisabled()


			// get build steps
			for (builder in item.builders){

				// sonar
				if ((builder instanceof hudson.tasks.Maven) && builder.targets.toLowerCase().contains('sonar'))  {

					def jdk = getJDK(item)

					def jobName = item.getFullName();
					def jobUrl =  item.getAbsoluteUrl();

					def jmTargets =  builder.targets;
					def jmTargetsText = jmTargets?.replaceAll("(\\r|\\n|\\r\\n)+", " \\\\n ")

					def jmMavenName =  builder.mavenName;
					def jmPom =  builder.pom;

					def jmProperties =  builder.@properties;
					def jmPropertiesText = jmProperties?.replaceAll("(\\r|\\n|\\r\\n)+", " \\\\n ")

					def jmJVMOptions =  builder.jvmOptions;
					def jmPrivateRepository =  builder.usePrivateRepository;
					def jmSettings =  builder.settings;
					def jmGlobamSettings =  builder.globalSettings;

					output += "\n${jobName},${jobUrl},${assignedNode},${isDisabled},${jdk},${jmMavenName},${jmTargetsText},${jmPom},${jmPropertiesText},${jmJVMOptions},${jmPrivateRepository},${jmSettings},${jmGlobamSettings}"
				}
				else if(builder instanceof hudson.tasks.Shell){

					def cmdL = builder.getCommand()?.toLowerCase()

					if(cmdL?.contains('/opt/mdp/app/bin/maven-sonar-scan')){
						def jdk = getJDK(item)

						def jobName = item.getFullName();
						def jobUrl =  item.getAbsoluteUrl();

						def command = builder.getCommand()

						def commandText = command?.replaceAll("(\\r|\\n|\\r\\n)+",  " \\\\n ")

						def pomRegEx = command =~ /.*-f\s+(.*pom.xml)?\s*.*/

						def jmPom = ""

						if(pomRegEx.count>0){
							// this is to verify if multiple commands added in one shell
							jmPom += ("," + pomRegEx.getAt(0)[1])
						}

						// remove the first ,
						jmPom = jmPom.minus(",")

						output+=  "\n${jobName},${jobUrl},${assignedNode},${isDisabled},${jdk},,,${jmPom},,,,,,${commandText}"
					}
					// check for mvn & sonar, but not latest configuration
					else if(cmdL?.contains('mvn') && cmdL?.contains('sonar')
					&& !cmdL?.contains('/opt/mdp/app/bin/maven-sonar-scan')){
						def jdk = getJDK(item)

						def jobName = item.getFullName();
						def jobUrl =  item.getAbsoluteUrl();

						def command = builder.getCommand()

						def commandText = command?.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n")

						def pomRegEx = command =~ /.*-f\s+(.*pom.xml)?\s*.*/

						def jmPom = ""

						if(pomRegEx.count>0){
							// this is to verify if multiple commands added in one shell
							jmPom += ("," + pomRegEx.getAt(0)[1])
						}

						// remove the first ,
						jmPom = jmPom.minus(",")

						output+=  "\n${jobName},${jobUrl},${assignedNode},${isDisabled},${jdk},,,${jmPom},,,,,,${commandText}"
					}
				}

				else if(builder instanceof hudson.tasks.BatchFile){

					def cmdL = builder.getCommand()?.toLowerCase()

					if(cmdL?.contains('maven-sonar-scan.cmd ')){
						def jdk = getJDK(item)

						def jobName = item.getFullName();
						def jobUrl =  item.getAbsoluteUrl();

						def command = builder.getCommand()

						def commandText = command?.replaceAll("(\\r|\\n|\\r\\n)+",  " \\\\n ")

						def pomRegEx = command =~ /.*-f\s+(.*pom.xml)?\s*.*/

						def jmPom = ""

						if(pomRegEx.count>0){
							// this is to verify if multiple commands added in one shell
							jmPom += ("," + pomRegEx.getAt(0)[1])
						}

						// remove the first ,
						jmPom = jmPom.minus(",")

						output+=  "\n${jobName},${jobUrl},${assignedNode},${isDisabled},${jdk},,,${jmPom},,,,,,${commandText}"
					}
				}
			}

		}

	}catch(Exception e){
		println "Failed to get maven-sonar configuration for job ${item.getFullName()},${item.getAbsoluteUrl()}  - " + e.getMessage()
		printStackTrace(e)
	}
}


println output


String printStackTrace(Exception e){
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	println sw.toString();

}

def getJDK(def item){
	def jdk = ""
	if(item.hasProperty('JDK') && null!=item.getJDK() && '' != item.getJDK().getName().trim()){
		jdk = item.getJDK().getName()
	}

	return jdk
}