jen=Jenkins.instance

output = "Job Url,Job Name,Custom Workspace"
jen.getAllItems().each{
	  if(it.hasProperty("customWorkspace") && it.getCustomWorkspace()!=null && it.getCustomWorkspace().trim() !="") {
		output += "\n${it.getAbsoluteUrl()}configure,${it.getFullName()},${it.getCustomWorkspace()}"
	  }
}

