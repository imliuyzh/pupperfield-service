.PHONY: all build clean populate

all: clean build populate

build:
	go fmt
	go vet
	CGO_ENABLED=0 go build -o populator -ldflags "-extldflags=-static -s -w"

clean:
	-rm dogs.db populator

populate:
	./populator