def folder = Jenkins.instance.getItemByFullName('BFM/BFM/1254469')

key="Label"
value=""
desc =""

for(job in folder.items) {
  
  if(job instanceof com.cloudbees.hudson.plugins.folder.Folder){
    continue
  }

    println("[ " + job.name + " ] setting " + key + "=" + value)

    newParam = new StringParameterDefinition(key, value, desc)
    paramDef = job.getProperty(ParametersDefinitionProperty.class)

    if (paramDef == null) {
        newArrList = new ArrayList<ParameterDefinition>(1)
        newArrList.add(newParam)
        newParamDef = new ParametersDefinitionProperty(newArrList)
        job.addProperty(newParamDef)
    }
    else {
        // Parameters exist! We should check if this one exists already!
        found = paramDef.parameterDefinitions.find{ it.name == key }
        if (found == null) {
            paramDef.parameterDefinitions.add(newParam)
        }
    }
	
	if(job.getScm()!=null && job.getScm() instanceof hudson.scm.IntegritySCM){
		println "Updating password for ${job.getName()}"
		job.getScm().setPassword("phacBad#0cewutyab")
		
		job.getScm().setConfigPath(job.getScm().getConfigPath().replace("d=1254469",'b=${env[\'Label\']}'))
  }
    
  
  job.save()
  println()
}
