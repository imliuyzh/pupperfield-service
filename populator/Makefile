.PHONY: all build clean lint populate

all: clean build populate

build: lint
	CGO_ENABLED=0 go build -o populator -ldflags "-extldflags=-static -s -w"

clean:
	-rm dogs.db populator

lint:
	go fmt
	go vet
	go tool staticcheck
	go tool govulncheck

list-outdated:
	go list -u -f '{{if (and (not (or .Main .Indirect)) .Update)}}{{.Path}}: {{.Version}} -> {{.Update.Version}}{{end}}' -m all

populate:
	@./populator