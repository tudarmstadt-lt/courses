docker build -t infonetwork:demo .
docker run -d -p ${1:-8888}:8080 --name infornetwork infonetwork:demo
## Open your browser the url http://localhost:8888/front-end
