all: build run

build: populator.go
	CGO_ENABLED=0 go build -o populator -ldflags "-extldflags=-static"

.PHONY: clean
clean:
	rm dogs.db populator

run: populator
	./populator