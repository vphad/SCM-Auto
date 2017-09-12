// Groovy script to rename job in Hudson
import hudson.model.*;

def NEW_PART = "_Old"

def job_to_update = Jenkins.instance.getItemByFullName('Test_Old')

println ("Updating job " + job_to_update.name);

    def new_job_name = job_to_update.name + NEW_PART; //Append new part to the job name
    println ("New name: " + new_job_name);
    job_to_update.renameTo(new_job_name);
    println ("Updated name: " + job_to_update.name);
    println("="*80);