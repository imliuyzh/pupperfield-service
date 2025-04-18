.PHONY: all build clean lint populate

all: clean build populate

build: lint
	CGO_ENABLED=0 go build -o populator -ldflags "-extldflags=-static -s -w"

clean:
	-rm dogs.db populator

lint:
	go fmt
	go vet

populate:
	@./populator