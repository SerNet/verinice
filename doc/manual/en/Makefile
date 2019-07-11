.DELETE_ON_ERROR:
.PHONY: clean

include ../include.mk

all: pdf zip

PDF=verinice-user_manual_EN.pdf
ZIP=verinice-user_manual_EN.zip

all: pdf zip

pdf: $(PDF)

zip: $(ZIP)

clean:
	rm -rf $(BIN)
	rm -f $(PDF) $(ZIP)

