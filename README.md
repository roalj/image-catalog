docker run -d --name pg-image --network-alias=postgres -itd --network=rso -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=image -p 5432:5432 postgres:12

docker image build . -t  rkosir123/image-catalog
docker run -itd --network=rso -p 8080:8080 rkosir123/image-catalog


POST body
    {
        "description": "TEST",
        "height": 1325,
        "title": "New title",
        "uri": "URL",
        "width": 1280
    }