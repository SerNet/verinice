.DELETE_ON_ERROR:
.PHONY: clean

include ../include.mk

PDF=verinice-Benutzerdokumentation.pdf
ZIP=verinice-Benutzerdokumentation.zip

all: pdf zip

pdf: $(PDF)

zip: $(ZIP)

clean:
	rm -rf $(BIN)
	rm -f $(PDF) $(ZIP)

