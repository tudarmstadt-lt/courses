# Courses
Source code of projects related to courses taught by LT (also student projects)
In general consider to add projects as _git submodules_. Submodules appear as folders in the git repository hierarchy (although with a changed icon in github) and refer to other git repositories, which might be private.

## Working with submodules using `watson_speechandtext_demo` as example:

goal: have `remstef/WatsonSpeechTextDemo` as a submodule in `tudarmstadt-lt/courses/qa-examples/watson_speechandtext_demo`

- clone courses repository 
    ```
$ git clone https://github.com/tudarmstadt-lt/courses.git courses-lt
    ```

- add submodule
    ```
courses-lt/qa-examples$ git submodule add https://github.com/remstef/WatsonSpeechTextDemo.git watson_speechandtext_demo
    ```
    
- clone submodules, after main repository was cloned already
    ```
courses-lt/qa-examples/watson_speechandtext_demo$ git submodule init
courses-lt/qa-examples/watson_speechandtext_demo$ git submodule update
    ```

- clone project incl. submodules
    ```
$ git clone --recursive https://github.com/tudarmstadt-lt/courses.git courses-lt
    ```
    
- within the submodule folder everything is standard git and refers to the remote repository of the submodule, e.g. `git pull`, to update the contents from `remstef/WatsonSpeechTextDemo` or `git commit` and `git push` to push edited content to `remstef/WatsonSpeechTextDemo`.

- sync new content from the submodule's repository with the main repository (pull changes, commit updates, push changes)
    ```
courses-lt/qa-examples/watson_speechandtext_demo$ git pull
courses-lt/qa-examples$ git commit watson_speechandtext_demo -m 'updated submodule'
courses-lt/qa-examples$ git push
    ```
    
