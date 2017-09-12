/*
 * Below script updates the maven configuration for the Jenkins job for Sonar upgrade activity
 * 
 * Author : Vijayanand Vitthal Phad
 */

import hudson.model.*
import hudson.maven.*
import hudson.tasks.*

// new maven to set
def newMavenName="Maven 3"

// Set to true for updates
def updateJobs = false

def output =  "Job Name,Job Url,JDK,Maven Name,Targets,Pom,Properties,JVM Options,Private Repo,Settings,Global Settings,Custom Script"

for(def item : Jenkins.instance.getAllItems(FreeStyleProject.class)) {

	// For test
	// for(def item : Jenkins.instance.getItemByFullName('some folder').getItems()) {
	try{
		if(!item.isDisabled() && item.hasProperty('builders') && (null != item.builders) ) {
			def updated = false

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

					def newProps = ""

					if(null!=jmProperties && ""!=jmProperties.trim()){
						def props  = new Properties()
						props.load(new StringReader(jmProperties))

						props.each{ k, v ->
							newProps += " -D" + k?.trim() + "=" + v?.trim()
						}
					}

					output += "\n${jobName},${jobUrl},${jdk},${jmMavenName},${jmTargetsText},${jmPom},${jmPropertiesText},${jmJVMOptions},${jmPrivateRepository},${jmSettings},${jmGlobamSettings}"

					if(updateJobs)
					{
						switch(jdk?.toLowerCase().trim()){
							case "jdk1.6":
							case "jdk1.7":
								def newCommandText = "/opt/mdp/app/bin/maven-sonar-scan -Dsonar.branch=\$SonarBranch " + ((jmPom!=null && jmPom.trim() !="") ? " -f ${jmPom}" : " " + (newProps!=null && newProps.trim()!="" ? newProps: " "))
								def newShell = new  hudson.tasks.Shell(newCommandText)

							// UNCOMMENT FOR UPDATE
							//								item.buildersList.replace(builder, newShell)
							//								updated = true

								output +=  "\n${jobName},${jobUrl},${jdk},,,${jmPom},${newProps},,,,,${newCommandText}"
								break

							case "jdk1.8":
							// Update maven if not Maven 3
								if(null!=jmMavenName &&  !newMavenName?.equalsIgnoreCase(jmMavenName?.trim())){
									def newMaven = new Maven(builder.targets, newMavenName ,builder.pom,builder.@properties,builder.jvmOptions,builder.usePrivateRepository,builder.settings,builder.globalSettings);

									// UNCOMMENT FOR UPDATE
									// item.buildersList.replace(builder, newMaven);
									// updated = true

									output += "\n${jobName},${jobUrl},${jdk},${newMavenName},${jmTargetsText},${jmPom},${jmPropertiesText},${jmJVMOptions},${jmPrivateRepository},${jmSettings},${jmGlobamSettings}"
								}
								break

							default:
								log += "\n Error: No JDK configured for Job - ${jobName} \nUrl - ${jobUrl}"
								break
						}

					}
				}else if(builder instanceof hudson.tasks.Shell){

					def cmdL = builder.getCommand()?.toLowerCase()

					// check for mvn & sonar, but not latest configuration
					if(cmdL?.contains('mvn') && cmdL?.contains('sonar')
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

						oldShell = builder

						output+=  "\n${jobName},${jobUrl},${jdk},,,${jmPom},,,,,,${commandText}"

						if(updateJobs){
							def newCommandText = "/opt/mdp/app/bin/maven-sonar-scan -Dsonar.branch=\$SonarBranch " + ((jmPom != null && jmPom.trim() != "") ? " -f ${jmPom}" : "")

							def newShell = new  hudson.tasks.Shell(newCommandText)

							output+= "\n${jobName},${jobUrl},${jdk},,,${jmPom},,,,,,${newCommandText}"

							// UNCOMMENT FOR UPDATE
							//							item.buildersList.replace(oldShell, newShell)
							//							updated = true
						}
					}
				}
			}

			if(updated && updateJobs){
				// Save Jenkins job if updated
				// item.save()
			}
		}


	}catch(Exception e){
		println "Failed to update maven-sonar configuration for job ${item.getFullName()},${item.getAbsoluteUrl()}  - " + e.getMessage()
		printStackTrace(e)
	}
}



println output
println "\n\nEnd" + ("-"*80)


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