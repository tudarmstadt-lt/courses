# Marvel Information-Network
##How to deploy the front-end: 
- Run one of the shell scripts (depending on your platform) in the docker folder. The script will deploy the already built .war file.
- The front-end should now be available on http://localhost:8888/front-end

##Build the front-end
If you want to build the .war file on your own:

- Make sure you have the correct credential files in the correct folders (see the project report for details).

- Execute: 

```
cd front-end
mvn package
```

- At last copy the built .war file to the docker folder and execute one of the shell scripts.
