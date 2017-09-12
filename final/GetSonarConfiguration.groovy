/*
 Script to get maven & sonar configuration from Jenkins instance
 Author: Vijayanand Vitthal Phad
 */


import hudson.tasks.*
import hudson.maven.*
import jenkins.mvn.*

def output = "\n\nJob Name,Url,JDK,Maven,Maven Target,Custom Sonar"

for (job in Jenkins.instance.allItems) {

	if (!(job instanceof FreeStyleProject)) {
		println "Ignored - ${job.getFullName()} =>  Job is not FreeStyleProject, its a  " + job.getClass().getSimpleName()
		continue
	}

	if(job.isDisabled()){
		println "Ignored - ${job.getFullName()} =>  Job is Disabled"
		continue
	}

	def jobName = job.getFullName()
	def jobUrl = job.getAbsoluteUrl()

	def jdk = ""
	
	if(job.hasProperty('JDK') && null!=job.getJDK() && '' != job.getJDK().getName().trim()){
		jdk = job.getJDK().getName()
	}

	for (builder in job.builders) {
		if (builder instanceof Maven) {
			if(builder.targets.toLowerCase().contains("sonar")){
				output +=  "\n${jobName},${jobUrl},${jdk},${builder.mavenName},${builder.targets}"
			}
		}else if(builder instanceof hudson.tasks.Shell && builder.getCommand()?.contains('mvn')){
			output += "\n${jobName},${jobUrl},${jdk},,,${builder.getCommand()?.replaceAll("\n", " ").replaceAll(",", " ")}"
		}
	}
}

println output