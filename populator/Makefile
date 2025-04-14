all: clean build run

build:
	CGO_ENABLED=0 go build -o populator -ldflags "-extldflags=-static -s -w"

.PHONY: clean
clean:
	-rm dogs.db populator

run:
	./populator