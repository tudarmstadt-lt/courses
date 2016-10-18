# Courses
Source code of projects related to courses taught by LT (also student projects)
In general consider to add projects as _git submodules_. Submodules appear as folders in the git repository hierarchy (although with a changed icon in github) and refer to other git repositories, which might be private.

## Working with submodules using `watson_speechandtext_demo` as example:

goal: have `remstef/WatsonSpeechTextDemo` as a submodule in `tudarmstadt-lt/courses/qa-examples/watson_speechandtext_demo`

1. clone `tudarmstadt-lt/courses` repository 
    ```
$ git clone https://github.com/tudarmstadt-lt/courses.git courses-lt
    ```

2. add submodule
    ```
courses-lt/qa-examples$ git submodule add https://github.com/remstef/WatsonSpeechTextDemo.git watson_speechandtext_demo
    ```
    
3. clone submodules, after the main repository was cloned
    ```
courses-lt/qa-examples/watson_speechandtext_demo$ git submodule init
courses-lt/qa-examples/watson_speechandtext_demo$ git submodule update
    ```
    
4. within the submodule folder everything is standard git and refers to the remote repository of the submodule, e.g. `git pull`, to update the contents from `remstef/WatsonSpeechTextDemo` or `git commit` and `git push` to push edited content to `remstef/WatsonSpeechTextDemo`.

5. sync new content from the submodule's repository with the main repository (pull changes, commit updates, push changes)
    ```
courses-lt/qa-examples/watson_speechandtext_demo$ git pull
courses-lt/qa-examples$ git commit watson_speechandtext_demo -m 'updated submodule'
courses-lt/qa-examples$ git push
    ```
    
- Note, when you clone `tudarmstadt-lt/courses`, the submodules are not cloned, they appear as empty folders. Run step `3.` to pull the content. Alternatively you can add the `--recursive` switch to the `git clone` command in order to tell git to checkout also the submodules.
    ```
$ git clone --recursive https://github.com/tudarmstadt-lt/courses.git courses-lt
    ```
    
