.PHONY: all build clean lint run

all: clean build run

build: lint
	CGO_ENABLED=0 go build -o populator -ldflags "-extldflags=-static -s -w"

clean:
	-rm dogs.db populator

lint:
	go fmt
	go vet

run:
	./populator