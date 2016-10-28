docker build -t front-end .
docker run -it --rm -p 8888:8080 front-end
:: Open your browser the url http://localhost:8888/front-end