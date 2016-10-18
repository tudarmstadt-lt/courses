# Courses
Source code of projects related to courses taught by LT (also student projects)
In general consider to add projects as _git submodules_

## Working with submodules using `watson_speechandtext_demo` as example:

- clone courses repository 

    `$ git clone https://github.com/tudarmstadt-lt/courses.git courses-lt`

- add submodule

    `courses-lt/qa-examples$ git submodule add https://github.com/remstef/WatsonSpeechTextDemo.git watson_speechandtext_demo`

- clone submodules, after main repository was cloned already

    `courses-lt/qa-examples/watson_speechandtext_demo$ git submodule init`
    `courses-lt/qa-examples/watson_speechandtext_demo$ git submodule update`

- clone project incl. submodules

    `$ git clone --recursive https://github.com/tudarmstadt-lt/courses.git courses-lt`
    
- within the submodule folder everything is standard git again, e.g. `git pull` to update the contents from the remote repository of the submodule
