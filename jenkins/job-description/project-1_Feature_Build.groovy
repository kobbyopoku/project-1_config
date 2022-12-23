def scripts = """

def job_config = [
    job: [
        name: "project-1_Feature_Build",
        agent: "maven"
    ]
]
def config = [
    git: [
         protocol: "https",
         server: "github.developer.allianz.io",
         credentialsId: "git-token-credentials"
     ]
]
ghe: [
        //commitMsgPattern: '/[A-Z]{2,10}-[0-9]+:.*/',
        commitMsgPattern: '.*',
        apiUrl: 'https://github.developer.allianz.io/api/v3',
        GitGroup: 'kobbyopoku',
        RepoName: 'project-1',
        checks: [
           // [name: "di/base-branch-validation", description: "requested (or skipped if previous failed)"],
            [name: "scm/commit-validation", description: "requested (or skipped if previous failed)", type: "commit_validation"],
            [name: "scm/pr-validation",description: "requested (or skipped if previous failed)", type: "prname_validation"]
           // [name: "scm/branchname-validation",description: "requested (or skipped if previous failed)", type: "branchname_validation"],
            //[name: "di/target-branch-validation",description: "requested (or skipped if previous failed)", type: "commit_validation"]
           // [name: "di/checkout-and-merge",description: "requested (or skipped if previous failed)", type: "commit_validation"],
            //[name: "di/merge-ready",description: "requested (or skipped if previous failed)", type: "commit_validation"]
        ]
    ]
           
def lib = library identifier: 'BizDevOps_JSL@develop', retriever: modernSCM( 
  [\$class: 'GitSCMSource',
  remote: 'https://github.developer.allianz.io/JEQP/BizDevOps-JSL.git',
  credentialsId: 'git-token-credentials']) 
  
def customLib = library identifier: 'project-1_JSL@develop', retriever: modernSCM(
  [\$class: 'GitSCMSource',
   remote: 'https://github.developer.allianz.io/kobbyopoku/project-1_lib.git',
   credentialsId: 'git-token-credentials']) 

def jslGeneral    = lib.de.allianz.bdo.pipeline.JSLGeneral.new()
def jslGit        = lib.de.allianz.bdo.pipeline.JSLGit.new()
def jslMaven      = lib.de.allianz.bdo.pipeline.JSLMaven.new()
def jslGhe        = lib.de.allianz.bdo.pipeline.JSLGhe.new()

def jslCustom     = customLib.de.allianz.Project1.new()

def jslAppGeneral = customLib.de.allianz.app.BuildGeneral.new()

def manual_commit_sha

def bff = false
def fe = false

jslGhe.getAllChangeDirsInPullRequest(config, pr_number).each() {
                    if (it.equals('workbench-bff')) bff = true
                    if (it.equals('workbench-fe')) fe = true  
                }

// for questions about this job ask mario akermann/tobias pfeifer from team pipeline
pipeline {
    agent { label job_config.job.agent }
    stages {
    stage('Prepare') {
      steps {
        echo "prepare checkout"
        script {
          jslGeneral.clean()
          echo "action: \${action} pr_number: \${pr_number}"
                    jslGhe.getAllChangeDirsInPullRequest(config, pr_number).each() {
                        if (it.equals('workbench-bff')) bff = true
                        if (it.equals('workbench-fe')) fe = true  
                    }
                    jslAppGeneral.prep(config,commit_sha)
                    jslAppGeneral.build(config, commit_sha,commits_url,pr_title)
          jslGit.checkout( config, "kobbyopoku", "project-1", branch_name)
          }
        }
      }
stage('Build') {
            steps {
                echo "Build"
                script {
                      jslCustom.build()
                }
            }    
        }
        stage('Component Tests') {
            steps {
                echo "Component Tests"
                script {
                    jslCustom.componentTest()
                }
            }    
        }
        stage('Integration Tests') {
            steps {
                echo "Integration Tests"
                script {
                        jslCustom.integrationTest()
                }
            }    
        }
    }
}
"""
def job = pipelineJob("project-1_Feature_Build");

job.with {

    parameters {
        stringParam('branch_name','', 'name of the branch to build')
    }

    authenticationToken('s88NECR9MtT2rwfOCIRsM4xouTofhOds')
        
    definition {
        cps {
            script(scripts)
            sandbox()
        }
    }
}  
