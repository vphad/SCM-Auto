/*
*/

import hudson.model.*
import hudson.maven.*
import hudson.tasks.*
 
def newMaven="Maven-3.0.4-SQ"
 
Jenkins.instance.getAllItems().each{
item  ->

if(item.getFullName().contains('Sonar Upgrade') && (item instanceof FreeStyleProject) && !item.isDisabled() && item.hasProperty('builders') && (null != item.builders) )
{
		def updated = false

		// Find current recipients defined in project    
		for (builder in item.builders){
		   // println(">> "+builder);
		  
		  if ((builder instanceof Maven) && builder.targets.contains('install') && !builder.mavenName.toLowerCase().contains("sq"))  {
			  println("JOB : "+item.getFullName());
			 println("Url : "+item.getAbsoluteUrl());
			 println(">> MAVEN BUILDER");
			 println("  TARGETS : "+builder.targets);
			 println("  NAME : "+builder.mavenName);
			 println("  POM : "+builder.pom);
			// .properties is overridden by groovy
			 println("  PROPERTIES : "+builder.@properties);
			 println("  JVM-OPTIONS : "+ builder.jvmOptions);
			 println("  USE PRIVATE REPO : "+builder.usePrivateRepository);
			 println("  USER SETTINGS : "+builder.settings);
			 println("  GLOBAL SETTINGS : "+builder.globalSettings);
		   
		    def newBuilder = new Maven(builder.targets, newMaven ,builder.pom,builder.@properties,builder.jvmOptions,builder.usePrivateRepository,builder.settings,builder.globalSettings);
			item.buildersList.replace(builder, newBuilder);
			updated = true
		  }
		}
		// println("\n=======\n");

	if(updated){
	  item.save()
	}
}
}


println ""